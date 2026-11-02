package xyz.fokus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class ActionSetting extends Setting<Runnable> {
    public ActionSetting(String name, Runnable action) {
        super(name, action);
    }

    public void run() {
        this.value.run();
    }

    @Override
    public JsonElement save() {
        return JsonNull.INSTANCE;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public void load(JsonElement json) {
    }
}
