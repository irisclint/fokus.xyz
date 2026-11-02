package xyz.fokus.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.SkyProjection;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.pvp.ZoomModule;

@Mixin({Camera.class})
public class CameraMixin {
    @Inject(
        method = {"calculateFov"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void fokus$zoom(float partial, CallbackInfoReturnable<Float> cir) {
        ZoomModule zoom = Modules.get(ZoomModule.class);
        if (zoom != null && zoom.isEnabled()) {
            cir.setReturnValue(zoom.apply((Float)cir.getReturnValue()));
        }

        SkyProjection.fov = (Float)cir.getReturnValue();
    }
}
