package xyz.fokus.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.gui.FokusButton;

@Mixin({PauseScreen.class})
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(
        method = {"init"},
        at = {@At("TAIL")}
    )
    private void fokus$addButton(CallbackInfo ci) {
        if (((PauseScreen)(Object)this).showsPauseMenu()) {
            int minX = Integer.MAX_VALUE;
            int maxRight = Integer.MIN_VALUE;
            int maxBottom = Integer.MIN_VALUE;

            for (GuiEventListener child : this.children()) {
                if (child instanceof AbstractWidget w && w.visible && w.getWidth() >= 90) {
                    minX = Math.min(minX, w.getX());
                    maxRight = Math.max(maxRight, w.getRight());
                    maxBottom = Math.max(maxBottom, w.getBottom());
                }
            }

            int w = 204;
            int h = 20;
            int y;
            int x;
            if (maxBottom == Integer.MIN_VALUE) {
                x = this.width / 2 - w / 2;
                y = this.height / 4 + 120;
            } else {
                w = Math.max(98, maxRight - minX);
                x = minX;
                y = maxBottom + 8;
            }

            if (y + h > this.height - 4) {
                w = 110;
                x = 6;
                y = 6;
            }

            this.addRenderableWidget(new FokusButton(this, x, y, w, h));
        }
    }
}
