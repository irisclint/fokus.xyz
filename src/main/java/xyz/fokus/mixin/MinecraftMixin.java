package xyz.fokus.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.Events;

@Mixin({Minecraft.class})
public class MinecraftMixin {
    @Inject(
        method = {"tick"},
        at = {@At("TAIL")}
    )
    private void fokus$tick(CallbackInfo ci) {
        Events.tick((Minecraft)(Object)this);
    }
}
