package xyz.fokus.mixin;

import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.CrystalHooks;

@Mixin({EndCrystalModel.class})
public class CrystalModelMixin {
    private static final String SETUP = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;)V";

    @Inject(
        method = {"setupAnim(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;)V"},
        at = {@At("HEAD")}
    )
    private void fokus$speed(EndCrystalRenderState state, CallbackInfo ci) {
        CrystalHooks.beforeAnim(state);
    }

    @Inject(
        method = {"setupAnim(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;)V"},
        at = {@At("RETURN")}
    )
    private void fokus$speedEnd(EndCrystalRenderState state, CallbackInfo ci) {
        CrystalHooks.afterAnim(state);
    }
}
