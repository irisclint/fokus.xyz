package xyz.fokus.mixin;

import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.visual.ClearViewModule;

/** The fog of blindness and darkness: an effect fog that does not apply is not drawn. */
@Mixin(MobEffectFogEnvironment.class)
public abstract class EffectFogMixin {
    @Shadow
    public abstract Holder<MobEffect> getMobEffect();

    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true)
    private void fokus$clear(FogType type, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ClearViewModule.hideEffectFog(this.getMobEffect())) {
            cir.setReturnValue(false);
        }
    }
}
