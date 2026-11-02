package xyz.fokus.module.impl.anim;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ModeSetting;

public class EquipModule extends Module {
    public final ModeSetting mode = this.add(new ModeSetting("Modus", "Kein Dip", "Kein Dip", "Halber Dip"));

    public EquipModule() {
        super("Kein Wechsel-Dip", "Das Item senkt sich nicht beim Wechseln und nach Schlägen.", Category.ANIMATIONS);
    }

    public float apply(float equip) {
        return this.mode.is("Halber Dip") ? equip * 0.5F : 0.0F;
    }
}
