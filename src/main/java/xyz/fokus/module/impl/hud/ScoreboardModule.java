package xyz.fokus.module.impl.hud;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.NumberSetting;

/** The server's sidebar: without the red numbers, and as see-through as you like. */
public class ScoreboardModule extends Module {
    public final BoolSetting hideScores = this.add(new BoolSetting("Punkte ausblenden", true));
    public final NumberSetting background = this.add(new NumberSetting("Hintergrund", 100.0, 0.0, 100.0, 5.0, "%"));

    public ScoreboardModule() {
        super("Scoreboard", "Die Seitenleiste ohne rote Zahlen und mit durchsichtigem Hintergrund.", Category.HUD);
    }

    private static ScoreboardModule active() {
        ScoreboardModule m = Modules.get(ScoreboardModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    public static boolean hideScores() {
        ScoreboardModule m = active();
        return m != null && m.hideScores.on();
    }

    /** The game's background strength times this, 1 while the module is off. */
    public static float background() {
        ScoreboardModule m = active();
        return m == null ? 1.0F : m.background.f() / 100.0F;
    }
}
