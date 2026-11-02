package xyz.fokus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class FokusButton extends AbstractWidget {
    private final Screen parent;
    private final Anim hover = new Anim(0.0F, 16.0F);

    public FokusButton(Screen parent, int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("fokus.xyz"));
        this.parent = parent;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        float h = this.hover.to(this.isHoveredOrFocused() ? 1.0F : 0.0F).get();
        float x = this.getX();
        float y = this.getY();
        float w = this.getWidth();
        float ht = this.getHeight();
        R2D.round(g, x, y, w, ht, 4.0F, Colors.lerp(-771094006, -535291880, h));
        R2D.roundEdge(g, x, y, w, ht, 4.0F, Colors.lerp(822083583, 1895825407, h));
        Font font = Minecraft.getInstance().font;
        float word = font.width("fokus");
        float total = word + font.width(".xyz");
        float tx = x + w / 2.0F - total / 2.0F;
        float ty = y + ht / 2.0F - 4.0F;
        R2D.text(g, font, "fokus", tx, ty, -723724, false);
        R2D.text(g, font, ".xyz", tx + word, ty, Colors.lerp(-6052957, -3158065, h), false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        Minecraft.getInstance().setScreenAndShow(new FokusScreen(this.parent));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
