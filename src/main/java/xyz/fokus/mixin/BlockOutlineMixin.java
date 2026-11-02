package xyz.fokus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.fokus.module.impl.visual.BlockOutlineModule;

/** Block outline on 26.2 and 26.3: colour and width arrive as arguments of submitHitOutline. */
@Mixin(LevelRenderer.class)
public class BlockOutlineMixin {
    @ModifyVariable(method = "submitHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IFZ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int fokus$color(int color, PoseStack pose, SubmitNodeCollector collector, RenderType type,
            BlockOutlineRenderState state, int colorAgain, float width, boolean translucent) {
        return BlockOutlineModule.color(color, width);
    }

    @ModifyVariable(method = "submitHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IFZ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float fokus$width(float width, PoseStack pose, SubmitNodeCollector collector, RenderType type,
            BlockOutlineRenderState state, int color, float widthAgain, boolean translucent) {
        return BlockOutlineModule.width(color, width);
    }
}
