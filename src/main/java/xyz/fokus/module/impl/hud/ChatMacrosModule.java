package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.TextSetting;

/** A key sends a line you set: "gg", "/spawn", "/party accept" - a command when it starts with "/". */
public class ChatMacrosModule extends Module {
    private static final int MACROS = 3;
    /** At most one line a second: servers kick for spam. */
    private static final long GAP = 1000L;

    private final KeySetting[] keys = new KeySetting[MACROS];
    private final TextSetting[] texts = new TextSetting[MACROS];
    private long lastSent;

    public ChatMacrosModule() {
        super("Chat-Makros", "Eine Taste schreibt einen festen Text in den Chat, z. B. „gg“ oder „/spawn“.", Category.HUD);
        String[] defaults = {"gg", "/spawn", ""};
        for (int i = 0; i < MACROS; i++) {
            this.keys[i] = this.add(new KeySetting("Makro " + (i + 1) + " Taste", -1));
            this.texts[i] = this.add(new TextSetting("Makro " + (i + 1) + " Text", defaults[i], 100));
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        for (int i = 0; i < MACROS; i++) {
            // Every key is read each tick, so a press in a menu does not fire once it closes.
            if (this.keys[i].pressed() && Compat.currentScreen() == null) {
                send(mc, this.texts[i].get());
            }
        }
    }

    /** Sends the line: a command without its "/", anything else as chat. */
    boolean send(Minecraft mc, String line) {
        ClientPacketListener connection = mc.getConnection();
        String text = line.trim();
        long now = System.currentTimeMillis();
        if (connection == null || text.isEmpty() || now - this.lastSent < GAP) {
            return false;
        }
        this.lastSent = now;
        if (text.startsWith("/")) {
            connection.sendCommand(text.substring(1));
        } else {
            connection.sendChat(text);
        }
        return true;
    }
}
