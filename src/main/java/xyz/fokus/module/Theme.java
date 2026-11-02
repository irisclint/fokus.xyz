package xyz.fokus.module;

import xyz.fokus.module.impl.settings.InterfaceModule;
import xyz.fokus.util.Colors;

public final class Theme {
    private Theme() {
    }

    private static InterfaceModule ui() {
        return Modules.get(InterfaceModule.class);
    }

    public static int accent(float offset) {
        return ui().accent.color(offset);
    }

    public static int accent2(float offset) {
        InterfaceModule ui = ui();
        return ui.accent.rainbow() ? Colors.rainbow(offset + 0.25F, 1.0F) : ui.accent2.color(offset);
    }

    public static boolean shadow() {
        return ui().textShadow.on();
    }

    public static int hudOpacity() {
        return (int)Math.round(ui().hudOpacity.get() * 255.0 / 100.0);
    }

    public static boolean hudAccentLine() {
        return ui().hudAccentLine.on();
    }

    public static String hudStyle() {
        return ui().hudStyle.get();
    }

    public static float hudRadius() {
        return ui().hudRadius.f();
    }

    public static float scrollSpeed() {
        return ui().scrollSpeed.f();
    }

    public static float menuScale() {
        return ui().menuScale.f();
    }

    public static boolean clickSound() {
        return ui().clickSound.on();
    }

    public static boolean blur() {
        return ui().blur.on();
    }

    public static float animTime() {
        return (float)(1.0 / ui().animSpeed.get());
    }
}
