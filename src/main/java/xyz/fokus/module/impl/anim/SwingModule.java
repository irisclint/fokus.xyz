package xyz.fokus.module.impl.anim;

import xyz.fokus.compat.Compat;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;

public class SwingModule extends Module {
    public final ModeSetting style = this.add(
        new ModeSetting("Stil", "Glatt", "Vanilla", "Glatt", "Klassisch 1.7", "Spin", "Stich", "Slide", "Punch", "Exhibition", "Swank", "Wackeln", "Tippen")
    );
    public final NumberSetting speed = this.add(new NumberSetting("Tempo", 1.0, 0.3, 2.5, 0.05, "x"));
    public final NumberSetting intensity = this.add(new NumberSetting("Stärke", 1.0, 0.2, 1.6, 0.05, "x"));

    public SwingModule() {
        super("Schwung-Animation", "Eigene Schlag-Animationen für Items, mit Tempo und Stärke.", Category.ANIMATIONS);
        if (!Compat.isLoaded("swing-animation-plus")) {
            this.enabledByDefault();
        }
    }

    public int duration(int vanilla) {
        return Math.max(1, (int)Math.round(vanilla / this.speed.get()));
    }
}
