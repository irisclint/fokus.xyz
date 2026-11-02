package xyz.fokus.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.visual.ClearViewModule;

@Pseudo
@Mixin(
    targets = {"net.minecraft.client.gui.Hud"}
)
public class ClearViewHudMixin {
    @Inject(
        method = {"extractVignette(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/Entity;)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$vignette(GuiGraphicsExtractor g, Entity entity, CallbackInfo ci) {
        if (ClearViewModule.hideVignette()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$texture(GuiGraphicsExtractor g, Identifier texture, float alpha, CallbackInfo ci) {
        if (ClearViewModule.hideTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"extractPortalOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$portal(GuiGraphicsExtractor g, float strength, CallbackInfo ci) {
        if (ClearViewModule.hidePortal()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"extractConfusionOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fokus$nausea(GuiGraphicsExtractor g, float strength, CallbackInfo ci) {
        if (ClearViewModule.hideNausea()) {
            ci.cancel();
        }
    }
}
