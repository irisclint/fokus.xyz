package xyz.fokus.module.impl.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.NumberSetting;

/** More hit particles where they help in a fight, none where they only block the view. */
public class ParticlesModule extends Module {
    public final NumberSetting crits = this.add(new NumberSetting("Krit-Partikel", 1.0, 1.0, 5.0, 1.0, "x"));
    public final BoolSetting alwaysSharp = this.add(new BoolSetting("Immer Schärfe-Partikel", false));
    public final BoolSetting noExplosions = this.add(new BoolSetting("Explosions-Partikel aus", false));
    public final BoolSetting noTotem = this.add(new BoolSetting("Totem-Partikel aus", false));

    public ParticlesModule() {
        super("Partikel", "Mehr Treffer-Partikel, keine Explosions- oder Totem-Wolken.", Category.VISUALS);
    }

    private static ParticlesModule active() {
        ParticlesModule m = Modules.get(ParticlesModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    /** Whether a particle of this type is left out altogether. */
    public static boolean hide(ParticleType<?> type) {
        ParticlesModule m = active();
        if (m == null) {
            return false;
        }
        return m.noExplosions.on() && (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER)
                || m.noTotem.on() && type == ParticleTypes.TOTEM_OF_UNDYING;
    }

    /** How many emitters a critical or sharpness hit gets (1 = the game's own). */
    public static int hitCopies(ParticleType<?> type) {
        ParticlesModule m = active();
        if (m == null || type != ParticleTypes.CRIT && type != ParticleTypes.ENCHANTED_HIT) {
            return 1;
        }
        return Math.max(1, (int) Math.round(m.crits.get()));
    }

    /** Every hit you land, with or without sharpness, shows its sparks. */
    public static void onAttack(Entity target) {
        ParticlesModule m = active();
        if (m != null && m.alwaysSharp.on()) {
            Minecraft.getInstance().particleEngine.createTrackingEmitter(target, ParticleTypes.ENCHANTED_HIT);
        }
    }
}
