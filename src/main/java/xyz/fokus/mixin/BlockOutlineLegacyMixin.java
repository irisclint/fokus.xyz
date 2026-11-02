package xyz.fokus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.fokus.module.impl.visual.BlockOutlineModule;

/** Block outline on 26.1: colour and width arrive as arguments of renderHitOutline. */
@Mixin(LevelRenderer.class)
public class BlockOutlineLegacyMixin {
    @ModifyVariable(method = "renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int fokus$color(int color, PoseStack pose, VertexConsumer buffer, double x, double y, double z,
            BlockOutlineRenderState state, int colorAgain, float width) {
        return BlockOutlineModule.color(color, width);
    }

    @ModifyVariable(method = "renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float fokus$width(float width, PoseStack pose, VertexConsumer buffer, double x, double y, double z,
            BlockOutlineRenderState state, int color, float widthAgain) {
        return BlockOutlineModule.width(color, width);
    }
}
