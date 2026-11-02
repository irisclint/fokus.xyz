package xyz.fokus.module.impl.visual;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ActionSetting;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Recolor;

public class TotemModule extends Module {
    public final ModeSetting texture = this.add(new ModeSetting("Totem-Textur", "Umfärben", Recolor.STYLES));
    public final ColorSetting dark = this.add(new ColorSetting("Totem dunkel", -14013910)).visibleWhen(() -> Recolor.usesColors(this.texture.get()));
    public final ColorSetting light = this.add(new ColorSetting("Totem hell", -1)).visibleWhen(() -> Recolor.usesColors(this.texture.get()));
    public final BoolSetting crystalItem = this.add(new BoolSetting("Kristall-Item in Kristallfarbe", true));
    public final ActionSetting apply = this.add(new ActionSetting("Texturen übernehmen", this::applyTextures));
    public final NumberSetting popSize = this.add(new NumberSetting("Pop-Animation Größe", 0.7, 0.0, 1.5, 0.05, "x"));
    public final ModeSetting particles = this.add(new ModeSetting("Pop-Partikel", "Eigene Farben", "Vanilla", "Eigene Farben"));
    public final ColorSetting particle1 = this.add(new ColorSetting("Partikelfarbe 1", -1)).visibleWhen(() -> this.particles.is("Eigene Farben"));
    public final ColorSetting particle2 = this.add(new ColorSetting("Partikelfarbe 2", -7697782)).visibleWhen(() -> this.particles.is("Eigene Farben"));

    public TotemModule() {
        super("Totems", "Eigene Totem-Texturen und Farben, kleinere Pop-Animation, eigene Partikel.", Category.VISUALS);
        this.enabledByDefault();
    }

    public void applyTextures() {
        TexturesModule.applyAll();
    }

    public float popScale() {
        return this.isEnabled() ? this.popSize.f() : 1.0F;
    }

    public boolean customParticles() {
        return this.isEnabled() && this.particles.is("Eigene Farben");
    }
}
