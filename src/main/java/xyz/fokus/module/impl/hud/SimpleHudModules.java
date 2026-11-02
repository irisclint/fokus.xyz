package xyz.fokus.module.impl.hud;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Matrix3x2fStack;
import xyz.fokus.Lang;
import xyz.fokus.Stats;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.util.Anim;
import xyz.fokus.util.R2D;

public final class SimpleHudModules {
    private SimpleHudModules() {
    }

    public static class Clock extends HudModule {
        public final ModeSetting format = this.add(new ModeSetting("Format", "24 h", "24 h", "12 h"));
        public final BoolSetting seconds = this.add(new BoolSetting("Sekunden", false));
        public final BoolSetting date = this.add(new BoolSetting("Datum", false));
        private DateTimeFormatter formatter;
        private String pattern = "";
        private String text = "";

        public Clock() {
            super("Uhr", "Die aktuelle Uhrzeit (und das Datum).", 0.005F, 0.31F);
        }

        @Override
        public void onTick(Minecraft mc) {
            String p = (this.date.on() ? "dd.MM. " : "")
                + (this.format.is("12 h") ? "h:mm" : "HH:mm")
                + (this.seconds.on() ? ":ss" : "")
                + (this.format.is("12 h") ? " a" : "");
            if (!p.equals(this.pattern)) {
                this.pattern = p;
                this.formatter = DateTimeFormatter.ofPattern(p, Locale.ROOT);
            }

            this.text = LocalDateTime.now().format(this.formatter);
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "", this.text.isEmpty() ? "12:00" : this.text);
        }
    }

    public static class Combo extends HudModule {
        public final BoolSetting fightOnly = this.add(new BoolSetting("Nur im Kampf zeigen", true));
        private final Anim bump = new Anim(0.0F, 10.0F);
        private int lastCombo;

        public Combo() {
            super("Combo", "Zählt deine Treffer in Folge, ohne getroffen zu werden.", 0.43F, 0.56F);
            this.enabledByDefault();
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            int combo = editor && Stats.combo == 0 ? 3 : Stats.combo;
            if (Stats.combo > this.lastCombo) {
                this.bump.snap(1.0F);
            }

            this.lastCombo = Stats.combo;
            if (!editor && this.fightOnly.on() && combo == 0) {
                this.w = 40.0F;
                this.h = 14.0F;
            } else {
                this.bump.to(0.0F);
                float s = 1.0F + this.bump.get() * 0.25F;
                String value = String.valueOf(combo);
                String label = combo == 1 ? "Hit" : "Combo";
                this.w = 9.0F + font.width(label) + 4.0F + font.width(value) * 1.25F;
                this.h = 14.0F;
                this.box(g, this.w, this.h);
                R2D.text(g, font, label, 4.5F, 3.0F, this.label(), Theme.shadow());
                float vx = 4.5F + font.width(label) + 4.0F;
                Matrix3x2fStack pose = g.pose();
                pose.pushMatrix();
                pose.translate(vx + font.width(value) * 0.625F, 7.0F);
                pose.scale(s * 1.25F, s * 1.25F);
                R2D.text(g, font, value, -font.width(value) / 2.0F, -4.5F, this.text(), Theme.shadow());
                pose.popMatrix();
            }
        }
    }

    public static class Coordinates extends HudModule {
        private static final String[] DIRS = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        public final BoolSetting direction = this.add(new BoolSetting("Himmelsrichtung", true));
        public final BoolSetting compact = this.add(new BoolSetting("Einzeilig", false));
        public final ModeSetting decimals = this.add(new ModeSetting("Nachkommastellen", "1", "0", "1", "2"));
        private final String[] values = new String[]{"0", "64", "0"};
        private String dir = "N";
        private String line = "0 64 0";

        public Coordinates() {
            super("Koordinaten", "X, Y, Z und Blickrichtung.", 0.005F, 0.19F);
        }

        @Override
        public void onTick(Minecraft mc) {
            LocalPlayer p = mc.player;
            if (p != null) {
                String f = "%." + this.decimals.get() + "f";
                this.values[0] = String.format(Locale.ROOT, f, p.getX());
                this.values[1] = String.format(Locale.ROOT, f, p.getY());
                this.values[2] = String.format(Locale.ROOT, f, p.getZ());
                this.dir = DIRS[Math.floorMod(Math.round(p.getYRot() / 45.0F), 8)];
                this.line = this.values[0] + " " + this.values[1] + " " + this.values[2] + (this.direction.on() ? "  " + this.dir : "");
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            if (this.compact.on()) {
                this.labelValue(g, font, "XYZ", this.line);
            } else {
                float maxW = Math.max(font.width(this.values[0]), Math.max(font.width(this.values[1]), font.width(this.values[2])));
                this.w = 19.0F + maxW + (this.direction.on() ? 18 : 0);
                this.h = 34.0F;
                this.box(g, this.w, this.h);
                String[] labels = new String[]{"X", "Y", "Z"};

                for (int i = 0; i < 3; i++) {
                    R2D.text(g, font, labels[i], 4.5F, 3 + i * 10, this.label(), Theme.shadow());
                    R2D.text(g, font, this.values[i], 14.5F, 3 + i * 10, this.text(), Theme.shadow());
                }

                if (this.direction.on()) {
                    R2D.textCentered(g, font, this.dir, this.w - 10.0F, this.h / 2.0F - 6.0F, 1.3F, this.text(), Theme.shadow());
                }
            }
        }
    }

    public static class Cps extends HudModule {
        public final BoolSetting right = this.add(new BoolSetting("Rechtsklicks zeigen", true));
        private String text = "0";

        public Cps() {
            super("CPS", "Klicks pro Sekunde (links | rechts).", 0.005F, 0.11F);
            if (!Compat.irisClientHud()) {
                this.enabledByDefault();
            }
        }

        @Override
        public void onTick(Minecraft mc) {
            this.text = this.right.on() ? Stats.cps(true) + " | " + Stats.cps(false) : String.valueOf(Stats.cps(true));
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "CPS", this.text);
        }
    }

    public static class Fps extends HudModule {
        private String text = "0";

        public Fps() {
            super("FPS", "Zeigt deine Bilder pro Sekunde.", 0.005F, 0.07F);
            if (!Compat.irisClientHud()) {
                this.enabledByDefault();
            }
        }

        @Override
        public void onTick(Minecraft mc) {
            this.text = String.valueOf(mc.getFps());
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "FPS", this.text);
        }
    }

    public static class Ping extends HudModule {
        private String text = "0 ms";

        public Ping() {
            super("Ping", "Deine Verbindung zum Server in Millisekunden.", 0.005F, 0.15F);
        }

        @Override
        public void onTick(Minecraft mc) {
            int ms = 0;
            if (mc.player != null && mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                if (info != null) {
                    ms = info.getLatency();
                }
            }

            this.text = ms + " ms";
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "Ping", this.text);
        }
    }

    public static class Reach extends HudModule {
        private String text = "—";

        public Reach() {
            super("Reach", "Abstand zum zuletzt getroffenen Gegner.", 0.53F, 0.56F);
        }

        @Override
        public void onTick(Minecraft mc) {
            boolean fresh = System.currentTimeMillis() - Stats.lastReachAt < 3000L && Stats.lastReach >= 0.0;
            this.text = fresh ? String.format(Locale.ROOT, "%.2f m", Stats.lastReach) : "—";
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "Reach", editor && this.text.equals("—") ? "2.94 m" : this.text);
        }
    }

    public static class ServerInfo extends HudModule {
        private String text = "—";

        public ServerInfo() {
            super("Server", "Die Adresse des Servers, auf dem du spielst.", 0.005F, 0.39F);
        }

        @Override
        public void onTick(Minecraft mc) {
            ServerData data = mc.getCurrentServer();
            this.text = data != null ? data.ip : (mc.isLocalServer() ? Lang.t("Einzelspieler") : "—");
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "Server", this.text);
        }
    }

    public static class Speed extends HudModule {
        public final ModeSetting unit = this.add(new ModeSetting("Einheit", "Blöcke/s", "Blöcke/s", "km/h"));
        private String text = "0.00 b/s";

        public Speed() {
            super("Speed", "Deine Geschwindigkeit in Blöcken pro Sekunde.", 0.005F, 0.35F);
        }

        @Override
        public void onTick(Minecraft mc) {
            this.text = this.unit.is("km/h") ? String.format(Locale.ROOT, "%.1f km/h", Stats.speed * 3.6) : String.format(Locale.ROOT, "%.2f b/s", Stats.speed);
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "Speed", this.text);
        }
    }
}
