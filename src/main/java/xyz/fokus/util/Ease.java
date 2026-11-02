package xyz.fokus.util;

public final class Ease {
    private Ease() {
    }

    public static float clamp01(float t) {
        return t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
    }

    public static float outCubic(float t) {
        t = clamp01(t);
        float f = 1.0F - t;
        return 1.0F - f * f * f;
    }

    public static float inOutCubic(float t) {
        t = clamp01(t);
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
    }

    public static float outBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float f = t - 1.0F;
        return 1.0F + c3 * f * f * f + c1 * f * f;
    }

    public static float outQuart(float t) {
        t = clamp01(t);
        float f = 1.0F - t;
        return 1.0F - f * f * f * f;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
