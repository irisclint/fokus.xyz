package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import xyz.fokus.Lang;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Pointer;

/** One marked place - a base, a chest, a portal - shown as direction and distance; kept across restarts. */
public class WaypointModule extends HudModule {
    private static final String[] DIMENSIONS = {"overworld", "the_nether", "the_end"};
    private static final double FAR = 30_000_000.0;

    public final KeySetting key = this.add(new KeySetting("Wegpunkt-Taste", Compat.key("KEY_B")));
    public final BoolSetting showCoords = this.add(new BoolSetting("Koordinaten zeigen", false));
    // The point itself, saved with the settings but never shown as one.
    private final BoolSetting set = this.add(new BoolSetting("gesetzt", false)).visibleWhen(() -> false);
    private final NumberSetting px = this.add(new NumberSetting("x", 0.0, -FAR, FAR, 1.0, "")).visibleWhen(() -> false);
    private final NumberSetting py = this.add(new NumberSetting("y", 0.0, -FAR, FAR, 1.0, "")).visibleWhen(() -> false);
    private final NumberSetting pz = this.add(new NumberSetting("z", 0.0, -FAR, FAR, 1.0, "")).visibleWhen(() -> false);
    private final NumberSetting dim = this.add(new NumberSetting("dim", 0.0, 0.0, 2.0, 1.0, "")).visibleWhen(() -> false);

    public WaypointModule() {
        super("Wegpunkt", "Markiert einen Ort per Taste und zeigt Richtung und Entfernung dorthin.", 0.005F, 0.52F);
    }

    private static int dimensionIndex(Minecraft mc) {
        String path = mc.level.dimension().identifier().getPath();
        for (int i = 0; i < DIMENSIONS.length; i++) {
            if (DIMENSIONS[i].equals(path)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (this.key.pressed() && p != null && mc.level != null && Compat.currentScreen() == null) {
            this.toggle(mc, p);
        }
    }

    /** Marks where the player stands, or clears the mark that is there. */
    private void toggle(Minecraft mc, LocalPlayer p) {
        if (this.set.on()) {
            this.set.set(false);
            Compat.say(Component.literal("⚑ " + Lang.t("Wegpunkt gelöscht")).withColor(0x8A8A8A));
        } else {
            this.px.set((double) Mth.floor(p.getX()));
            this.py.set((double) Mth.floor(p.getY()));
            this.pz.set((double) Mth.floor(p.getZ()));
            this.dim.set((double) dimensionIndex(mc));
            this.set.set(true);
            Compat.say(Component.literal("⚑ " + Lang.t("Wegpunkt gesetzt") + " ").withColor(0x55E07A)
                    .append(Component.literal(this.x() + " " + this.y() + " " + this.z()).withColor(0xFFFFFF)));
        }
        xyz.fokus.config.ConfigIO.save();
    }

    private int x() {
        return (int) Math.round(this.px.get());
    }

    private int y() {
        return (int) Math.round(this.py.get());
    }

    private int z() {
        return (int) Math.round(this.pz.get());
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (editor && (!this.set.on() || p == null)) {
            this.labelValue(g, font, Lang.t("Wegpunkt"), "↑ 42 m");
            return;
        }
        if (!this.set.on() || p == null || mc.level == null) {
            this.w = 0.0F;
            this.h = 0.0F;
            return;
        }
        int d = (int) Math.round(this.dim.get());
        if (d != dimensionIndex(mc)) {
            this.labelValue(g, font, Lang.t("Wegpunkt"), Pointer.dimensionName(DIMENSIONS[Mth.clamp(d, 0, 2)]));
            return;
        }
        String value = Pointer.toward(p, this.x(), this.z());
        if (this.showCoords.on()) {
            value += "  " + this.x() + " " + this.y() + " " + this.z();
        }
        this.labelValue(g, font, Lang.t("Wegpunkt"), value);
    }
}
