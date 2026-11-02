package xyz.fokus.module.impl.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.CrystalTextures;

public class CrystalModule extends Module {
    public final ModeSetting texture = this.add(new ModeSetting("Textur", "Clean", "Vanilla", "Clean", "Wire", "Gem", "Neon", "Solid"));
    public final ColorSetting core = this.add(new ColorSetting("Kernfarbe", -1)).visibleWhen(() -> !this.texture.is("Vanilla"));
    public final ColorSetting frame = this.add(new ColorSetting("Rahmenfarbe", -1)).visibleWhen(() -> !this.texture.is("Vanilla"));
    public final ColorSetting base = this.add(new ColorSetting("Sockelfarbe", -15066598)).visibleWhen(() -> !this.texture.is("Vanilla") && !this.hideBaseOn());
    public final NumberSetting scale = this.add(new NumberSetting("Größe", 0.8, 0.2, 1.5, 0.05, "x"));
    public final NumberSetting speed = this.add(new NumberSetting("Dreh- und Schwebetempo", 1.0, 0.0, 3.0, 0.1, "x"));
    public final NumberSetting bounce = this.add(new NumberSetting("Hüpfhöhe", 1.0, 0.0, 1.5, 0.05, "x"));
    public final BoolSetting hideBase = this.add(new BoolSetting("Sockel ausblenden", true));

    public CrystalModule() {
        super("End-Kristalle", "Eigene Texturen und Farben, Größe, Drehung und Hüpfen - klare Sicht im Crystal-PvP.", Category.VISUALS);
        this.enabledByDefault();
    }

    private boolean hideBaseOn() {
        return this.hideBase != null && this.hideBase.on();
    }

    @Override
    public void onTick(Minecraft mc) {
        if (!this.texture.is("Vanilla")) {
            CrystalTextures.update(this.texture.get(), this.core.color(0.0F), this.frame.color(0.3F), this.base.color(0.6F));
        }
    }

    public Identifier textureId() {
        return this.texture.is("Vanilla") ? null : CrystalTextures.current();
    }
}
