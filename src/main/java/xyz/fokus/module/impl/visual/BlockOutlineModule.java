package xyz.fokus.module.impl.visual;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;

/** The line around the block you look at, in your colour and width. */
public class BlockOutlineModule extends Module {
    public final ColorSetting color = add(new ColorSetting("Farbe", 0xFFFFFFFF));
    public final NumberSetting opacity = add(new NumberSetting("Deckkraft", 80, 10, 100, 5, "%"));
    public final NumberSetting width = add(new NumberSetting("Linienbreite", 1.5, 0.5, 4.0, 0.1, "x"));

    public BlockOutlineModule() {
        super("Block-Umrandung", "Die Linie um den anvisierten Block in eigener Farbe und Breite.", Category.VISUALS);
    }

    private static BlockOutlineModule on() {
        BlockOutlineModule m = Modules.get(BlockOutlineModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    /**
     * The game draws a block's outline in up to two passes: a wide black one under it when "high
     * contrast block outline" is on (width 7), then the line itself. Only the line is changed.
     */
    private static boolean isContrastPass(int color, float lineWidth) {
        return color == 0xFF000000 && lineWidth >= 6.9F;
    }

    public static int color(int vanilla, float lineWidth) {
        BlockOutlineModule m = on();
        if (m == null || isContrastPass(vanilla, lineWidth)) {
            return vanilla;
        }
        return Colors.alpha(m.color.color(0.0F), m.opacity.f() / 100.0F);
    }

    public static float width(int color, float vanilla) {
        BlockOutlineModule m = on();
        if (m == null || isContrastPass(color, vanilla)) {
            return vanilla;
        }
        return vanilla * m.width.f();
    }
}
