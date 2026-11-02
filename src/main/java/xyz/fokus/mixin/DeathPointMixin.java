package xyz.fokus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.hud.DeathPointModule;

/**
 * The death packet, at the end of its handler: the network thread's pass leaves the method early
 * (handed to the game thread), so this runs once, on the game thread, with the player still in place.
 */
@Mixin(ClientPacketListener.class)
public class DeathPointMixin {
    @Inject(method = "handlePlayerCombatKill", at = @At("TAIL"))
    private void fokus$died(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && packet.playerId() == mc.player.getId()) {
            DeathPointModule.onDeath(mc);
        }
    }
}
