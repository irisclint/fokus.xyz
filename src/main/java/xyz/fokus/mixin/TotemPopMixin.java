package xyz.fokus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.TotemModule;

@Mixin({ScreenEffectRenderer.class})
public class TotemPopMixin {
    @Inject(
        method = {"renderItemActivationAnimation"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$hidePop(CallbackInfo ci) {
        TotemModule m = Modules.get(TotemModule.class);
        if (m != null && m.popScale() <= 0.001F) {
            ci.cancel();
        }
    }

    @WrapOperation(
        method = {"renderItemActivationAnimation"},
        at = {@At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"
        )}
    )
    private void fokus$popSize(PoseStack pose, float x, float y, float z, Operation<Void> original) {
        TotemModule m = Modules.get(TotemModule.class);
        float k = m == null ? 1.0F : m.popScale();
        original.call(new Object[]{pose, x * k, y * k, z * k});
    }
}
