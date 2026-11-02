package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import xyz.fokus.util.Colors;

public class ColorSetting extends Setting<Integer> {
    public static final int[] PRESETS = new int[]{-1, -3158065, -6645094, -10592674, -7362346, -2714990, -6502492, -2703217};
    private boolean rainbow;

    public ColorSetting(String name, int defaultValue) {
        super(name, defaultValue);
    }

    public int color(float offset) {
        return this.rainbow ? Colors.rainbow(offset, 1.0F) : this.value;
    }

    public int color() {
        return this.color(0.0F);
    }

    public boolean rainbow() {
        return this.rainbow;
    }

    @Override
    public boolean isDefault() {
        return !this.rainbow && super.isDefault();
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    @Override
    public void reset() {
        super.reset();
        this.rainbow = false;
    }

    @Override
    public JsonElement save() {
        JsonObject o = new JsonObject();
        o.add("rgb", new JsonPrimitive(String.format("#%08X", this.value)));
        o.add("rainbow", new JsonPrimitive(this.rainbow));
        return o;
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();

            try {
                if (o.has("rgb")) {
                    this.value = (int)Long.parseLong(o.get("rgb").getAsString().substring(1), 16);
                }
            } catch (RuntimeException var4) {
            }

            if (o.has("rainbow")) {
                this.rainbow = o.get("rainbow").getAsBoolean();
            }
        }
    }
}
