package xyz.fokus.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.visual.ParticlesModule;

/**
 * Every particle the world asks for goes through createParticle (null is what the game itself
 * returns for a type without a provider); hit sparks come as tracking emitters.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Unique
    private boolean fokus$copying;

    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void fokus$hide(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz,
            CallbackInfoReturnable<Particle> cir) {
        if (ParticlesModule.hide(options.getType())) {
            cir.setReturnValue(null);
        }
    }

    /** The two-argument form (critical hits) builds its own emitter; it does not call the other. */
    @Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;)V",
            at = @At("TAIL"))
    private void fokus$more(Entity entity, ParticleOptions options, CallbackInfo ci) {
        this.fokus$copy(entity, options, -1);
    }

    @Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V",
            at = @At("TAIL"))
    private void fokus$moreTimed(Entity entity, ParticleOptions options, int lifetime, CallbackInfo ci) {
        this.fokus$copy(entity, options, lifetime);
    }

    @Unique
    private void fokus$copy(Entity entity, ParticleOptions options, int lifetime) {
        if (this.fokus$copying) {
            return;
        }
        int copies = ParticlesModule.hitCopies(options.getType());
        if (copies <= 1) {
            return;
        }
        this.fokus$copying = true;
        try {
            ParticleEngine self = (ParticleEngine) (Object) this;
            for (int i = 1; i < copies; i++) {
                if (lifetime < 0) {
                    self.createTrackingEmitter(entity, options);
                } else {
                    self.createTrackingEmitter(entity, options, lifetime);
                }
            }
        } finally {
            this.fokus$copying = false;
        }
    }
}
