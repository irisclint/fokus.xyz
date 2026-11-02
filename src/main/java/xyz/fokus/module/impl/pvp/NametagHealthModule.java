package xyz.fokus.module.impl.pvp;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.util.Colors;

/** Every player's health after the name above their head. */
public class NametagHealthModule extends Module {
    public final ModeSetting unit = add(new ModeSetting("Anzeige", "Herzen", "Herzen", "Lebenspunkte"));
    public final BoolSetting absorption = add(new BoolSetting("Absorption mitzählen", true));
    public final BoolSetting mobs = add(new BoolSetting("Auch bei Mobs", false));

    public NametagHealthModule() {
        super("Leben im Namen", "Das Leben jedes Spielers hinter dem Namen über dem Kopf.", Category.PVP);
    }

    /** The name tag with the health after it, or the name tag as it was. */
    public static Component decorate(Entity entity, Component tag) {
        NametagHealthModule m = Modules.get(NametagHealthModule.class);
        if (m == null || !m.isEnabled() || tag == null || !(entity instanceof LivingEntity living)
                || !(entity instanceof Player || m.mobs.on()) || living.isDeadOrDying()) {
            return tag;
        }
        float health = living.getHealth();
        float extra = m.absorption.on() ? living.getAbsorptionAmount() : 0.0F;
        float fraction = Math.max(0.0F, Math.min(1.0F, health / Math.max(1.0F, living.getMaxHealth())));
        // Red when low, through yellow, to green when full.
        int color = fraction > 0.5F
                ? Colors.lerp(0xFFF2C94C, 0xFF55E07A, (fraction - 0.5F) * 2.0F)
                : Colors.lerp(0xFFEB5757, 0xFFF2C94C, fraction * 2.0F);
        boolean hearts = m.unit.is("Herzen");
        float shown = health + extra;
        String number = number(hearts ? shown / 2.0F : shown);
        Component value = Component.literal(" " + number + (hearts ? " ❤" : " HP")).withColor(color & 0xFFFFFF);
        if (extra > 0.0F) {
            value = Component.empty().append(value).append(Component.literal(" +").withColor(0xFFD84A));
        }
        return Component.empty().append(tag).append(value);
    }

    /** Whole numbers without ".0", halves with one place. */
    private static String number(float v) {
        float r = Math.round(v * 2.0F) / 2.0F;
        return r == Math.floor(r) ? String.valueOf((int) r) : String.format(Locale.ROOT, "%.1f", r);
    }
}
