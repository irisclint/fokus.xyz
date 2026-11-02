package xyz.fokus.util;

public final class Colors {
    private Colors() {
    }

    public static int a(int c) {
        return c >>> 24;
    }

    public static int r(int c) {
        return c >> 16 & 0xFF;
    }

    public static int g(int c) {
        return c >> 8 & 0xFF;
    }

    public static int b(int c) {
        return c & 0xFF;
    }

    public static int argb(int a, int r, int g, int b) {
        return clamp(a) << 24 | clamp(r) << 16 | clamp(g) << 8 | clamp(b);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }

    public static int alpha(int c, float f) {
        int a = Math.round(a(c) * Math.max(0.0F, Math.min(1.0F, f)));
        return a << 24 | c & 16777215;
    }

    public static int withAlpha(int c, int alpha) {
        return clamp(alpha) << 24 | c & 16777215;
    }

    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        return argb(
            Math.round(a(c1) + (a(c2) - a(c1)) * t),
            Math.round(r(c1) + (r(c2) - r(c1)) * t),
            Math.round(g(c1) + (g(c2) - g(c1)) * t),
            Math.round(b(c1) + (b(c2) - b(c1)) * t)
        );
    }

    public static int hsv(float h, float s, float v) {
        h = (h % 1.0F + 1.0F) % 1.0F * 6.0F;
        int i = (int)Math.floor(h);
        float f = h - i;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * f);
        float t = v * (1.0F - s * (1.0F - f));
        float r;
        float g;
        float b;
        switch (i % 6) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
        }

        return argb(255, Math.round(r * 255.0F), Math.round(g * 255.0F), Math.round(b * 255.0F));
    }

    public static float hue(int c) {
        float r = r(c) / 255.0F;
        float g = g(c) / 255.0F;
        float b = b(c) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        if (d == 0.0F) {
            return 0.0F;
        } else {
            float h;
            if (max == r) {
                h = (g - b) / d % 6.0F;
            } else if (max == g) {
                h = (b - r) / d + 2.0F;
            } else {
                h = (r - g) / d + 4.0F;
            }

            h /= 6.0F;
            return h < 0.0F ? h + 1.0F : h;
        }
    }

    public static int brighten(int c, float f) {
        return lerp(c, c & 0xFF000000 | 16777215, f);
    }

    public static int darken(int c, float f) {
        return lerp(c, c & 0xFF000000, f);
    }

    public static int rainbow(float offset, float speed) {
        float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0F * speed;
        return hsv((t * 0.12F + offset) % 1.0F, 0.65F, 1.0F);
    }

    public static int scale(int argb, float f) {
        f = Math.max(0.0F, Math.min(1.0F, f));
        int r = Math.round(r(argb) * f);
        int g = Math.round(g(argb) * f);
        int b = Math.round(b(argb) * f);
        return argb & 0xFF000000 | r << 16 | g << 8 | b;
    }
}
