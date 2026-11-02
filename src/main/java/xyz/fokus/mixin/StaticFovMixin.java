package xyz.fokus.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.pvp.StaticFovModule;

/** The factor sprinting, effects and a drawn bow put on the field of view. */
@Mixin(AbstractClientPlayer.class)
public class StaticFovMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void fokus$staticFov(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(StaticFovModule.modifier((AbstractClientPlayer) (Object) this, cir.getReturnValue()));
    }
}
