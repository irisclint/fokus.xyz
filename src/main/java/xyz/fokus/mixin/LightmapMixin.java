package xyz.fokus.mixin;

import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.FullbrightModule;

@Mixin({Lightmap.class})
public class LightmapMixin {
    @Inject(
        method = {"getBrightness"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private static void fokus$fullbright(DimensionType dimension, int level, CallbackInfoReturnable<Float> cir) {
        FullbrightModule m = Modules.get(FullbrightModule.class);
        if (m != null && m.isEnabled()) {
            cir.setReturnValue(1.0F);
        }
    }
}
