package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A short line of text typed into the menu (a chat macro, a word to look for). */
public class TextSetting extends Setting<String> {
    public final int maxLength;

    public TextSetting(String name, String defaultValue, int maxLength) {
        super(name, defaultValue);
        this.maxLength = maxLength;
    }

    public void type(String s) {
        if (this.value.length() + s.length() <= this.maxLength) {
            this.value = this.value + s;
        }
    }

    public void backspace() {
        if (!this.value.isEmpty()) {
            // A whole code point, so emoji and the like are not cut in half.
            this.value = this.value.substring(0, this.value.offsetByCodePoints(this.value.length(), -1));
        }
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            String s = json.getAsString();
            this.value = s.length() > this.maxLength ? s.substring(0, this.maxLength) : s;
        }
    }
}
