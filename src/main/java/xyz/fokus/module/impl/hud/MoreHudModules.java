package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix3x2fStack;
import xyz.fokus.Lang;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public final class MoreHudModules {
    private MoreHudModules() {
    }

    public static class ArmorWarning extends HudModule {
        private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        private static final String[] NAMES = new String[]{"Helm", "Brustpanzer", "Hose", "Schuhe"};
        public final NumberSetting below = this.add(new NumberSetting("Warnen unter", 15.0, 5.0, 50.0, 1.0, "%"));
        public final BoolSetting blink = this.add(new BoolSetting("Blinken", true));
        private String text = "";

        public ArmorWarning() {
            super("Rüstungswarnung", "Warnt, wenn ein Rüstungsteil gleich kaputtgeht.", 0.5F, 0.64F);
            this.centered = true;
            this.enabledByDefault();
        }

        @Override
        public void onTick(Minecraft mc) {
            this.text = "";
            if (mc.player != null) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < SLOTS.length; i++) {
                    ItemStack s = mc.player.getItemBySlot(SLOTS[i]);
                    if (!s.isEmpty() && s.isDamageableItem()) {
                        int pct = Math.round(100.0F * (s.getMaxDamage() - s.getDamageValue()) / s.getMaxDamage());
                        if (pct < this.below.f()) {
                            sb.append(sb.isEmpty() ? "" : "  ").append(Lang.t(NAMES[i])).append(' ').append(pct).append('%');
                        }
                    }
                }

                this.text = sb.toString();
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            String t = this.text.isEmpty() && editor ? Lang.t("Helm") + " 9%" : this.text;
            if (t.isEmpty()) {
                this.w = 60.0F;
                this.h = 13.0F;
            } else {
                float pulse = this.blink.on() && !editor ? 0.55F + 0.45F * (float)Math.abs(Math.sin(System.nanoTime() / 2.5E8)) : 1.0F;
                this.w = 9 + font.width(t) + 12;
                this.h = 13.0F;
                this.box(g, this.w, this.h);
                R2D.text(g, font, "!", 5.0F, 2.5F, Colors.alpha(-42406, pulse), Theme.shadow());
                R2D.text(g, font, t, 13.0F, 2.5F, Colors.alpha(this.text(), pulse), Theme.shadow());
            }
        }
    }

    public static class Biome extends HudModule {
        private String text = "Plains";

        public Biome() {
            super("Biom", "Das Biom, in dem du stehst.", 0.005F, 0.65F);
        }

        @Override
        public void onTick(Minecraft mc) {
            if (mc.level != null && mc.player != null && mc.level.getGameTime() % 10L == 0L) {
                String id = mc.level.getBiome(mc.player.blockPosition()).getRegisteredName();
                String name = id.substring(id.indexOf(58) + 1).replace('_', ' ');
                this.text = name.isEmpty() ? "—" : Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, Lang.t("Biom"), this.text);
        }
    }

    public static class Compass extends HudModule {
        private static final String[] NAMES = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        public final NumberSetting width = this.add(new NumberSetting("Breite", 180.0, 100.0, 360.0, 10.0, ""));
        public final NumberSetting range = this.add(new NumberSetting("Sichtfeld", 160.0, 60.0, 300.0, 10.0, "°"));
        public final BoolSetting degrees = this.add(new BoolSetting("Grad zeigen", true));

        public Compass() {
            super("Kompass", "Eine Kompass-Leiste: die Himmelsrichtungen ziehen beim Drehen vorbei.", 0.5F, 0.01F);
            this.centered = true;
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            LocalPlayer p = Minecraft.getInstance().player;
            float heading = p == null ? 0.0F : Math.floorMod(Math.round(p.getYRot() + 180.0F), 360);
            this.w = this.width.f();
            this.h = this.degrees.on() ? 22.0F : 15.0F;
            this.box(g, this.w, this.h);
            float half = this.range.f() / 2.0F;
            float perDeg = (this.w - 8.0F) / this.range.f();
            float cx = this.w / 2.0F;
            int start = (int)Math.floor((heading - half) / 15.0F) * 15;

            for (int mark = start; mark <= heading + half; mark += 15) {
                float off = mark - heading;
                if (!(Math.abs(off) > half)) {
                    float x = cx + off * perDeg;
                    float fade = 1.0F - Math.abs(off) / half;
                    int deg = Math.floorMod(mark, 360);
                    if (deg % 45 == 0) {
                        String name = NAMES[deg / 45];
                        int c = deg == 0 ? this.text() : Colors.alpha(this.text(), 0.85F);
                        R2D.textCentered(g, font, name, x, 3.5F, deg % 90 == 0 ? 1.0F : 0.75F, Colors.alpha(c, 0.25F + fade * 0.75F), Theme.shadow());
                    } else {
                        R2D.rect(g, x - 0.5F, 5.0F, x + 0.5F, 9.0F, Colors.alpha(this.label(), 0.2F + fade * 0.6F));
                    }
                }
            }

            R2D.rect(g, cx - 0.5F, this.h - 4.0F, cx + 0.5F, this.h - 1.0F, this.text());
            if (this.degrees.on()) {
                R2D.textCentered(g, font, Math.round(heading) + "°", cx, 13.0F, 0.7F, this.label(), Theme.shadow());
            }
        }
    }

    public static class Cooldowns extends HudModule {
        public final BoolSetting bars = this.add(new BoolSetting("Balken zeigen", true));
        private final List<ItemStack> items = new ArrayList<>();
        private final List<Float> left = new ArrayList<>();

        public Cooldowns() {
            super("Cooldowns", "Wie lange Enderperle, Windladung, Chorusfrucht oder Schild noch warten müssen.", 0.56F, 0.6F);
            this.enabledByDefault();
        }

        @Override
        public void onTick(Minecraft mc) {
            this.items.clear();
            this.left.clear();
            LocalPlayer p = mc.player;
            if (p != null) {
                List<Identifier> groups = new ArrayList<>();
                Inventory inv = p.getInventory();

                for (int i = 0; i < 10; i++) {
                    ItemStack s = i == 9 ? p.getOffhandItem() : inv.getItem(i);
                    if (!s.isEmpty() && p.getCooldowns().isOnCooldown(s)) {
                        Identifier group = p.getCooldowns().getCooldownGroup(s);
                        if (!groups.contains(group)) {
                            groups.add(group);
                            this.items.add(s.copy());
                            this.left.add(p.getCooldowns().getCooldownPercent(s, 0.0F));
                        }
                    }
                }
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            List<ItemStack> list = this.items;
            List<Float> rest = this.left;
            if (list.isEmpty() && editor) {
                list = List.of(new ItemStack(Items.ENDER_PEARL));
                rest = List.of(0.6F);
            }

            this.w = 22.0F;
            this.h = 4 + Math.max(1, list.size()) * 20;
            if (!list.isEmpty()) {
                this.box(g, this.w, this.h);

                for (int i = 0; i < list.size(); i++) {
                    float y = 3 + i * 20;
                    Matrix3x2fStack pose = g.pose();
                    pose.pushMatrix();
                    pose.translate(3.0F, y);
                    g.item(list.get(i), 0, 0);
                    pose.popMatrix();
                    if (this.bars.on()) {
                        R2D.round(g, 3.0F, y + 17.0F, 16.0F, 1.5F, 0.75F, 822083583);
                        R2D.round(g, 3.0F, y + 17.0F, Math.max(1.0F, 16.0F * rest.get(i)), 1.5F, 0.75F, this.text());
                    }
                }
            }
        }
    }

    public static class DayTime extends HudModule {
        public final BoolSetting day = this.add(new BoolSetting("Tag zeigen", true));
        private String text = "06:00";

        public DayTime() {
            super("Tageszeit", "Die Uhrzeit in der Welt und der wievielte Tag es ist.", 0.005F, 0.61F);
        }

        @Override
        public void onTick(Minecraft mc) {
            if (mc.level != null) {
                long t = mc.level.getOverworldClockTime();
                long ofDay = Math.floorMod(t + 6000L, 24000);
                String clock = String.format(Locale.ROOT, "%02d:%02d", ofDay / 1000L, ofDay % 1000L * 60L / 1000L);
                this.text = this.day.on() ? clock + "  ·  " + Lang.t("Tag") + " " + (t / 24000L + 1L) : clock;
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "", this.text);
        }
    }

    public static class HeldDurability extends HudModule {
        public final ModeSetting format = this.add(new ModeSetting("Format", "Prozent", "Prozent", "Zahl"));
        private ItemStack held = ItemStack.EMPTY;
        private String text = "";

        public HeldDurability() {
            super("Haltbarkeit", "Wie viel das Item in deiner Hand noch aushält.", 0.44F, 0.6F);
        }

        @Override
        public void onTick(Minecraft mc) {
            this.held = ItemStack.EMPTY;
            this.text = "";
            if (mc.player != null) {
                ItemStack s = mc.player.getMainHandItem();
                if (!s.isEmpty() && s.isDamageableItem()) {
                    this.held = s.copy();
                    int rest = s.getMaxDamage() - s.getDamageValue();
                    this.text = this.format.is("Zahl") ? String.valueOf(rest) : Math.round(100.0F * rest / s.getMaxDamage()) + "%";
                }
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            ItemStack s = this.held;
            String t = this.text;
            if (s.isEmpty() && editor) {
                s = new ItemStack(Items.DIAMOND_SWORD);
                t = "87%";
            }

            if (s.isEmpty()) {
                this.w = 40.0F;
                this.h = 20.0F;
            } else {
                this.w = 24 + font.width(t) + 6;
                this.h = 20.0F;
                this.box(g, this.w, this.h);
                Matrix3x2fStack pose = g.pose();
                pose.pushMatrix();
                pose.translate(2.0F, 2.0F);
                g.item(s, 0, 0);
                pose.popMatrix();
                R2D.text(g, font, t, 22.0F, 6.0F, this.text(), Theme.shadow());
            }
        }
    }

    public static class Hunger extends HudModule {
        public final BoolSetting saturation = this.add(new BoolSetting("Sättigung zeigen", true));
        private String text = "20";

        public Hunger() {
            super("Hunger", "Hunger und die versteckte Sättigung.", 0.005F, 0.53F);
        }

        @Override
        public void onTick(Minecraft mc) {
            if (mc.player != null) {
                FoodData food = mc.player.getFoodData();
                this.text = food.getFoodLevel() + (this.saturation.on() ? String.format(Locale.ROOT, "  ·  %.1f", food.getSaturationLevel()) : "");
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, Lang.t("Hunger"), this.text);
        }
    }

    public static class LightLevel extends HudModule {
        public final BoolSetting sky = this.add(new BoolSetting("Himmelslicht zeigen", false));
        private String text = "15";

        public LightLevel() {
            super("Lichtlevel", "Das Licht, in dem du stehst (unter 1 können Monster erscheinen).", 0.005F, 0.81F);
        }

        @Override
        public void onTick(Minecraft mc) {
            if (mc.level != null && mc.player != null) {
                BlockPos pos = mc.player.blockPosition();
                int block = mc.level.getBrightness(LightLayer.BLOCK, pos);
                this.text = this.sky.on() ? block + "  ·  " + mc.level.getBrightness(LightLayer.SKY, pos) : String.valueOf(block);
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, Lang.t("Licht"), this.text);
        }
    }

    public static class Memory extends HudModule {
        public final ModeSetting format = this.add(new ModeSetting("Format", "Prozent", "Prozent", "MB"));
        private String text = "0%";

        public Memory() {
            super("Arbeitsspeicher", "Wie viel Arbeitsspeicher Minecraft gerade belegt.", 0.005F, 0.69F);
        }

        @Override
        public void onTick(Minecraft mc) {
            Runtime rt = Runtime.getRuntime();
            long used = rt.totalMemory() - rt.freeMemory();
            long max = rt.maxMemory();
            this.text = this.format.is("MB") ? (used >> 20) + " / " + (max >> 20) + " MB" : used * 100L / max + "%";
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "RAM", this.text);
        }
    }

    public static class NearbyPlayers extends HudModule {
        public final NumberSetting count = this.add(new NumberSetting("Anzahl", 4.0, 1.0, 10.0, 1.0, ""));
        public final NumberSetting maxDistance = this.add(new NumberSetting("Höchstens", 64.0, 8.0, 256.0, 8.0, " m"));
        public final BoolSetting health = this.add(new BoolSetting("Leben zeigen", true));
        private final List<String[]> rows = new ArrayList<>();

        public NearbyPlayers() {
            super("Spieler in der Nähe", "Die nächsten Spieler mit Abstand und Leben.", 0.005F, 0.73F);
        }

        @Override
        public void onTick(Minecraft mc) {
            this.rows.clear();
            if (mc.player != null && mc.level != null) {
                LocalPlayer me = mc.player;
                List<Player> near = new ArrayList<>();

                for (Player other : mc.level.players()) {
                    if (other != me && !other.isSpectator() && me.distanceTo(other) <= this.maxDistance.f()) {
                        near.add(other);
                    }
                }

                near.sort(Comparator.comparingDouble(me::distanceTo));

                for (int i = 0; i < Math.min(near.size(), this.count.i()); i++) {
                    Player o = near.get(i);
                    this.rows
                        .add(
                            new String[]{
                                o.getName().getString(),
                                String.format(Locale.ROOT, "%.0fm", me.distanceTo(o)),
                                String.format(Locale.ROOT, "%.0f", o.getHealth() + o.getAbsorptionAmount())
                            }
                        );
                }
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            List<String[]> list = this.rows;
            if (list.isEmpty()) {
                if (!editor) {
                    this.w = 80.0F;
                    this.h = 13.0F;
                    return;
                }

                list = List.of(new String[]{"Steve", "6m", "20"}, new String[]{"Alex", "14m", "11"});
            }

            float nameW = 0.0F;

            for (String[] r : list) {
                nameW = Math.max(nameW, (float)font.width(r[0]));
            }

            float pad = 4.5F;
            this.w = pad * 2.0F + nameW + 30.0F + (this.health.on() ? 24 : 0);
            this.h = 4 + list.size() * 10;
            this.box(g, this.w, this.h);

            for (int i = 0; i < list.size(); i++) {
                String[] r = list.get(i);
                float y = 3 + i * 10;
                R2D.text(g, font, r[0], pad, y, this.text(), Theme.shadow());
                R2D.text(g, font, r[1], pad + nameW + 6.0F, y, this.label(), Theme.shadow());
                if (this.health.on()) {
                    R2D.text(g, font, "♥" + r[2], this.w - pad - font.width("♥" + r[2]), y, this.text(), Theme.shadow());
                }
            }
        }
    }

    public static class PlayTime extends HudModule {
        public final ModeSetting mode = this.add(new ModeSetting("Zeigt", "Spielzeit", "Spielzeit", "Stoppuhr"));
        private long joined = -1L;
        private long watchStart = -1L;
        private String text = "0:00";

        public PlayTime() {
            super("Spielzeit", "Wie lange du schon spielst - oder eine Stoppuhr, die beim Einschalten startet.", 0.005F, 0.57F);
        }

        @Override
        protected void onEnable() {
            this.watchStart = System.currentTimeMillis();
        }

        @Override
        public void onTick(Minecraft mc) {
            if (mc.player == null) {
                this.joined = -1L;
            } else {
                long now = System.currentTimeMillis();
                if (this.joined < 0L) {
                    this.joined = now;
                }

                long from = this.mode.is("Stoppuhr") ? (this.watchStart < 0L ? now : this.watchStart) : this.joined;
                long s = (now - from) / 1000L;
                this.text = s >= 3600L
                    ? String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600L, s / 60L % 60L, s % 60L)
                    : String.format(Locale.ROOT, "%d:%02d", s / 60L, s % 60L);
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, Lang.t(this.mode.get()), this.text);
        }
    }

    public static class Tps extends HudModule {
        private long lastGameTime = -1L;
        private long lastNanos;
        private double tps = 20.0;
        private String text = "20.0";

        public Tps() {
            super("TPS", "Wie schnell der Server läuft (20 = normal) - geschätzt aus der Spielzeit.", 0.005F, 0.77F);
        }

        @Override
        public void onTick(Minecraft mc) {
            if (mc.level == null) {
                this.lastGameTime = -1L;
            } else {
                long gt = mc.level.getGameTime();
                long now = System.nanoTime();
                if (this.lastGameTime < 0L) {
                    this.lastGameTime = gt;
                    this.lastNanos = now;
                } else {
                    double secs = (now - this.lastNanos) / 1.0E9;
                    if (secs >= 3.0 && gt != this.lastGameTime) {
                        double measured = Math.min(20.0, (gt - this.lastGameTime) / secs);
                        this.tps = this.tps * 0.5 + measured * 0.5;
                        this.text = String.format(Locale.ROOT, "%.1f", this.tps);
                        this.lastGameTime = gt;
                        this.lastNanos = now;
                    }
                }
            }
        }

        @Override
        public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
            this.labelValue(g, font, "TPS", this.text);
        }
    }
}
