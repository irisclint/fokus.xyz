package xyz.fokus.module.impl.anim;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Ease;

public class InventoryAnimModule extends Module {
    public final ModeSetting style = this.add(new ModeSetting("Stil", "Pop", "Pop", "Zoom", "Von unten", "Von oben"));
    public final NumberSetting duration = this.add(new NumberSetting("Dauer", 220.0, 80.0, 600.0, 10.0, "ms"));

    public InventoryAnimModule() {
        super("Inventar-Animation", "Inventare und Kisten öffnen mit einer flüssigen Animation.", Category.ANIMATIONS);
        this.enabledByDefault();
    }

    public float[] transform(long elapsed, int screenHeight) {
        float t = (float)elapsed / this.duration.f();
        if (t >= 1.0F) {
            return null;
        } else {
            String var5 = this.style.get();

            return switch (var5) {
                case "Zoom" -> new float[]{Ease.lerp(0.6F, 1.0F, Ease.outQuart(t)), 0.0F};
                case "Von unten" -> new float[]{1.0F, (1.0F - Ease.outCubic(t)) * screenHeight * 0.25F};
                case "Von oben" -> new float[]{1.0F, -(1.0F - Ease.outCubic(t)) * screenHeight * 0.25F};
                default -> new float[]{Ease.lerp(0.85F, 1.0F, Ease.outBack(t)), 0.0F};
            };
        }
    }
}
