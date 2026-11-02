package xyz.fokus.module.impl.pvp;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class HitmarkerModule extends Module {
    public final ModeSetting style = this.add(new ModeSetting("Stil", "Kreuz", "Kreuz", "Ecken", "Ring"));
    public final NumberSetting duration = this.add(new NumberSetting("Dauer", 300.0, 100.0, 1000.0, 25.0, " ms"));
    public final NumberSetting size = this.add(new NumberSetting("Größe", 6.0, 3.0, 14.0, 0.5, ""));
    public final ColorSetting color = this.add(new ColorSetting("Farbe", -1));
    private static long hitAt;

    public HitmarkerModule() {
        super("Hitmarker", "Ein kurzes Zeichen ums Fadenkreuz, wenn du triffst.", Category.PVP);
        this.enabledByDefault();
    }

    public static void onHit() {
        hitAt = System.currentTimeMillis();
    }

    public void draw(GuiGraphicsExtractor g) {
        float t = (float)(System.currentTimeMillis() - hitAt) / this.duration.f();
        if (!(t >= 1.0F) && !(t < 0.0F)) {
            float cx = g.guiWidth() / 2.0F;
            float cy = g.guiHeight() / 2.0F;
            float spread = 1.0F + (1.0F - (1.0F - t) * (1.0F - t)) * 0.6F;
            int c = Colors.alpha(this.color.color(0.0F), 1.0F - t * t);
            float s = this.size.f() * spread;
            float gap = s * 0.45F;
            String var9 = this.style.get();
            switch (var9) {
                case "Ecken":
                    float l = s * 0.45F;

                    for (int sx = -1; sx <= 1; sx += 2) {
                        for (int sy = -1; sy <= 1; sy += 2) {
                            R2D.line(g, cx + sx * s, cy + sy * s, cx + sx * (s - l), cy + sy * s, 1.0F, c);
                            R2D.line(g, cx + sx * s, cy + sy * s, cx + sx * s, cy + sy * (s - l), 1.0F, c);
                        }
                    }
                    break;
                case "Ring":
                    int n = 28;

                    for (int i = 0; i < n; i++) {
                        double a0 = (Math.PI * 2) * i / n;
                        double a1 = (Math.PI * 2) * (i + 1) / n;
                        R2D.line(
                            g, cx + (float)Math.cos(a0) * s, cy + (float)Math.sin(a0) * s, cx + (float)Math.cos(a1) * s, cy + (float)Math.sin(a1) * s, 1.0F, c
                        );
                    }
                    break;
                default:
                    for (int sx = -1; sx <= 1; sx += 2) {
                        for (int sy = -1; sy <= 1; sy += 2) {
                            R2D.line(g, cx + sx * gap, cy + sy * gap, cx + sx * s, cy + sy * s, 1.0F, c);
                        }
                    }
            }
        }
    }
}
