package xyz.fokus.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.module.Category;
import xyz.fokus.util.R2D;

final class Icons {
    private Icons() {
    }

    static void category(GuiGraphicsExtractor g, Category c, float x, float y, int color) {
        switch (c) {
            case PVP:
                R2D.line(g, x + 2.5F, y + 7.5F, x + 9.0F, y + 1.0F, 1.1F, color);
                R2D.line(g, x + 1.2F, y + 5.2F, x + 4.8F, y + 8.8F, 1.1F, color);
                R2D.line(g, x + 0.8F, y + 9.2F, x + 2.4F, y + 7.6F, 1.3F, color);
                break;
            case HUD:
                R2D.round(g, x + 0.5F, y + 0.5F, 4.0F, 4.0F, 1.0F, color);
                R2D.round(g, x + 5.5F, y + 0.5F, 4.0F, 4.0F, 1.0F, color);
                R2D.round(g, x + 0.5F, y + 5.5F, 4.0F, 4.0F, 1.0F, color);
                R2D.round(g, x + 5.5F, y + 5.5F, 4.0F, 4.0F, 1.0F, color);
                break;
            case ANIMATIONS:
                R2D.line(g, x + 0.5F, y + 2.5F, x + 4.5F, y + 2.5F, 1.0F, color);
                R2D.line(g, x + 0.0F, y + 5.0F, x + 3.5F, y + 5.0F, 1.0F, color);
                R2D.line(g, x + 0.5F, y + 7.5F, x + 4.5F, y + 7.5F, 1.0F, color);
                R2D.circle(g, x + 7.4F, y + 5.0F, 2.4F, color);
                break;
            case VISUALS:
                ring(g, x + 5.0F, y + 5.0F, 4.3F, 1.0F, color);
                R2D.circle(g, x + 5.0F, y + 5.0F, 1.7F, color);
                break;
            case SETTINGS:
                R2D.line(g, x + 0.5F, y + 1.8F, x + 9.5F, y + 1.8F, 0.9F, color);
                R2D.line(g, x + 0.5F, y + 5.0F, x + 9.5F, y + 5.0F, 0.9F, color);
                R2D.line(g, x + 0.5F, y + 8.2F, x + 9.5F, y + 8.2F, 0.9F, color);
                R2D.circle(g, x + 3.0F, y + 1.8F, 1.5F, color);
                R2D.circle(g, x + 7.0F, y + 5.0F, 1.5F, color);
                R2D.circle(g, x + 4.5F, y + 8.2F, 1.5F, color);
        }
    }

    static void search(GuiGraphicsExtractor g, float x, float y, int color) {
        ring(g, x + 4.0F, y + 4.0F, 3.2F, 1.0F, color);
        R2D.line(g, x + 6.4F, y + 6.4F, x + 9.0F, y + 9.0F, 1.1F, color);
    }

    static void ring(GuiGraphicsExtractor g, float cx, float cy, float r, float t, int color) {
        int steps = Math.max(20, (int)(r * 10.0F));

        for (int i = 0; i < steps; i++) {
            double a0 = i * Math.PI * 2.0 / steps;
            double a1 = (i + 1) * Math.PI * 2.0 / steps;
            R2D.line(g, cx + (float)Math.cos(a0) * r, cy + (float)Math.sin(a0) * r, cx + (float)Math.cos(a1) * r, cy + (float)Math.sin(a1) * r, t, color);
        }
    }
}
