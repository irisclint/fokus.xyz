package xyz.fokus.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import xyz.fokus.FokusMod;

public final class CrystalTextures {
    private static final int K = 2;
    private static final Identifier ID = Identifier.fromNamespaceAndPath("fokus", "crystal/custom");
    private static String loadedKey = "";
    private static boolean ready;
    private static DynamicTexture texture;

    private CrystalTextures() {
    }

    public static Identifier current() {
        return ready ? ID : null;
    }

    public static void update(String style, int core, int frame, int base) {
        String key = style + core + "/" + frame + "/" + base;
        if (!key.equals(loadedKey)) {
            loadedKey = key;

            try {
                if (texture == null) {
                    NativeImage img = new NativeImage(128, 64, true);
                    draw(img, style, core & 16777215, frame & 16777215, base & 16777215);
                    texture = new DynamicTexture(() -> "fokus/crystal", img);
                    Minecraft.getInstance().getTextureManager().register(ID, texture);
                } else {
                    draw(texture.getPixels(), style, core & 16777215, frame & 16777215, base & 16777215);
                    texture.upload();
                }

                ready = true;
            } catch (RuntimeException var6) {
                FokusMod.LOG.warn("fokus.xyz: could not build the crystal texture", var6);
                ready = false;
            }
        }
    }

    public static void reset() {
        loadedKey = "";
    }

    private static void draw(NativeImage img, String style, int core, int frame, int base) {
        fill(img, 0, 0, 128, 64, frame, 0);

        for (int[] f : faces(0, 0, 8, 8, 8)) {
            glass(img, style, frame, f);
        }

        for (int[] f : faces(32, 0, 8, 8, 8)) {
            core(img, style, core, frame, f);
        }

        for (int[] f : faces(0, 16, 12, 4, 12)) {
            int x = f[0] * 2;
            int y = f[1] * 2;
            int w = f[2] * 2;
            int h = f[3] * 2;
            fill(img, x, y, w, h, base, 255);
            frame(img, x, y, w, h, 1, Colors.lerp(0xFF000000 | base, -1, 0.25F) & 16777215, 255);
        }
    }

    private static int[][] faces(int u, int v, int w, int h, int d) {
        return new int[][]{
            {u + d, v, w, d}, {u + d + w, v, w, d}, {u, v + d, d, h}, {u + d, v + d, w, h}, {u + d + w, v + d, d, h}, {u + 2 * d + w, v + d, w, h}
        };
    }

    private static void glass(NativeImage img, String style, int frame, int[] f) {
        int x = f[0] * 2;
        int y = f[1] * 2;
        int w = f[2] * 2;
        int h = f[3] * 2;
        switch (style) {
            case "Solid":
                fill(img, x, y, w, h, darken(frame, 0.75F), 255);
                frame(img, x, y, w, h, 1, frame, 255);
                break;
            case "Neon":
                frame(img, x, y, w, h, 1, frame, 255);
                frame(img, x + 1, y + 1, w - 2, h - 2, 1, lighten(frame, 0.5F), 255);
                break;
            default:
                frame(img, x, y, w, h, 1, frame, 255);
        }
    }

    private static void core(NativeImage img, String style, int core, int frame, int[] f) {
        int x = f[0] * 2;
        int y = f[1] * 2;
        int w = f[2] * 2;
        int h = f[3] * 2;
        switch (style) {
            case "Wire":
                fill(img, x, y, w, h, core, 0);
                frame(img, x + 2, y + 2, w - 4, h - 4, 1, core, 255);
                break;
            case "Gem":
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        float t = (float)(i + j) / (w + h - 2);
                        float facet = (i * 2 / w + j * 2 / h) % 2 == 0 ? 0.08F : -0.06F;
                        float light = 0.35F - 0.7F * t + facet;
                        int c = light >= 0.0F ? lighten(core, light) : darken(core, -light);
                        set(img, x + i, y + j, c, 255);
                    }
                }

                frame(img, x, y, w, h, 1, darken(core, 0.45F), 255);
                fill(img, x + 3, y + 3, 2, 2, 16777215, 255);
                break;
            case "Neon":
                float cx = x + w / 2.0F - 0.5F;
                float cy = y + h / 2.0F - 0.5F;
                float max = (float)Math.hypot(w / 2.0F, h / 2.0F);

                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        float d = (float)Math.hypot(x + i - cx, y + j - cy) / max;
                        set(img, x + i, y + j, lighten(core, Math.max(0.0F, 1.0F - d * 1.4F)), 255);
                    }
                }
                break;
            default:
                fill(img, x, y, w, h, core, 255);
                frame(img, x, y, w, h, 1, darken(core, 0.35F), 255);
                fill(img, x + 2, y + 2, w - 4, 1, lighten(core, 0.35F), 255);
        }
    }

    private static int lighten(int rgb, float t) {
        return Colors.lerp(0xFF000000 | rgb, -1, t) & 16777215;
    }

    private static int darken(int rgb, float t) {
        return Colors.lerp(0xFF000000 | rgb, -16777216, t) & 16777215;
    }

    private static void set(NativeImage img, int x, int y, int rgb, int alpha) {
        img.setPixel(x, y, alpha << 24 | rgb & 16777215);
    }

    private static void fill(NativeImage img, int x, int y, int w, int h, int rgb, int alpha) {
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                set(img, x + i, y + j, rgb, alpha);
            }
        }
    }

    private static void frame(NativeImage img, int x, int y, int w, int h, int t, int rgb, int alpha) {
        fill(img, x, y, w, t, rgb, alpha);
        fill(img, x, y + h - t, w, t, rgb, alpha);
        fill(img, x, y + t, t, h - 2 * t, rgb, alpha);
        fill(img, x + w - t, y + t, t, h - 2 * t, rgb, alpha);
    }
}
