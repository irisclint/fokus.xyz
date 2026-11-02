package xyz.fokus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.loader.api.FabricLoader;
import xyz.fokus.FokusMod;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.TexturesModule;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.Setting;

public final class ConfigIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FORMAT = 2;
    private static final Object[][] OLD_DEFAULTS = new Object[][]{
        {"Wasserzeichen", "x", 0.005},
        {"Wasserzeichen", "y", 0.005},
        {"Keystrokes", "x", 0.005},
        {"Keystrokes", "y", 0.27},
        {"FPS", "enabled", true},
        {"CPS", "enabled", true},
        {"Lyrics", "settings/Höhe am Himmel", 30.0},
        {"Now Playing", "settings/Höhe am Himmel", 12.0}
    };
    private static final String PREFIX = "fokus.xyz:";

    private ConfigIO() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fokus.json");
    }

    public static void load() {
        Path file = file();
        if (Files.exists(file)) {
            try {
                apply(JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject());
            } catch (RuntimeException | IOException var2) {
                FokusMod.LOG.error("fokus.xyz: could not read config, using defaults", var2);
            }
        }
    }

    private static void forgetOldDefaults(JsonObject modules) {
        for (Object[] d : OLD_DEFAULTS) {
            JsonElement m = modules.get((String)d[0]);
            if (m != null && m.isJsonObject()) {
                JsonObject o = m.getAsJsonObject();
                String field = (String)d[1];
                if (field.startsWith("settings/")) {
                    o = o.has("settings") && o.get("settings").isJsonObject() ? o.getAsJsonObject("settings") : null;
                    field = field.substring("settings/".length());
                }

                if (o != null && o.has(field) && o.get(field).isJsonPrimitive()) {
                    JsonPrimitive v = o.getAsJsonPrimitive(field);
                    boolean same = d[2] instanceof Boolean b
                        ? v.isBoolean() && v.getAsBoolean() == b
                        : v.isNumber() && Math.abs(v.getAsDouble() - (Double)d[2]) < 1.0E-6;
                    if (same) {
                        o.remove(field);
                    }
                }
            }
        }
    }

    public static void apply(JsonObject root) {
        boolean sameKeys = root.has("keys") && Compat.backend().equals(root.get("keys").getAsString());
        JsonObject modules = root.getAsJsonObject("modules");
        if (modules != null) {
            if (!root.has("format") || root.get("format").getAsInt() < 2) {
                forgetOldDefaults(modules);
            }

            for (Module m : Modules.all()) {
                JsonElement el = modules.get(m.name);
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    if (o.has("enabled")) {
                        m.loadEnabled(o.get("enabled").getAsBoolean());
                    }

                    if (sameKeys && o.has("bind")) {
                        m.bind.load(o.get("bind"));
                    }

                    if (m instanceof HudModule hud && o.has("x") && o.has("y")) {
                        hud.px = o.get("x").getAsFloat();
                        hud.py = o.get("y").getAsFloat();
                    }

                    JsonObject settings = o.getAsJsonObject("settings");
                    if (settings != null) {
                        for (Setting<?> st : m.settings()) {
                            if (sameKeys || !(st instanceof KeySetting)) {
                                try {
                                    st.load(settings.get(st.name));
                                } catch (RuntimeException var11) {
                                    FokusMod.LOG.warn("fokus.xyz: could not read setting {} of {}", st.name, m.name);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("format", 2);
        root.addProperty("keys", Compat.backend());
        JsonObject modules = new JsonObject();

        for (Module m : Modules.all()) {
            JsonObject o = new JsonObject();
            if (!m.isAlwaysOn() && m.isEnabled() != m.isDefaultEnabled()) {
                o.addProperty("enabled", m.isEnabled());
            }

            if (!m.bind.isDefault()) {
                o.add("bind", m.bind.save());
            }

            if (m instanceof HudModule hud && (hud.px != hud.defaultX || hud.py != hud.defaultY)) {
                o.addProperty("x", hud.px);
                o.addProperty("y", hud.py);
            }

            JsonObject settings = new JsonObject();

            for (Setting<?> st : m.settings()) {
                if (!st.isDefault()) {
                    settings.add(st.name, st.save());
                }
            }

            if (settings.size() > 0) {
                o.add("settings", settings);
            }

            if (o.size() > 0) {
                modules.add(m.name, o);
            }
        }

        root.add("modules", modules);
        return root;
    }

    public static void save() {
        write(file(), toJson());
    }

    private static void write(Path file, JsonObject root) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException var3) {
            FokusMod.LOG.error("fokus.xyz: could not save config", var3);
        }
    }

    private static Path profile(int slot) {
        return FabricLoader.getInstance().getConfigDir().resolve("fokus-profiles").resolve("profil-" + slot + ".json");
    }

    public static boolean hasProfile(int slot) {
        return Files.exists(profile(slot));
    }

    public static void saveProfile(int slot) {
        write(profile(slot), toJson());
    }

    public static boolean loadProfile(int slot) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(profile(slot), StandardCharsets.UTF_8)).getAsJsonObject();
            String pack = TexturesModule.packSignature();
            Modules.resetAll();
            apply(root);
            repackIfChanged(pack);
            save();
            return true;
        } catch (RuntimeException | IOException var3) {
            FokusMod.LOG.warn("fokus.xyz: could not load profile {}", slot, var3);
            return false;
        }
    }

    private static void repackIfChanged(String before) {
        if (!TexturesModule.packSignature().equals(before)) {
            TexturesModule.applyAll();
        }
    }

    public static String shareCode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try (GZIPOutputStream gz = new GZIPOutputStream(bytes)) {
                gz.write(new Gson().toJson(toJson()).getBytes(StandardCharsets.UTF_8));
            }

            return "fokus.xyz:" + Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException var6) {
            throw new IllegalStateException(var6);
        }
    }

    public static boolean applyShareCode(String text) {
        if (text == null) {
            return false;
        } else {
            String t = text.trim();
            if (!t.startsWith("fokus.xyz:")) {
                return false;
            } else {
                try {
                    byte[] packed = Base64.getDecoder().decode(t.substring("fokus.xyz:".length()));
                    GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(packed));

                    String json;
                    try {
                        byte[] raw = gz.readNBytes(2000000);
                        json = new String(raw, StandardCharsets.UTF_8);
                    } catch (Throwable var8) {
                        try {
                            gz.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }

                        throw var8;
                    }

                    gz.close();
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    if (!root.has("modules")) {
                        return false;
                    } else {
                        String pack = TexturesModule.packSignature();
                        Modules.resetAll();
                        apply(root);
                        repackIfChanged(pack);
                        save();
                        return true;
                    }
                } catch (RuntimeException | IOException var9) {
                    FokusMod.LOG.warn("fokus.xyz: pasted text is not a fokus.xyz setup", var9);
                    return false;
                }
            }
        }
    }
}
