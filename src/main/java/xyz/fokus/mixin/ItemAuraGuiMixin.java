package xyz.fokus.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.visual.ItemAuraModule;

/** Every item icon in a GUI - hotbar, inventory, chests - is drawn through this one method. */
@Mixin(GuiGraphicsExtractor.class)
public class ItemAuraGuiMixin {
    @Inject(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"))
    private void fokus$aura(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
        ItemAuraModule.drawBehind((GuiGraphicsExtractor) (Object) this, stack, x, y);
    }
}
