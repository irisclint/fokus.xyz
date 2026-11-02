package xyz.fokus.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.VersionHooks;

@Mixin({LivingEntity.class})
public class SwingDurationMixin {
    @Inject(
        method = {"getModifiedSwingDuration"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void fokus$swingDuration(CallbackInfoReturnable<Integer> cir) {
        VersionHooks.swingDuration((LivingEntity)(Object)this, cir);
    }
}
