package xyz.fokus;

public final class SkyAnchor {
    public static final String FOLLOW = "Himmel - folgt dem Blick";
    public static final String FIXED = "Himmel - fest";
    public static final String SCREEN = "Bildschirm";
    private float yaw = Float.NaN;
    private long lastFrame = System.nanoTime();

    public void reset() {
        this.yaw = SkyProjection.viewYaw();
    }

    public float[] place(String mode, float elevation, float follow, int w, int h) {
        long now = System.nanoTime();
        float dt = Math.min(0.1F, (float)(now - this.lastFrame) / 1.0E9F);
        this.lastFrame = now;
        float view = SkyProjection.viewYaw();
        if (Float.isNaN(this.yaw)) {
            this.yaw = view;
        }

        if (mode.equals("Himmel - folgt dem Blick")) {
            this.yaw = this.yaw + wrap(view - this.yaw) * (1.0F - (float)Math.exp(-dt * follow));
        }

        float[] p = SkyProjection.project(this.yaw, elevation, w, h);
        if (p == null) {
            return null;
        } else {
            p[2] = Math.max(0.0F, Math.min(1.0F, (p[2] - 0.35F) / 0.3F));
            return p;
        }
    }

    private static float wrap(float deg) {
        deg %= 360.0F;
        if (deg >= 180.0F) {
            deg -= 360.0F;
        }

        if (deg < -180.0F) {
            deg += 360.0F;
        }

        return deg;
    }
}
