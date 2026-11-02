package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class ArmorModule extends HudModule {
    public final ModeSetting text = this.add(new ModeSetting("Anzeige", "Prozent", "Prozent", "Haltbarkeit", "Nur Icons"));
    public final BoolSetting mainHand = this.add(new BoolSetting("Item in der Hand", true));
    public final BoolSetting horizontal = this.add(new BoolSetting("Waagerecht", false));
    public final BoolSetting colored = this.add(new BoolSetting("Farbige Haltbarkeit", false));
    private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final List<ItemStack> items = new ArrayList<>();

    public ArmorModule() {
        super("Rüstung", "Deine Rüstung mit Haltbarkeit.", 1.0F, 0.4F);
        if (!Compat.isLoaded("ukus-armor-hud")) {
            this.enabledByDefault();
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        this.items.clear();
        LocalPlayer p = mc.player;
        if (p != null) {
            for (EquipmentSlot slot : SLOTS) {
                ItemStack s = p.getItemBySlot(slot);
                if (!s.isEmpty()) {
                    this.items.add(s);
                }
            }

            if (this.mainHand.on() && !p.getMainHandItem().isEmpty()) {
                this.items.add(p.getMainHandItem());
            }
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        int count = this.items.isEmpty() && editor ? 4 : this.items.size();
        if (count == 0) {
            this.w = 0.0F;
            this.h = 0.0F;
        } else {
            boolean showText = !this.text.is("Nur Icons");
            float cellW = this.horizontal.on() ? 18.0F : 18 + (showText ? 26 : 0);
            float cellH = this.horizontal.on() ? 18 + (showText ? 9 : 0) : 17.0F;
            this.w = this.horizontal.on() ? count * cellW + 4.0F : cellW + 4.0F;
            this.h = this.horizontal.on() ? cellH + 4.0F : count * cellH + 4.0F;
            this.box(g, this.w, this.h);

            for (int i = 0; i < count; i++) {
                float x = this.horizontal.on() ? 2.0F + i * cellW : 2.0F;
                float y = this.horizontal.on() ? 2.0F : 2.0F + i * cellH;
                if (i >= this.items.size()) {
                    R2D.round(g, x + 2.0F, y + 2.0F, 12.0F, 12.0F, 2.0F, 587202559);
                } else {
                    ItemStack s = this.items.get(i);
                    Matrix3x2fStack pose = g.pose();
                    pose.pushMatrix();
                    pose.translate(x + 1.0F, y);
                    g.item(s, 0, 0);
                    if (s.getCount() > 1) {
                        g.itemDecorations(font, s, 0, 0);
                    }

                    pose.popMatrix();
                    if (showText && s.isDamageableItem()) {
                        int max = s.getMaxDamage();
                        int left = max - s.getDamageValue();
                        float frac = max == 0 ? 1.0F : (float)left / max;
                        int c;
                        if (this.colored.on()) {
                            c = frac > 0.5F ? Colors.lerp(-1650054, -6502492, (frac - 0.5F) * 2.0F) : Colors.lerp(-2714990, -1650054, frac * 2.0F);
                        } else {
                            c = frac > 0.25F ? this.text() : this.label();
                        }

                        String v = this.text.is("Prozent") ? Math.round(frac * 100.0F) + "%" : String.valueOf(left);
                        if (this.horizontal.on()) {
                            R2D.textCentered(g, font, v, x + 9.0F, y + 18.0F, 0.7F, c, Theme.shadow());
                        } else {
                            R2D.text(g, font, v, x + 20.0F, y + 4.5F, c, Theme.shadow());
                        }
                    }
                }
            }
        }
    }
}
