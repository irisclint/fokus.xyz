package xyz.fokus.module.impl.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import xyz.fokus.Stats;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

/**
 * The crosshair, drawn in real screen pixels: its centre and every arm sit on whole pixels, so
 * all four arms are equally long and equally thick at any GUI scale. Outlines are drawn for the
 * whole shape first and the fill on top, so joining parts never get a dark seam.
 */
public class CrosshairModule extends Module {
    public final ModeSetting style = add(new ModeSetting("Stil", "Kreuz", "Kreuz", "Plus + Punkt", "Punkt", "Kreis",
            "Kreis + Punkt", "Kreis + Kreuz", "X", "Quadrat", "Raute", "Pfeil", "Klammern", "T-Form"));
    public final ColorSetting color = add(new ColorSetting("Farbe", 0xFFFFFFFF));
    public final NumberSetting size = add(new NumberSetting("Länge", 4.0, 1.0, 12.0, 0.5));
    public final NumberSetting gap = add(new NumberSetting("Lücke", 2.0, 0.0, 8.0, 0.5));
    public final NumberSetting thickness = add(new NumberSetting("Dicke", 1.0, 0.5, 4.0, 0.5));
    public final NumberSetting opacity = add(new NumberSetting("Deckkraft", 100.0, 10.0, 100.0, 5.0, "%"));
    public final BoolSetting outline = add(new BoolSetting("Umrandung", true));
    public final BoolSetting centerDot = add(new BoolSetting("Mittelpunkt", false));
    public final BoolSetting dynamic = add(new BoolSetting("Dynamisch (Laufen/Schlagen)", true));
    public final BoolSetting targetColor = add(new BoolSetting("Farbe bei Ziel", false));
    public final ColorSetting target = add(new ColorSetting("Zielfarbe", 0xFFD6918E));
    public final BoolSetting cooldown = add(new BoolSetting("Angriffs-Cooldown", true));
    public final BoolSetting thirdPerson = add(new BoolSetting("Auch in dritter Person", false));

    private final Anim spread = new Anim(0.0F, 12.0F);
    private final Anim targetBlend = new Anim(0.0F, 14.0F);

    /** One pass of drawing: the outline (wider, dark) or the fill. */
    private int passColor;
    private int grow;

    public CrosshairModule() {
        super("Custom Crosshair", "Eigenes Fadenkreuz: Stil, Farbe, Größe, Lücke, dynamisch.", Category.PVP);
        enabledByDefault();
    }

    public void draw(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(thirdPerson.on() || mc.options.getCameraType().isFirstPerson())) {
            return;
        }
        // Like the game's own: a spectator sees the crosshair only when looking at something.
        if (mc.player.isSpectator() && mc.crosshairPickEntity == null) {
            return;
        }
        boolean moving = Stats.speed > 0.5;
        boolean hitting = System.currentTimeMillis() - Stats.lastHitAt < 160L;
        spread.to(dynamic.on() ? (moving ? 1.5F : 0.0F) + (hitting ? 2.5F : 0.0F) : 0.0F);
        targetBlend.to(targetColor.on() && mc.crosshairPickEntity != null ? 1.0F : 0.0F);

        float k = (float) mc.getWindow().getGuiScale();
        // Everything below in screen pixels.
        int len = Math.max(1, Math.round(size.f() * k));
        int gp = Math.round((gap.f() + spread.get()) * k);
        int th = Math.max(1, Math.round(thickness.f() * k));
        int edge = Math.max(1, Math.round(k * 0.5F));
        int width = Math.round(g.guiWidth() * k);
        int height = Math.round(g.guiHeight() * k);
        // The centre square [ox, ox + th) x [oy, oy + th): the same distance to every edge.
        int ox = width / 2 - th / 2;
        int oy = height / 2 - th / 2;

