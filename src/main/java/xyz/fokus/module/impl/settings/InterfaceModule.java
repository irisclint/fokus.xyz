package xyz.fokus.module.impl.settings;

import net.minecraft.client.Minecraft;
import xyz.fokus.compat.Compat;
import xyz.fokus.config.ConfigIO;
import xyz.fokus.gui.HudEditorScreen;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.ActionSetting;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.update.Updater;

public class InterfaceModule extends Module {
    public final ModeSetting language = this.add(new ModeSetting("Sprache", "Automatisch", "Automatisch", "English", "Deutsch"));
    public final ColorSetting accent = this.add(new ColorSetting("Akzent", -1));
    public final ColorSetting accent2 = this.add(new ColorSetting("Zweitton", -6645094));
    public final KeySetting menuKey = this.add(new KeySetting("Menü-Taste", Compat.key("KEY_RCONTROL")));
    public final NumberSetting menuScale = this.add(new NumberSetting("Menügröße", 1.0, 0.8, 1.25, 0.05, "x"));
    public final NumberSetting animSpeed = this.add(new NumberSetting("Animationstempo", 1.0, 0.5, 2.0, 0.1, "x"));
    public final NumberSetting scrollSpeed = this.add(new NumberSetting("Scrolltempo", 1.0, 0.3, 2.5, 0.1, "x"));
    public final BoolSetting blur = this.add(new BoolSetting("Unschärfe hinter dem Menü", true));
    public final BoolSetting clickSound = this.add(new BoolSetting("Klickgeräusch im Menü", true));
    public final ModeSetting hudStyle = this.add(new ModeSetting("HUD-Stil", "Glas", "Glas", "Solide", "Ohne"));
    public final NumberSetting hudOpacity = this.add(new NumberSetting("HUD-Glas Deckkraft", 40.0, 0.0, 100.0, 5.0, "%"));
    public final NumberSetting hudRadius = this.add(new NumberSetting("HUD-Ecken", 2.5, 0.0, 5.0, 0.5));
    public final BoolSetting textShadow = this.add(new BoolSetting("Textschatten im HUD", true));
    public final BoolSetting hudAccentLine = this.add(new BoolSetting("HUD-Linie unten", false));
    public final BoolSetting autoUpdate = this.add(new BoolSetting("Automatische Updates", true));
    public final ActionSetting checkUpdate = this.add(new ActionSetting("Jetzt nach Updates suchen", Updater::check));
    public final ActionSetting hudEditor = this.add(new ActionSetting("HUD-Editor öffnen", () -> {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreenAndShow(new HudEditorScreen(Compat.currentScreen()));
    }));
    public final ActionSetting resetAll = this.add(new ActionSetting("Alles zurücksetzen", () -> {
        Modules.resetAll();
        ConfigIO.save();
    }));

    public InterfaceModule() {
        super("Oberfläche", "Akzent, Menü-Taste, Animationen und HUD-Stil.", Category.SETTINGS);
        this.alwaysOn = true;
    }
}
