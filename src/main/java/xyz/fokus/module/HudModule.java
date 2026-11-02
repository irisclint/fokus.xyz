package xyz.fokus.module;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.GlassBox;
import xyz.fokus.util.R2D;

public abstract class HudModule extends Module {
    public float px;
    public float py;
    public final float defaultX;
    public final float defaultY;
    public float w = 40.0F;
    public float h = 12.0F;
    public final NumberSetting scale = this.add(new NumberSetting("Größe", 1.0, 0.5, 2.0, 0.05, "x"));
    public final BoolSetting background = this.add(new BoolSetting("Hintergrund", true));
    public final BoolSetting ownColors = this.add(new BoolSetting("Eigene Farben", false));
    public final ColorSetting textColor = this.add(new ColorSetting("Textfarbe", -1)).visibleWhen(() -> this.ownColors.on());
    public final ColorSetting labelColor = this.add(new ColorSetting("Beschriftungsfarbe", -6645094)).visibleWhen(() -> this.ownColors.on());
    public final ColorSetting backColor = this.add(new ColorSetting("Hintergrundfarbe", -16777216)).visibleWhen(() -> this.ownColors.on());
    protected boolean centered;

    protected HudModule(String name, String description, float x, float y) {
        super(name, description, Category.HUD);
        this.px = x;
        this.py = y;
        this.defaultX = x;
        this.defaultY = y;
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        this.px = this.defaultX;
        this.py = this.defaultY;
    }

    public boolean placesItself() {
        return false;
    }

    public void drawPlaced(GuiGraphicsExtractor g, Font font) {
    }

    public abstract void draw(GuiGraphicsExtractor var1, Font var2, boolean var3);

    public float scaledW() {
        return this.w * this.scale.f();
    }

    public float scaledH() {
        return this.h * this.scale.f();
    }

    public boolean isCentered() {
        return this.centered;
    }

    public float screenX(int screenW) {
        float x = this.centered ? this.px * screenW - this.scaledW() / 2.0F : this.px * screenW;
        return Math.max(0.0F, Math.min(screenW - this.scaledW(), x));
    }

    public float screenY(int screenH) {
        return Math.max(0.0F, Math.min(screenH - this.scaledH(), this.py * screenH));
    }

    protected int text() {
        return this.ownColors.on() ? this.textColor.color(0.0F) : Theme.accent(0.0F);
    }

    protected int label() {
        return this.ownColors.on() ? this.labelColor.color(0.3F) : Theme.accent2(0.0F);
    }

    protected int back() {
        return this.ownColors.on() ? this.backColor.color(0.0F) & 16777215 : 0;
    }

    protected void box(GuiGraphicsExtractor g, float width, float height) {
        String style = Theme.hudStyle();
        if (this.background.on() && !style.equals("Ohne")) {
            float r = Theme.hudRadius();
            int fill = style.equals("Solide")
                ? (this.ownColors.on() ? Colors.withAlpha(this.back(), 240) : -267382768)
                : Colors.withAlpha(this.back(), Theme.hudOpacity());
            if (!GlassBox.fill(g, 0.0F, 0.0F, width, height, r, fill)) {
                R2D.round(g, 0.0F, 0.0F, width, height, r, fill);
            }

            if (!style.equals("Solide") && !GlassBox.edge(g, 0.0F, 0.0F, width, height, r, 385875967)) {
                R2D.roundEdge(g, 0.0F, 0.0F, width, height, r, 385875967);
            }

            if (Theme.hudAccentLine()) {
                R2D.rect(g, 2.0F, height - 0.8F, width - 2.0F, height, Theme.accent(0.0F));
            }
        }
    }

    protected void labelValue(GuiGraphicsExtractor g, Font font, String label, String value) {
        float pad = 4.5F;
        float lw = label.isEmpty() ? 0.0F : font.width(label) + 4;
        this.w = pad * 2.0F + lw + font.width(value);
        this.h = 13.0F;
        this.box(g, this.w, this.h);
        if (!label.isEmpty()) {
            R2D.text(g, font, label, pad, 2.5F, this.label(), Theme.shadow());
        }

        R2D.text(g, font, value, pad + lw, 2.5F, this.text(), Theme.shadow());
    }
}
