package xyz.fokus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.fokus.module.impl.pvp.FreelookModule;

@Mixin({Camera.class})
public class FreelookCameraMixin {
    @WrapOperation(
        method = {"alignWithEntity"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
        )}
    )
    private float fokus$yaw(Entity entity, float partial, Operation<Float> original) {
        return FreelookModule.active() && entity == Minecraft.getInstance().player ? FreelookModule.yaw() : (Float)original.call(new Object[]{entity, partial});
    }

    @WrapOperation(
        method = {"alignWithEntity"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"
        )}
    )
    private float fokus$pitch(Entity entity, float partial, Operation<Float> original) {
        return FreelookModule.active() && entity == Minecraft.getInstance().player
            ? FreelookModule.pitch()
            : (Float)original.call(new Object[]{entity, partial});
    }
}
