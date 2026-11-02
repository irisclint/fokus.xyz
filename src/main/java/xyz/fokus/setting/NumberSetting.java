package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;
import xyz.fokus.util.Anim;

public class NumberSetting extends Setting<Double> {
    public final double min;
    public final double max;
    public final double step;
    public final String suffix;
    public final Anim fill = new Anim(0.0F, 18.0F);

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, defaultValue, min, max, step, "");
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
        this.fill.snap((float)this.progress());
    }

    public float f() {
        return this.value.floatValue();
    }

    public int i() {
        return (int)Math.round(this.value);
    }

    public void set(Double v) {
        double snapped = Math.round(v / this.step) * this.step;
        this.value = Math.max(this.min, Math.min(this.max, snapped));
    }

    public double progress() {
        return (this.value - this.min) / (this.max - this.min);
    }

    public void setProgress(double p) {
        this.set(this.min + (this.max - this.min) * Math.max(0.0, Math.min(1.0, p)));
    }

    public String display() {
        if (this.step >= 1.0) {
            return Math.round(this.value) + this.suffix;
        } else {
            return this.step >= 0.1
                ? String.format(Locale.ROOT, "%.1f", this.value) + this.suffix
                : String.format(Locale.ROOT, "%.2f", this.value) + this.suffix;
        }
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            this.set(json.getAsDouble());
            this.fill.snap((float)this.progress());
        }
    }
}
