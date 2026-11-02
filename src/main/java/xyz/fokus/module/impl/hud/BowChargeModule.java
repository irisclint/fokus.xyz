package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

/** How far a bow, crossbow or trident is drawn: a short bar under the crosshair, green when full. */
public class BowChargeModule extends HudModule {
    /** The bow is fully drawn after a second (the game's own: draw = ticks / 20). */
    private static final float BOW_TICKS = 20.0F;
    private static final float WIDTH = 40.0F;

    public final BoolSetting percent = this.add(new BoolSetting("Prozent zeigen", false));
    public final ColorSetting full = this.add(new ColorSetting("Farbe wenn voll", 0xFF55E07A));

    public BowChargeModule() {
        super("Bogen-Anzeige", "Ein Balken unter dem Fadenkreuz: wie weit Bogen, Armbrust oder Dreizack gespannt sind.", 0.5F, 0.56F);
        this.centered = true;
    }

    /** 0..1 while drawing, -1 when nothing is drawn. */
    private static float charge(LocalPlayer p) {
        if (p == null || !p.isUsingItem()) {
            return -1.0F;
        }
        ItemStack stack = p.getUseItem();
        int ticks = p.getTicksUsingItem();
        if (stack.is(Items.BOW)) {
            return Math.min(1.0F, ticks / BOW_TICKS);
        }
        if (stack.is(Items.CROSSBOW) && !CrossbowItem.isCharged(stack)) {
            return Math.min(1.0F, ticks / (float) Math.max(1, CrossbowItem.getChargeDuration(stack, p)));
        }
        if (stack.is(Items.TRIDENT)) {
            return Math.min(1.0F, ticks / (float) TridentItem.THROW_THRESHOLD_TIME);
        }
        return -1.0F;
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        float c = editor ? 0.7F : charge(Minecraft.getInstance().player);
        if (c < 0.0F) {
            this.w = 0.0F;
            this.h = 0.0F;
            return;
        }
        this.w = WIDTH;
        this.h = this.percent.on() ? 10.0F : 4.0F;
        boolean ready = c >= 1.0F;
        int fill = ready ? this.full.color() | 0xFF000000 : Colors.lerp(this.label(), this.text(), c);
        R2D.round(g, 0.0F, 0.0F, WIDTH, 3.0F, 1.5F, Colors.alpha(0xFF000000, 0.45F));
        if (c > 0.02F) {
            R2D.round(g, 0.0F, 0.0F, Math.max(3.0F, WIDTH * c), 3.0F, 1.5F, fill);
        }
        if (this.percent.on()) {
            R2D.textCentered(g, font, Math.round(c * 100.0F) + "%", WIDTH / 2.0F, 4.5F, 0.6F, fill, true);
        }
    }
}
