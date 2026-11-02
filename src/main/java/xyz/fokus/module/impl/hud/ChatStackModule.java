package xyz.fokus.module.impl.hud;

import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.ColorSetting;

/** The same message twice in a row becomes one line with a counter instead of a wall of copies. */
public class ChatStackModule extends Module {
    public final ColorSetting color = add(new ColorSetting("Farbe", 0xFF8A8A8A));

    public ChatStackModule() {
        super("Chat stapeln", "Gleiche Nachrichten hintereinander werden zu einer Zeile mit Zähler.", Category.HUD);
    }

    public static ChatStackModule active() {
        ChatStackModule m = Modules.get(ChatStackModule.class);
        return m != null && m.isEnabled() ? m : null;
    }
}
