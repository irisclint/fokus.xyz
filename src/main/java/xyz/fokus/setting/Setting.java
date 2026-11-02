package xyz.fokus.setting;

import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import xyz.fokus.util.Anim;

public abstract class Setting<T> {
    public final String name;
    protected T value;
    protected T defaultValue;
    private BooleanSupplier visible = () -> true;
    public final Anim hover = new Anim(0.0F, 18.0F);
    private String label;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    public boolean isDefault() {
        return Objects.equals(this.value, this.defaultValue);
    }

    public void setDefault(T value) {
        this.defaultValue = value;
        this.set(value);
    }

    public boolean isVisible() {
        return this.visible.getAsBoolean();
    }

    public String label() {
        return this.label != null ? this.label : this.name;
    }

    public <S extends Setting<T>> S labeled(String label) {
        this.label = label;
        return (S)this;
    }

    public <S extends Setting<T>> S visibleWhen(BooleanSupplier condition) {
        this.visible = condition;
        return (S)this;
    }

    public abstract JsonElement save();

    public abstract void load(JsonElement var1);
}
