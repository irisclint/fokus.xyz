package xyz.fokus.util;

import com.mojang.blaze3d.platform.NativeImage;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public final class GlassBox {
    private static final int Q = 32;
    private static final int S = 2;
    private static final Identifier FILL = Identifier.fromNamespaceAndPath("fokus", "box/fill");
    private static final Identifier RING = Identifier.fromNamespaceAndPath("fokus", "box/ring");
    private static DynamicTexture fill;
    private static DynamicTexture ring;
    private static int ringFor = -1;
    private static Method tinted;
    private static Object pipeline;
    private static boolean looked;

    private GlassBox() {
    }

    public static void prepare(float radius) {
        int r = Math.round(radius * 2.0F);
        if (fill == null) {
            NativeImage img = new NativeImage(64, 64, true);
            paint(img, 0.0F);
            fill = new DynamicTexture(() -> "fokus/box-fill", img);
            Minecraft.getInstance().getTextureManager().register(FILL, fill);
        }

        if (r != ringFor && r > 0) {
            float width = 32.0F / r;
            if (ring == null) {
                ring = new DynamicTexture(() -> "fokus/box-ring", new NativeImage(64, 64, true));
                Minecraft.getInstance().getTextureManager().register(RING, ring);
            }

            paint(ring.getPixels(), width);
            ring.upload();
            ringFor = r;
        }
    }

    private static void paint(NativeImage img, float line) {
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int hits = 0;

                for (int sy = 0; sy < 4; sy++) {
                    for (int sx = 0; sx < 4; sx++) {
                        double dx = x + (sx + 0.5) / 4.0 - 32.0;
                        double dy = y + (sy + 0.5) / 4.0 - 32.0;
                        double d = Math.sqrt(dx * dx + dy * dy);
                        if (d <= 32.0 && (line <= 0.0F || d >= 32.0F - line)) {
                            hits++;
                        }
                    }
                }

                img.setPixel(x, y, Math.round(hits * 255 / 16.0F) << 24 | 16777215);
            }
        }
    }

    private static boolean ready() {
        if (!looked) {
            looked = true;

            try {
                Class<?> pipelines = Class.forName("net.minecraft.client.renderer.RenderPipelines");
                pipeline = pipelines.getField("GUI_TEXTURED").get(null);

                for (Method m : GuiGraphicsExtractor.class.getMethods()) {
                    Class<?>[] p = m.getParameterTypes();
                    if (m.getName().equals("blit") && p.length == 13 && p[0].isInstance(pipeline) && p[1] == Identifier.class) {
                        tinted = m;
                    }
                }
            } catch (RuntimeException | ReflectiveOperationException var6) {
                tinted = null;
            }
        }

        return tinted != null && fill != null;
    }

    private static boolean corner(GuiGraphicsExtractor g, Identifier tex, int x, int y, int size, int qx, int qy, int color) {
        try {
            tinted.invoke(g, pipeline, tex, x, y, (float)(qx * 32), (float)(qy * 32), size, size, 32, 32, 64, 64, color);
            return true;
        } catch (RuntimeException | ReflectiveOperationException var9) {
            tinted = null;
            return false;
        }
    }

    public static boolean fill(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int color) {
        if (!ready()) {
            return false;
        } else if (color >>> 24 != 0 && !(w <= 0.0F) && !(h <= 0.0F)) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            int X = Math.round(x * 2.0F);
            int Y = Math.round(y * 2.0F);
            int W = Math.max(1, Math.round(w * 2.0F));
            int H = Math.max(1, Math.round(h * 2.0F));
            int R = Math.min(Math.round(r * 2.0F), Math.min(W, H) / 2);
            boolean ok = true;
            if (R <= 0) {
                g.fill(X, Y, X + W, Y + H, color);
            } else {
                g.fill(X + R, Y, X + W - R, Y + H, color);
                g.fill(X, Y + R, X + R, Y + H - R, color);
                g.fill(X + W - R, Y + R, X + W, Y + H - R, color);
                ok = corner(g, FILL, X, Y, R, 0, 0, color)
                    && corner(g, FILL, X + W - R, Y, R, 1, 0, color)
                    && corner(g, FILL, X, Y + H - R, R, 0, 1, color)
                    && corner(g, FILL, X + W - R, Y + H - R, R, 1, 1, color);
            }

            pose.popMatrix();
            return ok;
        } else {
            return true;
        }
    }

    public static boolean edge(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int color) {
        int R0 = Math.round(r * 2.0F);
        if (!ready() || ring == null || R0 != ringFor) {
            return false;
        } else if (color >>> 24 != 0 && !(w <= 1.0F) && !(h <= 1.0F)) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(0.5F, 0.5F);
            int X = Math.round(x * 2.0F);
            int Y = Math.round(y * 2.0F);
            int W = Math.round(w * 2.0F);
            int H = Math.round(h * 2.0F);
            int R = Math.min(R0, Math.min(W, H) / 2);
            boolean ok = R == R0;
            if (ok) {
                g.fill(X + R, Y, X + W - R, Y + 1, color);
                g.fill(X + R, Y + H - 1, X + W - R, Y + H, color);
                g.fill(X, Y + R, X + 1, Y + H - R, color);
                g.fill(X + W - 1, Y + R, X + W, Y + H - R, color);
                ok = corner(g, RING, X, Y, R, 0, 0, color)
                    && corner(g, RING, X + W - R, Y, R, 1, 0, color)
                    && corner(g, RING, X, Y + H - R, R, 0, 1, color)
                    && corner(g, RING, X + W - R, Y + H - R, R, 1, 1, color);
            }

            pose.popMatrix();
            return ok;
        } else {
            return true;
        }
    }
}
