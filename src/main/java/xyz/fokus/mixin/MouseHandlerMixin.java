package xyz.fokus.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.Stats;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.pvp.ZoomModule;

@Mixin({MouseHandler.class})
public class MouseHandlerMixin {
    @Inject(
        method = {"onButton"},
        at = {@At("HEAD")}
    )
    private void fokus$click(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (action == Compat.PRESS && ((MouseHandler)(Object)this).isMouseGrabbed()) {
            if (info.button() == Compat.MOUSE_LEFT) {
                Stats.click(true);
            } else if (info.button() == Compat.MOUSE_RIGHT) {
                Stats.click(false);
            }
        }
    }

    @Inject(
        method = {"onScroll"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$scroll(long window, double x, double y, CallbackInfo ci) {
        ZoomModule zoom = Modules.get(ZoomModule.class);
        if (zoom != null && zoom.isEnabled() && Compat.currentScreen() == null && y != 0.0 && zoom.onScroll(y)) {
            ci.cancel();
        }
    }
}
