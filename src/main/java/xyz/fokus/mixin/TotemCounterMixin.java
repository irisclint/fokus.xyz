package xyz.fokus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.hud.TotemPopsModule;

@Mixin({ClientPacketListener.class})
public class TotemCounterMixin {
    @Inject(
        method = {"handleEntityEvent"},
        at = {@At(
            value = "INVOKE",
            shift = Shift.AFTER,
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V"
        )}
    )
    private void fokus$countPop(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (packet.getEventId() == 35 && mc.level != null) {
            Entity e = packet.getEntity(mc.level);
            if (e != null) {
                TotemPopsModule.onPop(e);
            }
        }
    }
}
