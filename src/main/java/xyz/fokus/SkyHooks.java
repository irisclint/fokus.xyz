package xyz.fokus;

import java.lang.reflect.Field;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import xyz.fokus.module.impl.visual.SkyModule;
import xyz.fokus.util.Colors;

public final class SkyHooks {
    private static Field sky;
    private static Field sunset;
    private static Field stars;
    private static boolean looked;

    private SkyHooks() {
    }

    public static void apply(Object state, SkyModule m) {
        try {
            if (!looked) {
                looked = true;
                Class<?> c = state.getClass();
                sky = c.getField("skyColor");
                sunset = c.getField("sunriseAndSunsetColor");
                stars = c.getField("starBrightness");
            }

            int skyColor = m.sky.color(0.0F);
            if (m.dayNight.on()) {
                skyColor = Colors.scale(skyColor, brightness(sky.get(state)));
            }

            write(sky, state, skyColor, 1.0F, false);
            if (m.sunset.on()) {
                write(sunset, state, m.sunsetColor.color(0.4F), 0.0F, true);
            }

            if (m.stars.f() > 0.0F) {
                stars.setFloat(state, Math.max(stars.getFloat(state), m.stars.f()));
            }
        } catch (RuntimeException | ReflectiveOperationException var3) {
        }
    }

    private static float brightness(Object value) {
        float max;
        if (value instanceof Integer argb) {
            max = Math.max(Colors.r(argb), Math.max(Colors.g(argb), Colors.b(argb))) / 255.0F;
        } else {
            if (!(value instanceof Vector3fc v)) {
                return 1.0F;
            }

            max = Math.max(v.x(), Math.max(v.y(), v.z()));
        }

        return Math.max(0.0F, Math.min(1.0F, max));
    }

    private static void write(Field f, Object state, int rgb, float defaultAlpha, boolean keepAlpha) throws IllegalAccessException {
        float r = Colors.r(rgb) / 255.0F;
        float g = Colors.g(rgb) / 255.0F;
        float b = Colors.b(rgb) / 255.0F;
        Class<?> t = f.getType();
        if (t == int.class) {
            int alpha = keepAlpha ? f.getInt(state) & 0xFF000000 : -16777216;
            f.setInt(state, alpha | rgb & 16777215);
        } else if (t.isAssignableFrom(Vector4f.class)) {
            float a = defaultAlpha;
            if (keepAlpha && f.get(state) instanceof Vector4fc old) {
                a = old.w();
            }

            f.set(state, new Vector4f(r, g, b, a));
        } else if (t.isAssignableFrom(Vector3f.class)) {
            f.set(state, new Vector3f(r, g, b));
        }
    }
}
