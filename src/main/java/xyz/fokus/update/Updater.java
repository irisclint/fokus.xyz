package xyz.fokus.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import xyz.fokus.FokusMod;

public final class Updater {
    public static final String MANIFEST = System.getProperty("fokus.updateUrl", "https://github.com/irisclint/fokus.xyz/releases/latest/download/update.json");
    private static volatile Updater.State state = Updater.State.IDLE;
    private static volatile String latest = "";
    private static volatile Path pending;
    private static Path installed;
    private static boolean hookArmed;
    private static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).connectTimeout(Duration.ofSeconds(8L)).build();

    private Updater() {
    }

    public static Updater.State state() {
        return state;
    }

    public static String latestVersion() {
        return latest;
    }

    public static synchronized void check() {
        if (state != Updater.State.CHECKING && state != Updater.State.DOWNLOADING && state != Updater.State.READY) {
            state = Updater.State.CHECKING;
            Thread t = new Thread(Updater::run, "fokus-update");
            t.setDaemon(true);
            t.start();
        }
    }

    private static void run() {
        try {
            installed = ownJar();
            if (installed == null) {
                state = Updater.State.UP_TO_DATE;
                return;
            }

            JsonObject manifest = JsonParser.parseString(get(MANIFEST)).getAsJsonObject();
            String version = manifest.get("version").getAsString();
            latest = version;
            if (compare(version, FokusMod.VERSION) <= 0) {
                state = Updater.State.UP_TO_DATE;
                return;
            }

            String sha = manifest.get("sha256").getAsString().toLowerCase(Locale.ROOT);
            Path target = installed.resolveSibling(installed.getFileName() + ".update");
            if (!Files.exists(target) || !sha.equals(sha256(target))) {
                state = Updater.State.DOWNLOADING;
                Path tmp = installed.resolveSibling(installed.getFileName() + ".download");
                HttpRequest req = HttpRequest.newBuilder(URI.create(manifest.get("jar").getAsString()))
                    .timeout(Duration.ofMinutes(2L))
                    .header("User-Agent", "fokus.xyz/" + FokusMod.VERSION)
                    .build();
                HttpResponse<InputStream> res = HTTP.send(req, BodyHandlers.ofInputStream());
                if (res.statusCode() != 200) {
                    throw new IllegalStateException("download answered " + res.statusCode());
                }

                try (InputStream in = res.body()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }

                if (!sha.equals(sha256(tmp))) {
                    Files.deleteIfExists(tmp);
                    throw new IllegalStateException("checksum does not match - download discarded");
                }

                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }

            pending = target;
            armInstall();
            state = Updater.State.READY;
            FokusMod.LOG.info("fokus.xyz {} downloaded; it is installed when the game closes", version);
        } catch (Exception var12) {
            state = Updater.State.FAILED;
            FokusMod.LOG.warn("fokus.xyz: update check failed: {}", var12.toString());
        }
    }

    private static Path ownJar() {
        return FabricLoader.getInstance()
            .getModContainer("fokus")
            .flatMap(c -> c.getOrigin().getPaths().stream().findFirst())
            .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".jar") && Files.isRegularFile(p))
            .orElse(null);
    }

    private static synchronized void armInstall() {
        if (!hookArmed) {
            hookArmed = true;
            Runtime.getRuntime().addShutdownHook(new Thread(Updater::launchInstaller, "fokus-update-install"));
        }
    }

    private static void launchInstaller() {
        Path from = pending;
        Path to = installed;
        if (from != null && to != null && Files.exists(from)) {
            long pid = ProcessHandle.current().pid();

            try {
                List<String> cmd;
                if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
                    String script = "$ErrorActionPreference='SilentlyContinue';Wait-Process -Id "
                        + pid
                        + " -Timeout 120;for($i=0;$i -lt 40;$i++){try{Move-Item -LiteralPath "
                        + ps(from)
                        + " -Destination "
                        + ps(to)
                        + " -Force -ErrorAction Stop;break}catch{Start-Sleep -Milliseconds 500}}";
                    cmd = List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-Command", script);
                } else {
                    String script = "while kill -0 " + pid + " 2>/dev/null; do sleep 0.5; done; mv -f " + sh(from) + " " + sh(to);
                    cmd = List.of("/bin/sh", "-c", script);
                }

                new ProcessBuilder(cmd)
                    .redirectOutput(java.lang.ProcessBuilder.Redirect.DISCARD)
                    .redirectError(java.lang.ProcessBuilder.Redirect.DISCARD)
                    .start();
            } catch (Exception var6) {
            }
        }
    }

    private static String ps(Path p) {
        return "'" + p.toAbsolutePath().toString().replace("'", "''") + "'";
    }

    private static String sh(Path p) {
        return "'" + p.toAbsolutePath().toString().replace("'", "'\\''") + "'";
    }

    private static String get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(10L))
            .header("User-Agent", "fokus.xyz/" + FokusMod.VERSION)
            .build();
        HttpResponse<String> res = HTTP.send(req, BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() != 200) {
            throw new IllegalStateException("manifest answered " + res.statusCode());
        } else {
            return res.body();
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[65536];

            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
        }

        return HexFormat.of().formatHex(md.digest());
    }

    static int compare(String a, String b) {
        String[] x = a.replaceAll("[^0-9.]", "").split("\\.");
        String[] y = b.replaceAll("[^0-9.]", "").split("\\.");

        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length && !x[i].isEmpty() ? Integer.parseInt(x[i]) : 0;
            int q = i < y.length && !y[i].isEmpty() ? Integer.parseInt(y[i]) : 0;
            if (p != q) {
                return Integer.compare(p, q);
            }
        }

        return 0;
    }

    public static enum State {
        IDLE,
        CHECKING,
        UP_TO_DATE,
        DOWNLOADING,
        READY,
        FAILED;
    }
}
