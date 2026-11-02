package xyz.fokus.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.SkyHooks;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.SkyModule;

@Mixin({SkyRenderer.class})
public class SkyMixin {
    @Inject(
        method = {"extractRenderState"},
        at = {@At("TAIL")}
    )
    private void fokus$sky(ClientLevel level, float partial, Camera camera, SkyRenderState state, CallbackInfo ci) {
        SkyModule m = Modules.get(SkyModule.class);
        if (m != null && m.isEnabled()) {
            SkyHooks.apply(state, m);
            MoonPhase phase = m.moonPhase();
            if (phase != null) {
                state.moonPhase = phase;
            }
        }
    }
}
