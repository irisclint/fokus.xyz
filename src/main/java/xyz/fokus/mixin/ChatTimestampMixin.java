package xyz.fokus.mixin;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.impl.hud.ChatFilterModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.fokus.module.impl.hud.ChatMentionModule;
import xyz.fokus.module.impl.hud.ChatStackModule;
import xyz.fokus.module.impl.hud.ChatTimestampModule;
import xyz.fokus.module.impl.hud.NickHiderModule;

/**
 * Every chat line - player, server and client messages - passes through addMessage. Stacking and
 * the timestamp share one handler so their order is fixed: the counter goes on the bare message,
 * the time in front of everything.
 */
@Mixin(ChatComponent.class)
public abstract class ChatTimestampMixin {
    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @Shadow
    protected abstract void refreshTrimmedMessages();

    /** The previous message as it arrived, how often it came in a row, and the line that shows it. */
    @Unique
    private String fokus$lastText;
    @Unique
    private int fokus$repeats;
    @Unique
    private Component fokus$lastShown;

    /** Filtered lines never reach the chat - nor the stacking and timestamp below. */
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void fokus$filter(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag,
            CallbackInfo ci) {
        if (message != null && ChatFilterModule.hides(message.getString())) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component fokus$decorate(Component message) {
        if (message == null) {
            return null;
        }
        String text = message.getString();
        Component shown = ChatMentionModule.mark(message, text);
        ChatStackModule stack = ChatStackModule.active();
        if (stack != null && !text.isBlank() && text.equals(fokus$lastText) && !allMessages.isEmpty()
                && allMessages.get(0).content() == fokus$lastShown) {
            allMessages.remove(0);
            refreshTrimmedMessages();
            fokus$repeats++;
            shown = Component.empty().append(shown)
                    .append(Component.literal(" (x" + fokus$repeats + ")").withColor(stack.color.color() & 0xFFFFFF));
        } else {
            fokus$repeats = 1;
        }
        fokus$lastText = text;
        shown = NickHiderModule.replace(shown);
        shown = ChatTimestampModule.stamp(shown);
        fokus$lastShown = shown;
        return shown;
    }
}
