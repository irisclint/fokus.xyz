package xyz.fokus.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.hud.TabPingModule;

/** Tab list: the ping column (13px, the only 13 in extractRenderState) and what is drawn in it. */
@Mixin(PlayerTabOverlay.class)
public class TabPingMixin {
    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 13))
    private int fokus$slot(int vanilla) {
        return TabPingModule.slot(vanilla);
    }

    @Inject(method = "extractPingIcon", at = @At("HEAD"), cancellable = true)
    private void fokus$ping(GuiGraphicsExtractor g, int width, int x, int y, PlayerInfo info, CallbackInfo ci) {
        if (TabPingModule.draw(g, width, x, y, info)) {
            ci.cancel();
        }
    }
}