        float alpha = opacity.f() / 100.0F;
        int base = Colors.lerp(color.color(0.0F), target.color(0.0F), targetBlend.get());
        int fill = Colors.alpha(base, alpha);
        int dark = Colors.alpha(0xFF000000, alpha * 0.8F);

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1.0F / k, 1.0F / k);
        for (int pass = outline.on() ? 0 : 1; pass < 2; pass++) {
            passColor = pass == 0 ? dark : fill;
            grow = pass == 0 ? edge : 0;
            shape(g, ox, oy, th, len, gp);
            if (centerDot.on() && !style.get().contains("Punkt")) {
                dot(g, ox, oy, th, th / 2.0F);
            }
        }
        pose.popMatrix();

        if (cooldown.on()) {
            float charge = mc.player.getAttackStrengthScale(0.0F);
            if (charge < 1.0F) {
                float bw = 16.0F;
                float by = g.guiHeight() / 2.0F + (gp + len) / k + 5.0F;
                float bx = g.guiWidth() / 2.0F - bw / 2.0F;
                R2D.round(g, bx - 0.5F, by - 0.5F, bw + 1.0F, 3.0F, 1.5F, 0x90000000);
                R2D.roundGradient(g, bx, by, bw * charge, 2.0F, 1.0F, Theme.accent(0.0F), Theme.accent2(0.3F));
            }
        }
    }

    private void shape(GuiGraphicsExtractor g, int ox, int oy, int th, int len, int gp) {
        float mx = ox + th / 2.0F;
        float my = oy + th / 2.0F;
        switch (style.get()) {
            case "Plus + Punkt" -> {
                cross(g, ox, oy, th, len, gp, true);
                dot(g, ox, oy, th, th / 2.0F);
            }
            case "Punkt" -> dot(g, ox, oy, th, Math.max(th / 2.0F, len / 2.0F));
            case "Kreis" -> ring(g, mx, my, len / 2.0F + gp, th);
            case "Kreis + Punkt" -> {
                ring(g, mx, my, len / 2.0F + gp, th);
                dot(g, ox, oy, th, th / 2.0F);
            }
            case "Kreis + Kreuz" -> {
                ring(g, mx, my, len + gp, th);
                cross(g, ox, oy, th, Math.max(1, Math.round(len * 0.6F)), gp, true);
            }
            case "X" -> {
                float d = (gp + th / 2.0F) * 0.7071F;
                float e = (gp + th / 2.0F + len) * 0.7071F;
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sy = -1; sy <= 1; sy += 2) {
                        line(g, mx + sx * d, my + sy * d, mx + sx * e, my + sy * e, th);
                    }
                }
            }
            case "Pfeil" -> {
                float tip = my + gp * 0.3F;
                line(g, mx - len, tip + len * 0.7F, mx, tip, th);
                line(g, mx + len, tip + len * 0.7F, mx, tip, th);
            }
            case "Klammern" -> {
                int r = gp + len / 2 + th;
                int l = Math.max(th, Math.round(len * 0.6F));
                int x0 = ox - r;
                int x1 = ox + th + r;
                int y0 = oy - r;
                int y1 = oy + th + r;
                rect(g, x0 - th, y0, x0, y1);
                rect(g, x0, y0, x0 + l, y0 + th);
                rect(g, x0, y1 - th, x0 + l, y1);
                rect(g, x1, y0, x1 + th, y1);
                rect(g, x1 - l, y0, x1, y0 + th);
                rect(g, x1 - l, y1 - th, x1, y1);
            }
            case "Quadrat" -> {
                int r = gp + len / 2;
                int x0 = ox - r;
                int x1 = ox + th + r;
                int y0 = oy - r;
                int y1 = oy + th + r;
                rect(g, x0, y0, x1, y0 + th);
                rect(g, x0, y1 - th, x1, y1);
                rect(g, x0, y0 + th, x0 + th, y1 - th);
                rect(g, x1 - th, y0 + th, x1, y1 - th);
            }
            case "Raute" -> {
                float r = gp + len * 0.7F + th / 2.0F;
                line(g, mx, my - r, mx + r, my, th);
                line(g, mx + r, my, mx, my + r, th);
                line(g, mx, my + r, mx - r, my, th);
                line(g, mx - r, my, mx, my - r, th);
            }
            case "T-Form" -> cross(g, ox, oy, th, len, gp, false);
            default -> cross(g, ox, oy, th, len, gp, true);
        }
    }

    /** Four arms around the centre square, each {@code gp} away from it. */
    private void cross(GuiGraphicsExtractor g, int ox, int oy, int th, int len, int gp, boolean top) {
        if (top) {
            rect(g, ox, oy - gp - len, ox + th, oy - gp);
        }
        rect(g, ox, oy + th + gp, ox + th, oy + th + gp + len);
        rect(g, ox - gp - len, oy, ox - gp, oy + th);
        rect(g, ox + th + gp, oy, ox + th + gp + len, oy + th);
        if (gp == 0) {
            rect(g, ox, oy, ox + th, oy + th);
        }
    }

    private void rect(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2) {
        g.fill(x1 - grow, y1 - grow, x2 + grow, y2 + grow, passColor);
    }

    private void dot(GuiGraphicsExtractor g, int ox, int oy, int th, float r) {
        float rr = Math.max(1.0F, r) + grow;
        R2D.circle(g, ox + th / 2.0F, oy + th / 2.0F, rr, passColor);
    }

    private void line(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, int th) {
        R2D.line(g, x1, y1, x2, y2, th + grow * 2, passColor);
    }

    /** A closed ring of short line pieces - even and unbroken at any size. */
    private void ring(GuiGraphicsExtractor g, float cx, float cy, float r, int th) {
        int steps = Math.max(24, Math.round(r * 1.5F));
        for (int i = 0; i < steps; i++) {
            double a0 = Math.PI * 2 * i / steps;
            double a1 = Math.PI * 2 * (i + 1) / steps;
            line(g, cx + (float) Math.cos(a0) * r, cy + (float) Math.sin(a0) * r,
                    cx + (float) Math.cos(a1) * r, cy + (float) Math.sin(a1) * r, th);
        }
    }
}
