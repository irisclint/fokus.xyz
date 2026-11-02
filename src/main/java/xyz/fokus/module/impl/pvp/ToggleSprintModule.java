package xyz.fokus.module.impl.pvp;

import net.minecraft.client.Minecraft;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.BoolSetting;

public class ToggleSprintModule extends Module {
    public final BoolSetting onlyForward = this.add(new BoolSetting("Nur vorwärts", true));

    public ToggleSprintModule() {
        super("Toggle Sprint", "Du sprintest automatisch, ohne die Sprinttaste zu halten.", Category.PVP);
        this.enabledByDefault();
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player != null && Compat.currentScreen() == null) {
            boolean forward = mc.options.keyUp.isDown();
            mc.options.keySprint.setDown(!this.onlyForward.on() || forward);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keySprint.setDown(false);
    }
}
