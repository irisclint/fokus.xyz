package xyz.fokus.mixin;

import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.fokus.module.impl.visual.SkyModule;

@Mixin({CloudRenderer.class})
public class CloudColorMixin {
    @ModifyVariable(
        method = {"prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V"},
        at = {@At("HEAD")},
        argsOnly = true,
        ordinal = 0
    )
    private int fokus$cloudColor(int color) {
        return SkyModule.cloudColor(color);
    }
}
