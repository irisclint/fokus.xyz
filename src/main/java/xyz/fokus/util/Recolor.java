package xyz.fokus.util;

import java.awt.image.BufferedImage;

public final class Recolor {
    public static final String[] STYLES = new String[]{
        "Vanilla", "Umfärben", "Mono", "Kontur", "Invertiert", "Pastell", "Kräftig", "Dunkel", "Hell", "Regenbogen", "Gold", "Eis", "Feuer", "Neon"
    };

    private Recolor() {
    }

    public static boolean usesColors(String style) {
        return style.equals("Umfärben") || style.equals("Kontur") || style.equals("Invertiert");
    }

    public static BufferedImage apply(BufferedImage src, TexturePack.Look look) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (look.style().equals("Vanilla")) {
            return src;
        } else {
            BufferedImage out = new BufferedImage(w, h, 2);
            int dark = 0xFF000000 | look.dark();
            int light = 0xFF000000 | look.light();

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = src.getRGB(x, y);
                    int a = argb >>> 24;
                    if (a == 0) {
                        out.setRGB(x, y, 0);
                    } else {
                        float lum = luminance(argb);
                        float[] hsv = hsv(argb);
                        String var14 = look.style();

                        int rgb = switch (var14) {
                            case "Mono" -> gray(0.12F + lum * 0.88F);
                            case "Invertiert" -> Colors.lerp(light, dark, lum);
                            case "Kontur" -> edge(src, x, y) ? light : Colors.lerp(dark, -16777216, 0.35F);
                            case "Pastell" -> Colors.lerp(Colors.hsv(hsv[0], hsv[1] * 0.45F, 0.75F + hsv[2] * 0.25F), -1, 0.15F);
                            case "Kräftig" -> Colors.hsv(hsv[0], Math.min(1.0F, hsv[1] * 1.6F + 0.1F), Math.min(1.0F, hsv[2] * 1.1F));
                            case "Dunkel" -> Colors.hsv(hsv[0], hsv[1], hsv[2] * 0.55F);
                            case "Hell" -> Colors.lerp(argb | 0xFF000000, -1, 0.3F);
                            case "Regenbogen" -> Colors.hsv((float)(x + y) / (w + h), 0.7F, 0.35F + lum * 0.65F);
                            case "Gold" -> Colors.lerp(-11915264, -7558, lum);
                            case "Eis" -> Colors.lerp(-16045238, -2689281, lum);
                            case "Feuer" -> Colors.lerp(-12974592, -19148, lum);
                            case "Neon" -> Colors.hsv(hsv[0], 1.0F, Math.min(1.0F, 0.55F + lum * 0.6F));
                            default -> Colors.lerp(dark, light, lum);
                        };
                        out.setRGB(x, y, a << 24 | rgb & 16777215);
                    }
                }
            }

            return out;
        }
    }

    private static boolean edge(BufferedImage img, int x, int y) {
        int[][] around = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] d : around) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx < 0 || ny < 0 || nx >= img.getWidth() || ny >= img.getHeight() || img.getRGB(nx, ny) >>> 24 == 0) {
                return true;
            }
        }

        return false;
    }

    private static float luminance(int argb) {
        float r = (argb >> 16 & 0xFF) / 255.0F;
        float g = (argb >> 8 & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        return Math.min(1.0F, (0.299F * r + 0.587F * g + 0.114F * b) * 1.15F);
    }

    private static float[] hsv(int argb) {
        float r = (argb >> 16 & 0xFF) / 255.0F;
        float g = (argb >> 8 & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float s = max == 0.0F ? 0.0F : (max - min) / max;
        return new float[]{Colors.hue(argb), s, max};
    }

    private static int gray(float v) {
        int c = Math.round(Math.max(0.0F, Math.min(1.0F, v)) * 255.0F);
        return 0xFF000000 | c << 16 | c << 8 | c;
    }
}
