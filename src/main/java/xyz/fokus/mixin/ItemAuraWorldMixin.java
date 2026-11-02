package xyz.fokus.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.visual.ItemAuraModule;

/**
 * The aura in the world: dropped items and thrown ones (a pearl in flight) get the game's own
 * glowing outline, in the aura's colours. Only on the client's copy of the world.
 */
@Mixin(Entity.class)
public class ItemAuraWorldMixin {
    private static ItemStack fokus$item(Object self) {
        if (self instanceof ItemEntity item) {
            return item.getItem();
        }
        if (self instanceof ItemSupplier supplier) {
            return supplier.getItem();
        }
        return null;
    }

    private boolean fokus$auraHere() {
        Entity self = (Entity) (Object) this;
        return self.level().isClientSide() && ItemAuraModule.glows(fokus$item(self));
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void fokus$glow(CallbackInfoReturnable<Boolean> cir) {
        if (fokus$auraHere()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void fokus$color(CallbackInfoReturnable<Integer> cir) {
        if (fokus$auraHere()) {
            cir.setReturnValue(ItemAuraModule.glowColor());
        }
    }
}
