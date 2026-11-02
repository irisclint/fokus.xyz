package xyz.fokus.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TotemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.TotemModule;
import xyz.fokus.util.Colors;

@Mixin({TotemParticle.class})
public class TotemParticleMixin {
    @Inject(
        method = {"<init>"},
        at = {@At("RETURN")}
    )
    private void fokus$color(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites, CallbackInfo ci) {
        TotemModule m = Modules.get(TotemModule.class);
        if (m != null && m.customParticles()) {
            int c = Colors.lerp(m.particle1.color(0.0F), m.particle2.color(0.5F), (float)Math.random());
            SimpleAnimatedParticle self = (SimpleAnimatedParticle)(Object)this;
            self.setColor(Colors.r(c) / 255.0F, Colors.g(c) / 255.0F, Colors.b(c) / 255.0F);
            self.setFadeColor(Colors.darken(c, 0.4F) & 16777215);
        }
    }
}
