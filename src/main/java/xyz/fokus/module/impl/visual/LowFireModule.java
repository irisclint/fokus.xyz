package xyz.fokus.module.impl.visual;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.NumberSetting;

public class LowFireModule extends Module {
    public final NumberSetting height = this.add(new NumberSetting("Absenkung", 0.3, 0.05, 0.6, 0.05));

    public LowFireModule() {
        super("Low Fire", "Das Feuer-Overlay sitzt tiefer und verdeckt weniger Sicht.", Category.VISUALS);
        this.enabledByDefault();
    }
}
