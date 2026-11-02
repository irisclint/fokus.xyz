package xyz.fokus.module.impl.pvp;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;

/** The view stays as wide as set: sprinting, speed and slowness no longer zoom it in and out. */
public class StaticFovModule extends Module {
    public final BoolSetting bow = add(new BoolSetting("Bogen-Zoom behalten", true));
    public final BoolSetting spyglass = add(new BoolSetting("Fernrohr behalten", true));

    public StaticFovModule() {
        super("Festes Sichtfeld", "Kein Zoomen beim Sprinten, mit Schnelligkeit oder Langsamkeit.", Category.PVP);
    }

    /** The game's field-of-view factor, or 1 (unchanged view) while the module is on. */
    public static float modifier(AbstractClientPlayer player, float vanilla) {
        StaticFovModule m = Modules.get(StaticFovModule.class);
        if (m == null || !m.isEnabled()) {
            return vanilla;
        }
        if (player.isUsingItem()) {
            ItemStack using = player.getUseItem();
            boolean drawing = using.is(Items.BOW) || using.is(Items.CROSSBOW);
            if (drawing && m.bow.on() || using.is(Items.SPYGLASS) && m.spyglass.on()) {
                return vanilla;
            }
        }
        return 1.0F;
    }
}
