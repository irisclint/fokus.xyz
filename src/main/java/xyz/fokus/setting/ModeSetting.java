package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public class ModeSetting extends Setting<String> {
    public final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = List.of(modes);
    }

    public boolean is(String mode) {
        return this.value.equals(mode);
    }

    public int index() {
        return Math.max(0, this.modes.indexOf(this.value));
    }

    public void cycle(int dir) {
        int n = this.modes.size();
        this.value = this.modes.get(((this.index() + dir) % n + n) % n);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive() && this.modes.contains(json.getAsString())) {
            this.value = json.getAsString();
        }
    }
}
