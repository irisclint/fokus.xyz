package xyz.fokus.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.fokus.util.TintableOverlay;

/**
 * The 16x16 texture behind the flash of a hurt entity: the top eight rows are the hurt colour
 * (vanilla: red at 70%), the bottom eight the white flash of creepers and the like.
 */
@Mixin(OverlayTexture.class)
public class OverlayTextureMixin implements TintableOverlay {
    @Shadow
    @Final
    private DynamicTexture texture;

    @Override
    public void fokus$tint(int argb) {
        NativeImage pixels = this.texture.getPixels();
        if (pixels == null) {
            return;
        }
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++) {
                pixels.setPixel(x, y, argb);
            }
        }
        this.texture.upload();
    }
}
