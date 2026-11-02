package xyz.fokus.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.pvp.FreelookModule;

@Mixin({Entity.class})
public class FreelookTurnMixin {
    @Inject(
        method = {"turn"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$freelook(double dx, double dy, CallbackInfo ci) {
        if (FreelookModule.active() && (Object)this instanceof LocalPlayer) {
            FreelookModule.turn(dx, dy);
            ci.cancel();
        }
    }
}
