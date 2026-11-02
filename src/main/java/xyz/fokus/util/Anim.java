package xyz.fokus.util;

public final class Anim {
    private float value;
    private float target;
    private final float speed;
    private long last = System.nanoTime();

    public Anim(float initial) {
        this(initial, 14.0F);
    }

    public Anim(float initial, float speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
    }

    public Anim to(float target) {
        this.target = target;
        return this;
    }

    public float get() {
        long now = System.nanoTime();
        float dt = Math.min((float)(now - this.last) / 1.0E9F, 0.1F);
        this.last = now;
        float k = 1.0F - (float)Math.exp(-dt * this.speed);
        this.value = this.value + (this.target - this.value) * k;
        if (Math.abs(this.target - this.value) < 5.0E-4F) {
            this.value = this.target;
        }

        return this.value;
    }

    public void snap(float v) {
        this.value = v;
        this.target = v;
        this.last = System.nanoTime();
    }

    public float target() {
        return this.target;
    }
}
