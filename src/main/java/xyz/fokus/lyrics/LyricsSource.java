package xyz.fokus.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xyz.fokus.FokusMod;

public final class LyricsSource {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6L)).build();
    private static final Pattern TAG = Pattern.compile("\\[(\\d+):(\\d+(?:\\.\\d+)?)]");
    private static volatile String key = "";
    private static volatile List<LyricsSource.Line> lines = List.of();
    private static volatile boolean loading;
    private static final Map<String, List<LyricsSource.Line>> CACHE = Collections.synchronizedMap(
        new LinkedHashMap<String, List<LyricsSource.Line>>(16, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Entry<String, List<LyricsSource.Line>> eldest) {
                return this.size() > 40;
            }
        }
    );
    private static volatile long retryAt;
    private static volatile int attempts;
    private static final ThreadLocal<boolean[]> TROUBLE = new InheritableThreadLocal<boolean[]>() {
        protected boolean[] initialValue() {
            return new boolean[1];
        }
    };
    public static volatile boolean secondSource = true;

    private LyricsSource() {
    }

    public static List<LyricsSource.Line> forTrack(MediaWatcher.Track track) {
        String k = track.artist() + "\u0000" + track.title();
        boolean retry = !loading && retryAt > 0L && System.currentTimeMillis() >= retryAt && k.equals(key);
        if (!k.equals(key) && CACHE.containsKey(k)) {
            key = k;
            lines = CACHE.get(k);
            loading = false;
            retryAt = 0L;
            return lines;
        } else {
            if (!k.equals(key) || retry) {
                if (!k.equals(key)) {
                    attempts = 0;
                }

                key = k;
                lines = List.of();
                loading = true;
                retryAt = 0L;
                Thread t = new Thread(() -> load(k, track), "fokus-lyrics");
                t.setDaemon(true);
                t.start();
            }

            return lines;
        }
    }

    public static boolean loading() {
        return loading;
    }

    static boolean hadTrouble() {
        return TROUBLE.get()[0];
    }

    private static void load(String k, MediaWatcher.Track track) {
        List<LyricsSource.Line> found = List.of();
        TROUBLE.set(new boolean[1]);

        try {
            found = find(track.cleanArtist(), track.title(), track.duration());
        } catch (Exception var4) {
            TROUBLE.get()[0] = true;
            FokusMod.LOG.debug("fokus.xyz: lyrics lookup failed", var4);
        }

        boolean trouble = TROUBLE.get()[0];
        if (!found.isEmpty() || !trouble) {
            CACHE.put(k, found);
        }

        if (k.equals(key)) {
            lines = found;
            if (found.isEmpty() && trouble && attempts < 6) {
                attempts++;
                retryAt = System.currentTimeMillis() + 15000L * (1L << Math.min(attempts - 1, 3));
                return;
            }

            loading = false;
        }
    }

    static List<LyricsSource.Line> find(String artist, String title, double duration) throws Exception {
        String t = clean(title);
        String a = clean(artist);
        String first = a.split("(?i)\\s*(,|&|\\bx\\b|\\bfeat\\.?|\\bft\\.?|\\bwith\\b|/|;)\\s*")[0].trim();
        List<String[]> tries = new ArrayList<>();
        if (t.contains(" - ")) {
            String[] parts = t.split(" - ", 2);
            String[] split = new String[]{clean(parts[0]), clean(parts[1])};
            if (a.isEmpty() || !norm(t).contains(norm(a))) {
                tries.add(split);
            }
        }

        tries.add(new String[]{a, t});
        if (!first.isEmpty() && !first.equals(a)) {
            tries.add(new String[]{first, t});
        }

        if (t.contains(" - ") && tries.size() == (!first.isEmpty() && !first.equals(a) ? 2 : 1)) {
            String[] parts = t.split(" - ", 2);
            tries.add(new String[]{clean(parts[0]), clean(parts[1])});
        }

        CompletableFuture<LyricsSource.Result> second = new CompletableFuture<>();
        if (secondSource) {
            Thread n = new Thread(() -> {
                LyricsSource.Result found = LyricsSource.Result.NONE;

                try {
                    for (String[] attempt : tries) {
                        LyricsSource.Result r = netease(attempt[0], attempt[1], duration);
                        if (r.synced()) {
                            found = r;
                            break;
                        }

                        if (found.lines().isEmpty()) {
                            found = r;
                        }
                    }
                } catch (Exception var8x) {
                    TROUBLE.get()[0] = true;
                }

                second.complete(found);
            }, "fokus-lyrics-2");
            n.setDaemon(true);
            n.start();
        } else {
            second.complete(LyricsSource.Result.NONE);
        }

        LyricsSource.Result first_ = lrclib(tries, first, t, duration, second);
        if (first_.synced()) {
            return first_.lines();
        } else {
            LyricsSource.Result other = second.get(30L, TimeUnit.SECONDS);
            if (other.synced()) {
                return other.lines();
            } else {
                return !first_.lines().isEmpty() ? first_.lines() : other.lines();
            }
        }
    }

    private static LyricsSource.Result lrclib(List<String[]> tries, String first, String t, double duration, CompletableFuture<LyricsSource.Result> second) throws Exception {
        LyricsSource.Result plain = LyricsSource.Result.NONE;

        for (String[] attempt : tries) {
            if (syncedAlready(second)) {
                return plain;
            }

            LyricsSource.Result r = fetch(attempt[0], attempt[1], duration);
            if (r.synced()) {
                return r;
            }

            if (plain.lines().isEmpty()) {
                plain = r;
            }
        }

        if (syncedAlready(second)) {
            return plain;
        } else {
            LyricsSource.Result rx = search("q=" + enc((first.isEmpty() ? "" : first + " ") + t), duration, t);
            return !rx.synced() && !plain.lines().isEmpty() ? plain : rx;
        }
    }

    private static boolean syncedAlready(CompletableFuture<LyricsSource.Result> f) {
        return f.isDone() && f.getNow(LyricsSource.Result.NONE).synced();
    }

    private static LyricsSource.Result netease(String artist, String title, double duration) throws Exception {
        JsonElement found = get("https://music.163.com/api/search/get?type=1&limit=8&s=" + enc((artist.isEmpty() ? "" : artist + " ") + title));
        if (found != null && found.isJsonObject() && found.getAsJsonObject().has("result")) {
            JsonObject result = found.getAsJsonObject().getAsJsonObject("result");
            if (!result.has("songs")) {
                return LyricsSource.Result.NONE;
            } else {
                String wantTitle = norm(title);
                String wantArtist = norm(artist);

                for (JsonElement e : result.getAsJsonArray("songs")) {
                    JsonObject song = e.getAsJsonObject();
                    String name = norm(text(song, "name"));
                    if (!name.isEmpty() && (name.equals(wantTitle) || name.startsWith(wantTitle) || wantTitle.startsWith(name))) {
                        boolean artistOk = false;
                        if (song.has("artists")) {
                            for (JsonElement ar : song.getAsJsonArray("artists")) {
                                String n = norm(text(ar.getAsJsonObject(), "name"));
                                if (!n.isEmpty() && !wantArtist.isEmpty() && (wantArtist.contains(n) || n.contains(wantArtist))) {
                                    artistOk = true;
                                }
                            }
                        }

                        double length = song.has("duration") ? song.get("duration").getAsDouble() / 1000.0 : 0.0;
                        boolean lengthOk = duration > 1.0 && length > 1.0 && Math.abs(length - duration) <= 4.0;
                        if (artistOk || lengthOk) {
                            JsonElement lyric = get("https://music.163.com/api/song/lyric?lv=1&id=" + song.get("id").getAsLong());
                            if (lyric != null && lyric.isJsonObject() && lyric.getAsJsonObject().has("lrc")) {
                                String lrc = text(lyric.getAsJsonObject().getAsJsonObject("lrc"), "lyric");
                                JsonObject asLrclib = new JsonObject();
                                asLrclib.addProperty("syncedLyrics", withoutCredits(lrc));
                                LyricsSource.Result r = parse(asLrclib, duration);
                                if (!r.lines().isEmpty()) {
                                    return r;
                                }
                            }
                        }
                    }
                }

                return LyricsSource.Result.NONE;
            }
        } else {
            return LyricsSource.Result.NONE;
        }
    }

    private static String withoutCredits(String lrc) {
        StringBuilder out = new StringBuilder();

        for (String line : lrc.split("\n")) {
            String text = line.replaceAll("^(\\[[^\\]]*])+", "").trim();
            if (!text.matches("^[^:：]{1,14}\\s*[:：]\\s*.+$") || !line.matches("^\\[00:(0\\d|1\\d)[.:].*")) {
                out.append(line).append('\n');
            }
        }

        return out.toString();
    }

    private static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private static LyricsSource.Result fetch(String artist, String title, double duration) throws Exception {
        if (title.isBlank()) {
            return LyricsSource.Result.NONE;
        } else {
            String q = "artist_name=" + enc(artist) + "&track_name=" + enc(title);
            JsonElement direct = get("https://lrclib.net/api/get?" + q + (duration > 1.0 ? "&duration=" + Math.round(duration) : ""));
            LyricsSource.Result plain = LyricsSource.Result.NONE;
            if (direct != null && direct.isJsonObject()) {
                LyricsSource.Result r = parse(direct.getAsJsonObject(), duration);
                if (r.synced()) {
                    return r;
                }

                plain = r;
            }

            LyricsSource.Result free = search("q=" + enc((artist.isEmpty() ? "" : artist + " ") + title), duration, title);
            if (free.synced()) {
                return free;
            } else {
                LyricsSource.Result r = search(q, duration, null);
                if (r.synced()) {
                    return r;
                } else {
                    return !plain.lines().isEmpty() ? plain : (!free.lines().isEmpty() ? free : r);
                }
            }
        }
    }

    private static LyricsSource.Result search(String query, double duration, String title) throws Exception {
        JsonElement search = get("https://lrclib.net/api/search?" + query);
        if (search != null && search.isJsonArray()) {
            LyricsSource.Result best = LyricsSource.Result.NONE;
            double bestDiff = Double.MAX_VALUE;

            for (JsonElement e : (JsonArray)search) {
                JsonObject o = e.getAsJsonObject();
                if (title != null) {
                    String want = norm(title);
                    String got = norm(text(o, "trackName"));
                    if (got.isEmpty() || !got.equals(want) && !got.startsWith(want) && !want.startsWith(got)) {
                        continue;
                    }
                }

                double d = o.has("duration") && !o.get("duration").isJsonNull() && duration > 1.0 ? Math.abs(o.get("duration").getAsDouble() - duration) : 0.0;
                if (!(duration > 1.0) || !(d > 20.0)) {
                    LyricsSource.Result r = parse(o, duration);
                    if (r.lines().size() > 2) {
                        boolean better = r.synced() && !best.synced() || r.synced() == best.synced() && d < bestDiff;
                        if (better) {
                            bestDiff = d;
                            best = r;
                        }
                    }
                }
            }

            return best;
        } else {
            return LyricsSource.Result.NONE;
        }
    }

    private static JsonElement get(String url) {
        for (int i = 0; i < 3; i++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8L))
                    .header("User-Agent", "fokus.xyz Minecraft mod (https://flexbio.link/1)")
                    .GET()
                    .build();
                HttpResponse<String> res = HTTP.send(req, BodyHandlers.ofString(StandardCharsets.UTF_8));
                int code = res.statusCode();
                if (code == 200) {
                    return JsonParser.parseString(res.body());
                }

                if (code != 429 && code < 500) {
                    return null;
                }
            } catch (InterruptedException var6) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception var7) {
            }

            try {
                Thread.sleep(350L * (i + 1));
            } catch (InterruptedException var5) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        TROUBLE.get()[0] = true;
        return null;
    }

    private static String text(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static LyricsSource.Result parse(JsonObject o, double duration) {
        String synced = text(o, "syncedLyrics");
        if (!synced.isBlank()) {
            List<LyricsSource.Line> out = new ArrayList<>();

            for (String raw : synced.split("\n")) {
                Matcher m = TAG.matcher(raw);
                List<Long> stamps = new ArrayList<>();

                int end;
                for (end = 0; m.find() && m.start() == end; end = m.end()) {
                    stamps.add(Math.round(Integer.parseInt(m.group(1)) * 60000.0 + Double.parseDouble(m.group(2)) * 1000.0));
                }

                String text = raw.substring(end).trim();

                for (long st : stamps) {
                    out.add(new LyricsSource.Line(st, text));
                }
            }

            out.sort((a, b) -> Long.compare(a.ms, b.ms));
            if (out.size() > 2) {
                return new LyricsSource.Result(Collections.unmodifiableList(out), true);
            }
        }

        List<String> plain = new ArrayList<>();

        for (String raw : text(o, "plainLyrics").split("\n")) {
            if (!raw.isBlank()) {
                plain.add(raw.trim());
            }
        }

        if (plain.size() <= 2) {
            return LyricsSource.Result.NONE;
        } else {
            double length = duration > 1.0 ? duration : (o.has("duration") && !o.get("duration").isJsonNull() ? o.get("duration").getAsDouble() : 180.0);
            double start = length * 0.08;
            double step = length * 0.86 / plain.size();
            List<LyricsSource.Line> out = new ArrayList<>();

            for (int i = 0; i < plain.size(); i++) {
                out.add(new LyricsSource.Line(Math.round((start + i * step) * 1000.0), plain.get(i)));
            }

            return new LyricsSource.Result(Collections.unmodifiableList(out), false);
        }
    }

    private static String clean(String s) {
        return s.replaceAll("(?i)\\s*[\\(\\[](official|lyric|audio|video|visualizer|hd|4k|remaster|feat\\.|ft\\.)[^\\)\\]]*[\\)\\]]", "")
            .replaceAll("(?i)\\s+-\\s+(remaster(ed)?|topic).*$", "")
            .replaceAll("(?i)\\s+-\\s+topic$", "")
            .trim();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public record Line(long ms, String text) {
    }

    private record Result(List<LyricsSource.Line> lines, boolean synced) {
        static final LyricsSource.Result NONE = new LyricsSource.Result(List.of(), false);
    }
}
