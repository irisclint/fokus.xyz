package xyz.fokus.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public final class R2D {
    private static final int S = 2;
    private static FontDescription face;
    public static final FontDescription SMOOTH = new Resource(Identifier.fromNamespaceAndPath("minecraft", "uniform"));
    private static final Map<String, Component> STYLED = lru(512);
    private static final Map<String, Float> WIDTHS = lru(1024);
    private static final Map<String, String> TRIMMED = lru(256);

    private R2D() {
    }

    public static void rect(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, int color) {
        if (color >>> 24 != 0 && !(x2 <= x1) && !(y2 <= y1)) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            g.fill(Math.round(x1 * 2.0F), Math.round(y1 * 2.0F), Math.round(x2 * 2.0F), Math.round(y2 * 2.0F), color);
            pose.popMatrix();
        }
    }

    public static void round(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int color) {
        if (color >>> 24 != 0 && !(w <= 0.0F) && !(h <= 0.0F)) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            int X = Math.round(x * 2.0F);
            int Y = Math.round(y * 2.0F);
            int W = Math.max(1, Math.round(w * 2.0F));
            int H = Math.max(1, Math.round(h * 2.0F));
            int R = Math.min(Math.round(r * 2.0F), Math.min(W, H) / 2);
            if (R <= 0) {
                g.fill(X, Y, X + W, Y + H, color);
            } else {
                if (H - 2 * R > 0) {
                    g.fill(X, Y + R, X + W, Y + H - R, color);
                }

                boolean smooth = R >= 8;
                int i = 0;

                while (i < R) {
                    float inset = cornerInset(i, R);
                    int full = (int)Math.ceil(inset);
                    int run = 1;

                    while (!smooth && i + run < R && (int)Math.ceil(cornerInset(i + run, R)) == full) {
                        run++;
                    }

                    int top = Y + i;
                    int bot = Y + H - i - run;
                    g.fill(X + full, top, X + W - full, top + run, color);
                    g.fill(X + full, bot, X + W - full, bot + run, color);
                    float cover = full - inset;
                    if (smooth && full > 0 && cover > 0.06F) {
                        int edge = Colors.alpha(color, cover);
                        g.fill(X + full - 1, top, X + full, top + 1, edge);
                        g.fill(X + W - full, top, X + W - full + 1, top + 1, edge);
                        g.fill(X + full - 1, bot, X + full, bot + 1, edge);
                        g.fill(X + W - full, bot, X + W - full + 1, bot + 1, edge);
                    }

                    i += run;
                }
            }

            pose.popMatrix();
        }
    }

    private static float cornerInset(int i, int r) {
        float dy = r - i - 0.5F;
        return r - (float)Math.sqrt(Math.max(0.0F, r * r - dy * dy));
    }

    private static float insetAt(int row, int h, int r) {
        int i = row < r ? row : (row >= h - r ? h - 1 - row : -1);
        if (i < 0) {
            return 0.0F;
        } else {
            float dy = r - i - 0.5F;
            return r - (float)Math.sqrt(Math.max(0.0F, r * r - dy * dy));
        }
    }

    public static void roundEdge(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int color) {
        if (color >>> 24 != 0 && !(w <= 1.0F) && !(h <= 1.0F)) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            int X = Math.round(x * 2.0F);
            int Y = Math.round(y * 2.0F);
            int W = Math.round(w * 2.0F);
            int H = Math.round(h * 2.0F);
            int R = Math.min(Math.round(r * 2.0F), Math.min(W, H) / 2);
            int T = 1;
            int ri = Math.max(0, R - T);
            int band = Math.max(R, T);
            if (H - 2 * band > 0) {
                g.fill(X, Y + band, X + T, Y + H - band, color);
                g.fill(X + W - T, Y + band, X + W, Y + H - band, color);
            }

            for (int j = 0; j < H; j++) {
                if (j >= band && j < H - band) {
                    j = H - band - 1;
                } else {
                    int ox = Math.round(insetAt(j, H, R));
                    if (j >= T && j < H - T) {
                        int ix = Math.round(insetAt(j - T, H - 2 * T, ri));
                        int left = X + T + ix;
                        int right = X + W - T - ix;
                        if (left > X + ox) {
                            g.fill(X + ox, Y + j, left, Y + j + 1, color);
                        }

                        if (right < X + W - ox) {
                            g.fill(right, Y + j, X + W - ox, Y + j + 1, color);
                        }
                    } else {
                        g.fill(X + ox, Y + j, X + W - ox, Y + j + 1, color);
                    }
                }
            }

            pose.popMatrix();
        }
    }

    public static void shadow(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, float strength, int size) {
        for (int i = size; i >= 1; i--) {
            float t = 1.0F - (float)i / (size + 1);
            round(g, x - i, y - i + size * 0.35F, w + i * 2, h + i * 2, r + i, Colors.alpha(-16777216, t * t * 0.16F * strength));
        }
    }

    public static void roundGradient(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int left, int right) {
        if (!(w <= 0.0F) && !(h <= 0.0F)) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            int X = Math.round(x * 2.0F);
            int Y = Math.round(y * 2.0F);
            int W = Math.max(1, Math.round(w * 2.0F));
            int H = Math.max(1, Math.round(h * 2.0F));
            int R = Math.min(Math.round(r * 2.0F), Math.min(W, H) / 2);

            for (int j = 0; j < W; j++) {
                int inset = 0;
                if (R > 0) {
                    float dx = 0.0F;
                    if (j < R) {
                        dx = R - j - 0.5F;
                    } else if (j >= W - R) {
                        dx = j - (W - R) + 0.5F;
                    }

                    if (dx > 0.0F) {
                        inset = Math.round(R - (float)Math.sqrt(Math.max(0.0F, R * R - dx * dx)));
                    }
                }

                int c = Colors.lerp(left, right, W == 1 ? 0.0F : (float)j / (W - 1));
                if (Y + inset < Y + H - inset) {
                    g.fill(X + j, Y + inset, X + j + 1, Y + H - inset, c);
                }
            }

            pose.popMatrix();
        }
    }

    public static void gradientV(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, int top, int bottom) {
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(0.5F, 0.5F);
        g.fillGradient(Math.round(x1 * 2.0F), Math.round(y1 * 2.0F), Math.round(x2 * 2.0F), Math.round(y2 * 2.0F), top, bottom);
        pose.popMatrix();
    }

    public static void glow(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int color, int size) {
        if (color >>> 24 != 0) {
            for (int i = size; i >= 1; i--) {
                float t = 1.0F - (float)i / (size + 1);
                round(g, x - i, y - i, w + i * 2, h + i * 2, r + i, Colors.alpha(color, t * t * 0.22F));
            }
        }
    }

    public static void circle(GuiGraphicsExtractor g, float cx, float cy, float radius, int color) {
        round(g, cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, color);
    }

    public static void line(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float thick, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (color >>> 24 != 0) {
            int steps = Math.max(1, Math.round(len * 2.0F));
            int half = Math.max(1, Math.round(thick * 2.0F / 2.0F));
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            int lastX = Integer.MIN_VALUE;
            int lastY = Integer.MIN_VALUE;

            for (int i = 0; i <= steps; i++) {
                float t = (float)i / steps;
                int px = Math.round((x1 + dx * t) * 2.0F);
                int py = Math.round((y1 + dy * t) * 2.0F);
                if (px != lastX || py != lastY) {
                    lastX = px;
                    lastY = py;
                    g.fill(px - half, py - half, px + half, py + half, color);
                }
            }

            pose.popMatrix();
        }
    }

    public static void outline(GuiGraphicsExtractor g, float x, float y, float w, float h, float t, int color) {
        rect(g, x, y, x + w, y + t, color);
        rect(g, x, y + h - t, x + w, y + h, color);
        rect(g, x, y + t, x + t, y + h - t, color);
        rect(g, x + w - t, y + t, x + w, y + h - t, color);
    }

    public static void face(FontDescription f) {
        face = f;
    }

    private static <V> Map<String, V> lru(final int size) {
        return new LinkedHashMap<String, V>(size, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Entry<String, V> eldest) {
                return this.size() > size;
            }
        };
    }

    private static Component styled(String s) {
        Component c = STYLED.get(s);
        if (c == null) {
            c = Component.literal(s).withStyle(st -> st.withFont(face));
            STYLED.put(s, c);
        }

        return c;
    }

    public static boolean rightToLeft(String s) {
        for (int i = 0; i < s.length(); i++) {
            byte d = Character.getDirectionality(s.charAt(i));
            if (d == 1 || d == 2) {
                return true;
            }
        }

        return false;
    }

    public static float width(Font font, String s) {
        if (face == null) {
            return font.width(s);
        } else {
            Float w = WIDTHS.get(s);
            if (w == null) {
                w = (float)font.width(styled(s));
                WIDTHS.put(s, w);
            }

            return w;
        }
    }

    private static void draw(GuiGraphicsExtractor g, Font font, String s, int color, boolean shadow) {
        if (face == null) {
            g.text(font, s, 0, 0, color, shadow);
        } else {
            g.text(font, styled(s), 0, 0, color, shadow);
        }
    }

    public static void text(GuiGraphicsExtractor g, Font font, String s, float x, float y, int color, boolean shadow) {
        if (color >>> 24 >= 4 && !s.isEmpty()) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            draw(g, font, s, color, shadow);
            pose.popMatrix();
        }
    }

    public static void text(GuiGraphicsExtractor g, Font font, String s, float x, float y, float scale, int color, boolean shadow) {
        if (color >>> 24 >= 4 && !s.isEmpty()) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(scale, scale);
            draw(g, font, s, color, shadow);
            pose.popMatrix();
        }
    }

    public static void textCentered(GuiGraphicsExtractor g, Font font, String s, float cx, float y, float scale, int color, boolean shadow) {
        text(g, font, s, cx - width(font, s) * scale / 2.0F, y, scale, color, shadow);
    }

    public static void gradientText(
        GuiGraphicsExtractor g, Font font, String s, float x, float y, float scale, int c1, int c2, float phase, float alpha, boolean shadow
    ) {
        float cx = x;
        int n = s.length();

        for (int i = 0; i < n; i++) {
            String ch = String.valueOf(s.charAt(i));
            float t = (float)(Math.sin((float)i / Math.max(1, n) * Math.PI * 2.0 + phase) * 0.5 + 0.5);
            int c = Colors.alpha(Colors.lerp(c1, c2, t), alpha);
            text(g, font, ch, cx, y, scale, c, shadow);
            cx += width(font, ch) * scale;
        }
    }

    public static String trim(Font font, String s, float maxWidth, float scale) {
        if (width(font, s) * scale <= maxWidth) {
            return s;
        } else {
            String key = (face == null ? "m" : "s") + maxWidth + "/" + scale + "/" + s;
            String done = TRIMMED.get(key);
            if (done == null) {
                done = trimSlow(font, s, maxWidth, scale);
                TRIMMED.put(key, done);
            }

            return done;
        }
    }

    private static String trimSlow(Font font, String s, float maxWidth, float scale) {
        String dots = "…";

        while (
            !s.isEmpty()
                && (face == null ? font.width(s + dots) : font.width(Component.literal(s + dots).withStyle(st -> st.withFont(face)))) * scale > maxWidth
        ) {
            s = s.substring(0, s.length() - 1);
        }

        return s + dots;
    }

    public static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
