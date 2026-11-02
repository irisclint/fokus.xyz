package xyz.fokus.module.impl.pvp;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
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

    /**
     * The game's field-of-view factor, or 1 (unchanged view) while the module is on. The bow and the
     * spyglass keep their own zoom - only theirs, so a drawn bow under Speed does not zoom out again.
     */
    public static float modifier(AbstractClientPlayer player, float vanilla, float effectScale) {
        StaticFovModule m = Modules.get(StaticFovModule.class);
        if (m == null || !m.isEnabled()) {
            return vanilla;
        }
        if (m.spyglass.on() && player.isScoping()) {
            return vanilla;
        }
        if (m.bow.on() && player.isUsingItem() && player.getUseItem().is(Items.BOW)) {
            // The game's own bow zoom: 15% at full draw, eased in over the first second.
            float draw = Math.min(player.getTicksUsingItem() / 20.0F, 1.0F);
            return Mth.lerp(effectScale, 1.0F, 1.0F - draw * draw * 0.15F);
        }
        return 1.0F;
    }
}
