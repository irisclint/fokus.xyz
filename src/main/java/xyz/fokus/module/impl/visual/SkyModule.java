package xyz.fokus.module.impl.visual;

import net.minecraft.world.level.MoonPhase;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;

public class SkyModule extends Module {
    public final ColorSetting sky = this.add(new ColorSetting("Himmelsfarbe", -14803422));
    public final BoolSetting dayNight = this.add(new BoolSetting("Tag & Nacht folgen", true));
    public final BoolSetting fog = this.add(new BoolSetting("Horizont & Nebel einfärben", true));
    public final ColorSetting fogColor = this.add(new ColorSetting("Horizontfarbe", -12961216)).visibleWhen(() -> this.fog.on());
    public final BoolSetting sunset = this.add(new BoolSetting("Sonnenuntergang einfärben", false));
    public final ColorSetting sunsetColor = this.add(new ColorSetting("Sonnenuntergangsfarbe", -3158065)).visibleWhen(() -> this.sunset.on());
    public final NumberSetting stars = this.add(new NumberSetting("Sterne", 0.0, 0.0, 1.0, 0.05));
    public final BoolSetting clouds = this.add(new BoolSetting("Wolken einfärben", false));
    public final ColorSetting cloudTint = this.add(new ColorSetting("Wolkenfarbe", -4671296)).visibleWhen(() -> this.clouds.on());
    public final ModeSetting moon = this.add(
        new ModeSetting("Mond", "Wie im Spiel", "Wie im Spiel", "Immer Vollmond", "Immer Halbmond", "Immer Sichel", "Immer Neumond")
    );

    public SkyModule() {
        super("Himmel", "Himmel, Horizont und Sterne in deinen Farben - Lyrics stehen davor.", Category.VISUALS);
    }

    public static int cloudColor(int game) {
        SkyModule m = Modules.get(SkyModule.class);
        if (m != null && m.isEnabled() && m.clouds.on()) {
            int c = m.cloudTint.color(0.0F);
            int r = (c >> 16 & 0xFF) * (game >> 16 & 0xFF) / 255;
            int g = (c >> 8 & 0xFF) * (game >> 8 & 0xFF) / 255;
            int b = (c & 0xFF) * (game & 0xFF) / 255;
            return game & 0xFF000000 | r << 16 | g << 8 | b;
        } else {
            return game;
        }
    }

    public MoonPhase moonPhase() {
        String var1 = this.moon.get();

        return switch (var1) {
            case "Immer Vollmond" -> MoonPhase.FULL_MOON;
            case "Immer Halbmond" -> MoonPhase.FIRST_QUARTER;
            case "Immer Sichel" -> MoonPhase.WAXING_CRESCENT;
            case "Immer Neumond" -> MoonPhase.NEW_MOON;
            default -> null;
        };
    }
}
