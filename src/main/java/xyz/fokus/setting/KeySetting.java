package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import xyz.fokus.compat.Compat;

public class KeySetting extends Setting<Integer> {
    private boolean wasDown;

    public KeySetting(String name, int defaultValue) {
        super(name, defaultValue);
    }

    public String display() {
        return Compat.keyName(this.value);
    }

    public boolean pressed() {
        boolean down = this.value >= 0 && Compat.isKeyDown(this.value);
        boolean edge = down && !this.wasDown;
        this.wasDown = down;
        return edge;
    }

    public boolean held() {
        return this.value >= 0 && Compat.isKeyDown(this.value);
    }

    public void clearEdge() {
        this.wasDown = this.value >= 0 && Compat.isKeyDown(this.value);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            this.value = json.getAsInt();
        }
    }
}
