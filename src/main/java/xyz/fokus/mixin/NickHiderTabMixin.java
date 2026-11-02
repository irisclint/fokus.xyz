package xyz.fokus.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.hud.NickHiderModule;

/** The name each row of the tab list shows. */
@Mixin(PlayerTabOverlay.class)
public class NickHiderTabMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void fokus$hide(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(NickHiderModule.replace(cir.getReturnValue()));
    }
}
