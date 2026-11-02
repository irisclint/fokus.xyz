package xyz.fokus.module.impl.visual;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;

public class ClearWeatherModule extends Module {
    public final BoolSetting rain = this.add(new BoolSetting("Regen & Schnee aus", true));
    public final BoolSetting thunder = this.add(new BoolSetting("Gewitter aus", true));

    public ClearWeatherModule() {
        super("Klares Wetter", "Kein Regen, Schnee oder Gewitter auf deinem Bildschirm - nur bei dir.", Category.VISUALS);
    }

    public static boolean noRain() {
        ClearWeatherModule m = Modules.get(ClearWeatherModule.class);
        return m != null && m.isEnabled() && m.rain.on();
    }

    public static boolean noThunder() {
        ClearWeatherModule m = Modules.get(ClearWeatherModule.class);
        return m != null && m.isEnabled() && m.thunder.on();
    }
}
