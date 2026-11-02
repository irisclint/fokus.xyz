package xyz.fokus.module.impl.visual;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;

public class ClearViewModule extends Module {
    public final BoolSetting pumpkin = this.add(new BoolSetting("Kürbis-Overlay aus", true));
    public final BoolSetting vignette = this.add(new BoolSetting("Vignette aus", true));
    public final BoolSetting portal = this.add(new BoolSetting("Portal-Effekt aus", false));
    public final BoolSetting nausea = this.add(new BoolSetting("Übelkeit-Verzerrung aus", true));
    public final BoolSetting powderSnow = this.add(new BoolSetting("Pulverschnee-Rand aus", false));
    public final BoolSetting blindness = this.add(new BoolSetting("Blindheit-Nebel aus", false));
    public final BoolSetting darkness = this.add(new BoolSetting("Dunkelheit-Nebel aus", false));

    public ClearViewModule() {
        super("Klare Sicht", "Kürbis-Overlay, dunkler Bildrand, Portal- und Übelkeits-Effekt weg.", Category.VISUALS);
    }

    private static ClearViewModule on() {
        ClearViewModule m = Modules.get(ClearViewModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    public static boolean hideVignette() {
        ClearViewModule m = on();
        return m != null && m.vignette.on();
    }

    public static boolean hidePortal() {
        ClearViewModule m = on();
        return m != null && m.portal.on();
    }

    public static boolean hideNausea() {
        ClearViewModule m = on();
        return m != null && m.nausea.on();
    }

    /** Whether the fog of this effect (blindness, the warden's darkness) is left out. */
    public static boolean hideEffectFog(Holder<MobEffect> effect) {
        ClearViewModule m = on();
        return m != null && (m.blindness.on() && effect.is(MobEffects.BLINDNESS)
                || m.darkness.on() && effect.is(MobEffects.DARKNESS));
    }

    public static boolean hideTexture(Identifier texture) {
        ClearViewModule m = on();
        if (m != null && texture != null) {
            String path = texture.getPath();
            return m.pumpkin.on() && path.contains("pumpkinblur") || m.powderSnow.on() && path.contains("powder_snow");
        } else {
            return false;
        }
    }
}
