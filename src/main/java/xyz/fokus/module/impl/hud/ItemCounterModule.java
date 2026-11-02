package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.util.R2D;

public class ItemCounterModule extends HudModule {
    public final BoolSetting crystals = this.add(new BoolSetting("Endkristalle", true));
    public final BoolSetting obsidian = this.add(new BoolSetting("Obsidian", true));
    public final BoolSetting totems = this.add(new BoolSetting("Totems", true));
    public final BoolSetting gapples = this.add(new BoolSetting("Goldene Äpfel", true));
    public final BoolSetting pearls = this.add(new BoolSetting("Enderperlen", true));
    public final BoolSetting anchors = this.add(new BoolSetting("Seelenanker + Glowstone", false));
    public final BoolSetting xp = this.add(new BoolSetting("Erfahrungsfläschchen", true));
    public final BoolSetting potions = this.add(new BoolSetting("Wurftränke", false));
    public final BoolSetting arrows = this.add(new BoolSetting("Pfeile", false));
    private final List<ItemCounterModule.Entry> entries = new ArrayList<>();

    public ItemCounterModule() {
        super("Item-Zähler", "Kristalle, Obsidian, Totems, Äpfel, Perlen, XP - wie viele du dabei hast.", 1.0F, 0.72F);
        this.enabledByDefault();
    }

    @Override
    public void onTick(Minecraft mc) {
        this.entries.clear();
        LocalPlayer p = mc.player;
        if (p != null) {
            Inventory inv = p.getInventory();
            this.count(inv, this.crystals.on(), Items.END_CRYSTAL);
            this.count(inv, this.obsidian.on(), Items.OBSIDIAN);
            this.count(inv, this.totems.on(), Items.TOTEM_OF_UNDYING);
            this.count(inv, this.gapples.on(), Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
            this.count(inv, this.pearls.on(), Items.ENDER_PEARL);
            this.count(inv, this.anchors.on(), Items.RESPAWN_ANCHOR);
            this.count(inv, this.anchors.on(), Items.GLOWSTONE);
            this.count(inv, this.xp.on(), Items.EXPERIENCE_BOTTLE);
            this.count(inv, this.potions.on(), Items.SPLASH_POTION);
            this.count(inv, this.arrows.on(), Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW);
        }
    }

    private void count(Inventory inv, boolean on, Item... items) {
        if (on) {
            int n = 0;

            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty()) {
                    for (Item item : items) {
                        if (s.is(item)) {
                            n += s.getCount();
                            break;
                        }
                    }
                }
            }

            if (n > 0) {
                this.entries.add(new ItemCounterModule.Entry(new ItemStack(items[0]), n));
            }
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        int rows = this.entries.isEmpty() && editor ? 3 : this.entries.size();
        if (rows == 0) {
            this.w = 0.0F;
            this.h = 0.0F;
        } else {
            float maxText = font.width("64");

            for (ItemCounterModule.Entry e : this.entries) {
                maxText = Math.max(maxText, (float)font.width(String.valueOf(e.count)));
            }

            this.w = 22.0F + maxText + 5.0F;
            this.h = rows * 17 + 3;
            this.box(g, this.w, this.h);

            for (int i = 0; i < rows; i++) {
                float y = 2 + i * 17;
                if (i >= this.entries.size()) {
                    R2D.round(g, 5.0F, y + 2.0F, 12.0F, 12.0F, 2.0F, 587202559);
                    R2D.text(g, font, "0", 22.0F, y + 4.5F, this.label(), Theme.shadow());
                } else {
                    ItemCounterModule.Entry e = this.entries.get(i);
                    Matrix3x2fStack pose = g.pose();
                    pose.pushMatrix();
                    pose.translate(3.0F, y);
                    g.item(e.icon, 0, 0);
                    pose.popMatrix();
                    R2D.text(g, font, String.valueOf(e.count), 22.0F, y + 4.5F, this.text(), Theme.shadow());
                }
            }
        }
    }

    private record Entry(ItemStack icon, int count) {
    }
}
