package xyz.fokus.module.impl.hud;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.Lang;
import xyz.fokus.Stats;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;

/** Kills, deaths and K/D since you joined: a kill is a target dying within two seconds of your hit. */
public class SessionStatsModule extends HudModule {
    public final BoolSetting playersOnly = this.add(new BoolSetting("Nur Spieler-Kills", true));
    public final BoolSetting ratio = this.add(new BoolSetting("K/D zeigen", true));

    public SessionStatsModule() {
        super("Session-Statistik", "Kills, Tode und K/D seit du auf dem Server bist.", 0.005F, 0.46F);
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        int k = editor ? 7 : this.playersOnly.on() ? Stats.playerKills : Stats.kills;
        int d = editor ? 3 : Stats.deaths;
        String value = k + " K  " + d + " D";
        if (this.ratio.on()) {
            float kd = d == 0 ? k : (float) k / d;
            value += String.format(Locale.ROOT, "  %.2f", kd);
        }
        this.labelValue(g, font, Lang.t("Session"), value);
    }
}
