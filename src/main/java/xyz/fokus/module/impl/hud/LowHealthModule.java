package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Matrix3x2fStack;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

/** A red edge around the screen while your health is low: the lower, the stronger. */
public class LowHealthModule extends HudModule {
    public final NumberSetting threshold = this.add(new NumberSetting("Ab Herzen", 4.0, 1.0, 10.0, 0.5, " ❤"));
    public final NumberSetting strength = this.add(new NumberSetting("Stärke", 70.0, 10.0, 100.0, 5.0, "%"));
    public final BoolSetting pulse = this.add(new BoolSetting("Pulsieren", true));
    public final BoolSetting absorption = this.add(new BoolSetting("Absorption mitzählen", true));
    public final ColorSetting color = this.add(new ColorSetting("Farbe", 0xFFE02424));

    public LowHealthModule() {
        super("Wenig-Leben-Warnung", "Ein roter Rand am Bildschirm, wenn dein Leben knapp wird.", 0.5F, 0.5F);
    }

    @Override
    public boolean placesItself() {
        return true;
    }

    /** 0 at the threshold or above, 1 at no health left. */
    private float danger(LocalPlayer p) {
        if (p == null || p.isCreative() || p.isSpectator() || p.isDeadOrDying()) {
            return 0.0F;
        }
        float hp = p.getHealth() + (this.absorption.on() ? p.getAbsorptionAmount() : 0.0F);
        float limit = this.threshold.f() * 2.0F;
        return hp >= limit ? 0.0F : Math.min(1.0F, 1.0F - hp / limit);
    }

    @Override
    public void drawPlaced(GuiGraphicsExtractor g, Font font) {
        float d = this.danger(Minecraft.getInstance().player);
        if (d <= 0.0F) {
            return;
        }
        // Faster and deeper pulses the closer to death.
        float wave = this.pulse.on()
                ? 0.65F + 0.35F * (float) Math.sin(System.currentTimeMillis() / (420.0 - 220.0 * d))
                : 1.0F;
        float alpha = (0.25F + 0.75F * d) * this.strength.f() / 100.0F * wave;
        int edge = Colors.alpha(this.color.color() | 0xFF000000, alpha);
        int clear = edge & 0x00FFFFFF;
        int w = g.guiWidth();
        int h = g.guiHeight();
        float depth = Math.min(w, h) * (0.12F + 0.10F * d);
        R2D.gradientV(g, 0.0F, 0.0F, w, depth, edge, clear);
        R2D.gradientV(g, 0.0F, h - depth, w, h, clear, edge);
        Matrix3x2fStack pose = g.pose();
        // The sides: the same gradient, turned a quarter.
        pose.pushMatrix();
        pose.translate(0.0F, h);
        pose.rotate((float) (-Math.PI / 2.0));
        R2D.gradientV(g, 0.0F, 0.0F, h, depth, edge, clear);
        pose.popMatrix();
        pose.pushMatrix();
        pose.translate(w, 0.0F);
        pose.rotate((float) (Math.PI / 2.0));
        R2D.gradientV(g, 0.0F, 0.0F, h, depth, edge, clear);
        pose.popMatrix();
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        // Drawn over the whole screen by drawPlaced; nothing to place in the HUD editor.
        this.w = 0.0F;
        this.h = 0.0F;
    }
}
