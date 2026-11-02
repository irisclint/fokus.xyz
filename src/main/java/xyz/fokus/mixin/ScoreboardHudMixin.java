package xyz.fokus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.fokus.module.impl.hud.ScoreboardModule;

/** The scoreboard sidebar from 26.2, where Hud draws it: each row's number, and the background behind it. */
@Pseudo
@Mixin(targets = "net.minecraft.client.gui.Hud")
public class ScoreboardHudMixin {
    @WrapOperation(method = "lambda$displayScoreboardSidebar$1(Lnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/network/chat/numbers/NumberFormat;Lnet/minecraft/world/scores/PlayerScoreEntry;)Lnet/minecraft/client/gui/Hud$1DisplayEntry;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/PlayerScoreEntry;formatValue(Lnet/minecraft/network/chat/numbers/NumberFormat;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent fokus$score(PlayerScoreEntry entry, NumberFormat format, Operation<MutableComponent> original) {
        return ScoreboardModule.hideScores() ? Component.empty() : original.call(entry, format);
    }

    @ModifyArg(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/scores/Objective;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundColor(F)I"))
    private float fokus$background(float strength) {
        return strength * ScoreboardModule.background();
    }
}
