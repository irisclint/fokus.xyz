package xyz.fokus.module.impl.visual;

import net.minecraft.client.Minecraft;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.TintableOverlay;

/** The flash a hit entity shows, in any colour instead of red. */
public class HitColorModule extends Module {
    /** The game's own: red at 70%. */
    private static final int VANILLA = 0xB2FF0000;

    public final ColorSetting color = add(new ColorSetting("Farbe", 0xFFFFFFFF));
    public final NumberSetting opacity = add(new NumberSetting("Deckkraft", 60, 0, 100, 5, "%"));

    /** The colour now in the texture, so it is only uploaded when it changes. */
    private static int applied = VANILLA;

    public HitColorModule() {
        super("Treffer-Farbe", "Getroffene Spieler und Mobs leuchten in deiner Farbe statt rot.", Category.VISUALS);
    }

    private int wanted() {
        int alpha = Math.round(opacity.f() / 100.0F * 255.0F);
        return alpha << 24 | color.color() & 0xFFFFFF;
    }

    @Override
    public void onTick(Minecraft mc) {
        apply(mc, wanted());
    }

    @Override
    protected void onDisable() {
        apply(Minecraft.getInstance(), VANILLA);
    }

    private static void apply(Minecraft mc, int argb) {
        if (argb == applied || mc == null || mc.gameRenderer == null) {
            return;
        }
        ((TintableOverlay) mc.gameRenderer.overlayTexture()).fokus$tint(argb);
        applied = argb;
    }
}
