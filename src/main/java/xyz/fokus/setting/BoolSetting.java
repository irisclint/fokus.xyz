package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import xyz.fokus.util.Anim;

public class BoolSetting extends Setting<Boolean> {
    public final Anim knob;

    public BoolSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
        this.knob = new Anim(defaultValue ? 1.0F : 0.0F, 16.0F);
    }

    public boolean on() {
        return this.value;
    }

    public void toggle() {
        this.value = !this.value;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            this.value = json.getAsBoolean();
            this.knob.snap(this.value ? 1.0F : 0.0F);
        }
    }
}
