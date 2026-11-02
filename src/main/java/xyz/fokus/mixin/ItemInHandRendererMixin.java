package xyz.fokus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.HandAnimator;

@Pseudo
@Mixin(
    targets = {"net.minecraft.client.renderer.ItemInHandRenderer"}
)
public class ItemInHandRendererMixin {
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

    @Inject(
        method = {"renderItem"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$item(
        LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci
    ) {
        if (context.firstPerson() && !HandAnimator.beforeItem(pose)) {
            ci.cancel();
        }
    }
}
