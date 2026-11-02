package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import xyz.fokus.Lang;
import xyz.fokus.compat.Compat;
import xyz.fokus.lyrics.MediaKeys;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.KeySetting;

/** Play, pause and skip whatever music is playing, without leaving the game. */
public class MediaControlModule extends Module {
    public final KeySetting playPause = this.add(new KeySetting("Play/Pause", Compat.key("KEY_F8")));
    public final KeySetting next = this.add(new KeySetting("Nächster Song", Compat.key("KEY_F9")));
    public final KeySetting previous = this.add(new KeySetting("Vorheriger Song", Compat.key("KEY_F7")));

    public MediaControlModule() {
        super("Musik-Steuerung", "Musik pausieren und Songs wechseln, ohne das Spiel zu verlassen (Windows).", Category.HUD);
    }

    @Override
    public void onTick(Minecraft mc) {
        // Read every key each tick, so a press in a menu does not count once the menu closes.
        boolean pp = this.playPause.pressed();
        boolean nx = this.next.pressed();
        boolean pv = this.previous.pressed();
        if (Compat.currentScreen() != null) {
            return;
        }
        if (pp) {
            this.send(MediaKeys.PLAY_PAUSE, "⏯ " + Lang.t("Play/Pause"));
        } else if (nx) {
            this.send(MediaKeys.NEXT, "⏭ " + Lang.t("Nächster Song"));
        } else if (pv) {
            this.send(MediaKeys.PREVIOUS, "⏮ " + Lang.t("Vorheriger Song"));
        }
    }

    private void send(int key, String label) {
        MediaKeys.pressLater(key);
        Compat.actionBar(Component.literal(label));
    }
}
