package xyz.fokus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.VersionHooks;

@Mixin({ScreenEffectRenderer.class})
public class FireOverlayLegacyMixin {
    @Inject(
        method = {"renderFire"},
        at = {@At("HEAD")}
    )
    private static void fokus$fire(PoseStack pose, @Coerce Object buffers, TextureAtlasSprite sprite, CallbackInfo ci) {
        VersionHooks.fireStart(pose);
    }

    @Inject(
        method = {"renderFire"},
        at = {@At("RETURN")}
    )
    private static void fokus$fireEnd(PoseStack pose, @Coerce Object buffers, TextureAtlasSprite sprite, CallbackInfo ci) {
        VersionHooks.fireEnd(pose);
    }
}
