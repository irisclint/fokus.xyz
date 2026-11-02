package xyz.fokus.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.visual.ClearWeatherModule;

@Mixin({Level.class})
public class WeatherMixin {
    @Inject(
        method = {"getRainLevel"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$rain(float partialTick, CallbackInfoReturnable<Float> cir) {
        if ((Object)this instanceof ClientLevel && ClearWeatherModule.noRain()) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(
        method = {"getThunderLevel"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$thunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        if ((Object)this instanceof ClientLevel && ClearWeatherModule.noThunder()) {
            cir.setReturnValue(0.0F);
        }
    }
}
