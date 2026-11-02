package xyz.fokus.module.impl.hud;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.Lang;
import xyz.fokus.Stats;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.NumberSetting;

/** How long the server's combat tag still runs: counted down from the last hit, given or taken. */
public class CombatTimerModule extends HudModule {
    public final NumberSetting duration = this.add(new NumberSetting("Dauer", 15.0, 5.0, 60.0, 1.0, " s"));
    public final BoolSetting playersOnly = this.add(new BoolSetting("Nur Treffer gegen Spieler", true));
    public final BoolSetting whenHurt = this.add(new BoolSetting("Auch wenn du getroffen wirst", true));

    public CombatTimerModule() {
        super("Kampf-Timer", "Zählt den Combat-Tag herunter: so lange nicht ausloggen.", 0.5F, 0.62F);
        this.centered = true;
    }

    /** Milliseconds of the tag left, 0 when out of combat. */
    private long left() {
        long start = this.playersOnly.on() ? Stats.lastHitPlayerAt : Stats.lastHitAnyAt;
        if (this.whenHurt.on()) {
            start = Math.max(start, Stats.lastHurtAt);
        }
        if (start == 0L) {
            return 0L;
        }
        return Math.max(0L, Math.round(this.duration.get() * 1000.0) - (System.currentTimeMillis() - start));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        long left = editor ? 11400L : this.left();
        if (left <= 0L) {
            this.w = 0.0F;
            this.h = 0.0F;
            return;
        }
        this.labelValue(g, font, Lang.t("Kampf"), String.format(Locale.ROOT, "%.1f s", left / 1000.0));
    }
}
