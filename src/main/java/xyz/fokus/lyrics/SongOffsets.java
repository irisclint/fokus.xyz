package xyz.fokus.lyrics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import xyz.fokus.FokusMod;

/**
 * How far each song's lyrics are moved, in seconds: synced lyrics differ per song, so one global
 * offset never fits all. Kept in config/fokus-lyrics-offsets.json.
 */
public final class SongOffsets {
    private static final Map<String, Double> OFFSETS = new HashMap<>();
    private static boolean loaded;

    private SongOffsets() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fokus-lyrics-offsets.json");
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            if (Files.exists(file())) {
                JsonObject o = JsonParser.parseString(Files.readString(file(), StandardCharsets.UTF_8)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                    OFFSETS.put(e.getKey(), e.getValue().getAsDouble());
                }
            }
        } catch (RuntimeException | IOException e) {
            FokusMod.LOG.warn("fokus.xyz: could not read lyrics offsets", e);
        }
    }

    public static double get(String song) {
        load();
        return OFFSETS.getOrDefault(song, 0.0);
    }

    /** Moves the song's lyrics by delta seconds; returns the new offset. */
    public static double nudge(String song, double delta) {
        load();
        double v = Math.round((get(song) + delta) * 10.0) / 10.0;
        v = Math.max(-10.0, Math.min(10.0, v));
        if (Math.abs(v) < 0.05) {
            OFFSETS.remove(song);
            v = 0.0;
        } else {
            OFFSETS.put(song, v);
        }
        save();
        return v;
    }

    private static void save() {
        JsonObject o = new JsonObject();
        OFFSETS.forEach(o::addProperty);
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), o.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            FokusMod.LOG.warn("fokus.xyz: could not save lyrics offsets", e);
        }
    }
}
