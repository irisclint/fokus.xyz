package xyz.fokus.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.SkyModule;
import xyz.fokus.util.Colors;

@Mixin({FogRenderer.class})
public class FogMixin {
    @Inject(
        method = {"computeFogColor"},
        at = {@At("TAIL")}
    )
    private void fokus$fog(Camera camera, float partial, ClientLevel level, int renderDistance, float darken, Vector4f color, CallbackInfo ci) {
        SkyModule m = Modules.get(SkyModule.class);
        if (m != null && m.isEnabled() && m.fog.on() && camera.getFluidInCamera() == FogType.NONE) {
            int c = m.fogColor.color(0.2F);
            if (m.dayNight.on()) {
                c = Colors.scale(c, Math.max(color.x, Math.max(color.y, color.z)));
            }

            color.set(Colors.r(c) / 255.0F, Colors.g(c) / 255.0F, Colors.b(c) / 255.0F, color.w);
        }
    }
}
