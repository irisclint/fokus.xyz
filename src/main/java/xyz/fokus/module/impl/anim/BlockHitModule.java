package xyz.fokus.module.impl.anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;

public class BlockHitModule extends Module {
    public final ModeSetting style = this.add(new ModeSetting("Stil", "1.7", "1.7", "Exhibition", "Sanft"));
    public final BoolSetting axes = this.add(new BoolSetting("Auch mit Äxten", false));

    public BlockHitModule() {
        super("1.7 Blockhit", "Rechtsklick mit dem Schwert zeigt die alte Block-Haltung (nur optisch).", Category.ANIMATIONS);
    }

    public boolean isBlocking(HumanoidArm arm) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (!this.isEnabled() || p == null || p.isUsingItem() || arm != p.getMainArm()) {
            return false;
        } else if (!mc.options.keyUse.isDown()) {
            return false;
        } else {
            ItemStack stack = p.getMainHandItem();
            return stack.is(ItemTags.SWORDS) || this.axes.on() && stack.is(ItemTags.AXES);
        }
    }
}
