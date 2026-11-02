package xyz.fokus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.HandAnimator;

@Pseudo
@Mixin(
    targets = {"net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer"}
)
public class HandsRendererMixin {
    @ModifyVariable(
        method = {"applyItemArmTransform"},
        at = {@At("HEAD")},
        argsOnly = true
    )
    private float fokus$equip(float equip) {
        return HandAnimator.equip(equip);
    }

    @Inject(
        method = {"applyItemArmTransform"},
        at = {@At("TAIL")}
    )
    private void fokus$position(PoseStack pose, HumanoidArm arm, float equip, CallbackInfo ci) {
        HandAnimator.afterArmTransform(pose, arm);
    }

    @Inject(
        method = {"swingArm"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$swing(float swing, PoseStack pose, int side, HumanoidArm arm, CallbackInfo ci) {
        if (HandAnimator.swing(pose, swing, side, arm)) {
            ci.cancel();
        }
    }

    @WrapOperation(
        method = {"submitArmWithItem"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        )}
    )
    private void fokus$item(
        ItemStackRenderState state, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, int outline, Operation<Void> original
    ) {
        pose.pushPose();
        if (HandAnimator.beforeItem(pose)) {
            original.call(new Object[]{state, pose, collector, light, overlay, outline});
        }

        pose.popPose();
    }
}
