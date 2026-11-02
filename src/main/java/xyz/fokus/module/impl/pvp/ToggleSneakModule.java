package xyz.fokus.module.impl.pvp;

import net.minecraft.client.Minecraft;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;

public class ToggleSneakModule extends Module {
    public ToggleSneakModule() {
        super("Toggle Sneak", "Schleichen bleibt an, bis du das Modul wieder ausschaltest.", Category.PVP);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player != null && Compat.currentScreen() == null) {
            mc.options.keyShift.setDown(true);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keyShift.setDown(false);
    }
}
