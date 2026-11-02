package xyz.fokus.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.HudRenderer;

@Mixin({Gui.class})
public class GuiHudMixin {
    @Inject(
        method = {"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"},
        at = {@At("TAIL")}
    )
    private void fokus$hud(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        HudRenderer.render(g);
    }

    @Inject(
        method = {"extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$crosshair(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        if (HudRenderer.crosshair(g)) {
            ci.cancel();
        }
    }
}
