package xyz.fokus.lyrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import xyz.fokus.FokusMod;

public final class MediaWatcher {
    private static final Path COVER = FabricLoader.getInstance().getConfigDir().resolve("fokus-cover.img");
    private static volatile MediaWatcher.Track current;
    private static Process process;
    private static volatile long wantedAt;

    private MediaWatcher() {
    }

    public static MediaWatcher.Track current() {
        return current;
    }

    public static boolean supported() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
    }

    public static void idleCheck() {
        if (process != null && System.currentTimeMillis() - wantedAt > 3000L) {
            stop();
        }
    }

    public static synchronized void start() {
        wantedAt = System.currentTimeMillis();
        if ((process == null || !process.isAlive()) && supported()) {
            try {
                Path script = FabricLoader.getInstance().getConfigDir().resolve("fokus-media.ps1");

                try (InputStream in = MediaWatcher.class.getResourceAsStream("/assets/fokus/media.ps1")) {
                    if (in == null) {
                        return;
                    }

                    Files.createDirectories(script.getParent());
                    Files.copy(in, script, StandardCopyOption.REPLACE_EXISTING);
                }

                process = new ProcessBuilder(
                        "powershell.exe",
                        "-NoProfile",
                        "-NonInteractive",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-WindowStyle",
                        "Hidden",
                        "-File",
                        script.toString(),
                        COVER.toString()
                    )
                    .redirectError(Redirect.DISCARD)
                    .start();
                Process p = process;
                Thread reader = new Thread(() -> read(p), "fokus-media");
                reader.setDaemon(true);
                reader.start();
                Runtime.getRuntime().addShutdownHook(new Thread(p::destroy, "fokus-media-stop"));
            } catch (Exception var6) {
                FokusMod.LOG.warn("fokus.xyz: could not start the media watcher", var6);
                process = null;
            }
        }
    }

    public static synchronized void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }

        current = null;
    }

    private static void read(Process p) {
        String line;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            while ((line = r.readLine()) != null) {
                current = parse(line);
            }
        } catch (Exception var6) {
        }

        current = null;
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    static String drawable(String s) {
        StringBuilder out = new StringBuilder(s.length());
        s.codePoints().forEach(cp -> {
            boolean selector = cp >= 65024 && cp <= 65039 || cp == 8205;
            if (cp <= 65535 && !selector) {
                out.appendCodePoint(cp);
            }
        });
        return out.toString().replaceAll("\\s{2,}", " ").trim();
    }

    private static MediaWatcher.Track parse(String line) {
        try {
            JsonObject o = JsonParser.parseString(line.replace("\ufeff", "")).getAsJsonObject();
            if (!o.has("title")) {
                return null;
            } else {
                String title = drawable(o.get("title").getAsString());
                if (title.isBlank()) {
                    return null;
                } else {
                    int cover = o.has("cover") ? o.get("cover").getAsInt() : 0;
                    CoverArt.offer(cover, COVER);
                    return new MediaWatcher.Track(
                        title,
                        drawable(str(o, "artist")),
                        o.get("position").getAsDouble(),
                        o.get("duration").getAsDouble(),
                        o.get("updated").getAsLong(),
                        "Playing".equals(str(o, "status")),
                        str(o, "app"),
                        str(o, "tab"),
                        cover
                    );
                }
            }
        } catch (RuntimeException var4) {
            return null;
        }
    }

    public record Track(String title, String artist, double position, double duration, long updated, boolean playing, String app, String tab, int cover) {
        public double positionNow() {
            double p = this.playing ? this.position + (System.currentTimeMillis() - this.updated) / 1000.0 : this.position;
            return this.duration > 0.0 ? Math.min(p, this.duration) : p;
        }

        public boolean live() {
            return this.duration <= 0.5;
        }

        public String source() {
            return Sources.name(this.app, this.tab, this.artist, this.title);
        }

        public String cleanArtist() {
            return Sources.cleanArtist(this.artist);
        }
    }
}
