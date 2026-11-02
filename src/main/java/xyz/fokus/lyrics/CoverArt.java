package xyz.fokus.lyrics;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import xyz.fokus.FokusMod;

public final class CoverArt {
    private static final int SIZE = 96;
    private static final Identifier ID = Identifier.fromNamespaceAndPath("fokus", "cover");
    private static DynamicTexture texture;
    private static volatile int loadedId;
    private static volatile int wantedId;
    private static volatile boolean ready;
    private static volatile int tint = -1;
    private static Method tinted;
    private static Object pipeline;
    private static boolean looked;

    private CoverArt() {
    }

    static void offer(int id, Path file) {
        if (id != wantedId) {
            wantedId = id;
            if (id == 0) {
                ready = false;
            } else {
                Thread t = new Thread(() -> load(id, file), "fokus-cover");
                t.setDaemon(true);
                t.start();
            }
        }
    }

    private static void load(int id, Path file) {
        try {
            BufferedImage src = ImageIO.read(Files.newInputStream(file));
            if (src == null || id != wantedId) {
                return;
            }

            int side = Math.min(src.getWidth(), src.getHeight());
            BufferedImage square = new BufferedImage(96, 96, 2);
            Graphics2D g = square.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int sx = (src.getWidth() - side) / 2;
            int sy = (src.getHeight() - side) / 2;
            g.drawImage(src, 0, 0, 96, 96, sx, sy, sx + side, sy + side, null);
            g.dispose();
            int[] argb = square.getRGB(0, 0, 96, 96, null, 0, 96);
            long r = 0L;
            long gr = 0L;
            long b = 0L;

            for (int c : argb) {
                r += c >> 16 & 0xFF;
                gr += c >> 8 & 0xFF;
                b += c & 0xFF;
            }

            int n = argb.length;
            int avg = 0xFF000000 | (int)(r / n) << 16 | (int)(gr / n) << 8 | (int)(b / n);
            Minecraft.getInstance().execute(() -> upload(id, argb, avg));
        } catch (Exception var19) {
            FokusMod.LOG.debug("fokus.xyz: cover could not be read", var19);
        }
    }

    private static void upload(int id, int[] argb, int avg) {
        if (id == wantedId) {
            if (texture == null) {
                texture = new DynamicTexture(() -> "fokus/cover", new NativeImage(96, 96, true));
                Minecraft.getInstance().getTextureManager().register(ID, texture);
            }

            NativeImage img = texture.getPixels();

            for (int y = 0; y < 96; y++) {
                for (int x = 0; x < 96; x++) {
                    img.setPixel(x, y, argb[y * 96 + x]);
                }
            }

            texture.upload();
            loadedId = id;
            tint = avg;
            ready = true;
        }
    }

    public static boolean ready() {
        return ready && loadedId == wantedId;
    }

    public static int tint() {
        return tint;
    }

    public static void draw(GuiGraphicsExtractor g, int x, int y, int size, float alpha) {
        if (ready()) {
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
                } catch (RuntimeException | ReflectiveOperationException var12) {
                    tinted = null;
                }
            }

            int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
            if (tinted != null) {
                try {
                    tinted.invoke(g, pipeline, ID, x, y, 0.0F, 0.0F, size, size, 96, 96, 96, 96, a << 24 | 16777215);
                    return;
                } catch (RuntimeException | ReflectiveOperationException var11) {
                    tinted = null;
                }
            }

            if (a > 200) {
                g.blit(ID, x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
            }
        }
    }
}
