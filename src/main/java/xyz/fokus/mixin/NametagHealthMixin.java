package xyz.fokus.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.impl.hud.NickHiderModule;
import xyz.fokus.module.impl.pvp.NametagHealthModule;

/** The text above an entity's head; no renderer overrides it, players included. */
@Mixin(EntityRenderer.class)
public class NametagHealthMixin {
    @Inject(method = "getNameTag(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/Component;",
            at = @At("RETURN"), cancellable = true)
    private void fokus$health(Entity entity, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(NametagHealthModule.decorate(entity, NickHiderModule.replace(cir.getReturnValue())));
    }
}
