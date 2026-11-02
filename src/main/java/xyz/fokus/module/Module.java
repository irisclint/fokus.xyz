package xyz.fokus.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.Setting;
import xyz.fokus.util.Anim;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final KeySetting bind = new KeySetting("Taste", -1);
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    protected boolean alwaysOn;
    public final Anim toggleAnim = new Anim(0.0F, 14.0F);
    public final Anim expandAnim = new Anim(0.0F, 13.0F);
    public final Anim hoverAnim = new Anim(0.0F, 18.0F);
    public final Anim listAnim = new Anim(0.0F, 10.0F);
    private boolean defaultEnabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <S extends Setting<?>> S add(S setting) {
        this.settings.add(setting);
        return setting;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(this.settings);
    }

    public boolean isEnabled() {
        return this.alwaysOn || this.enabled;
    }

    public boolean isAlwaysOn() {
        return this.alwaysOn;
    }

    public void setEnabled(boolean on) {
        if (!this.alwaysOn && on != this.enabled) {
            this.enabled = on;
            if (on) {
                this.onEnable();
            } else {
                this.onDisable();
            }
        }
    }

    public void toggle() {
        this.setEnabled(!this.enabled);
    }

    protected final void enabledByDefault() {
        this.defaultEnabled = true;
        this.enabled = true;
        this.toggleAnim.snap(1.0F);
    }

    public boolean isDefaultEnabled() {
        return this.defaultEnabled;
    }

    public void resetToDefaults() {
        if (this.enabled != this.defaultEnabled) {
            this.setEnabled(this.defaultEnabled);
        }

        this.toggleAnim.snap(this.isEnabled() ? 1.0F : 0.0F);
        this.bind.reset();
        this.settings.forEach(Setting::reset);
    }

    public void loadEnabled(boolean on) {
        this.enabled = on;
        this.toggleAnim.snap(this.isEnabled() ? 1.0F : 0.0F);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick(Minecraft mc) {
    }

    public boolean showInList() {
        return !this.alwaysOn && this.category != Category.HUD;
    }
}
