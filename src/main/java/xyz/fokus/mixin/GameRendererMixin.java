package xyz.fokus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.pvp.NoHurtCamModule;
import xyz.fokus.module.impl.visual.NoBobModule;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
    @Inject(
        method = {"bobHurt"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$noHurtCam(CameraRenderState camera, PoseStack pose, CallbackInfo ci) {
        if (Modules.get(NoHurtCamModule.class).isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"bobView"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$noBob(CameraRenderState camera, PoseStack pose, CallbackInfo ci) {
        if (Modules.get(NoBobModule.class).isEnabled()) {
            ci.cancel();
        }
    }
}
