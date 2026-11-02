package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;

/** The tab list shows every player's ping as a number instead of five bars. */
public class TabPingModule extends Module {
    public final BoolSetting unit = add(new BoolSetting("Mit „ms“", false));
    public final BoolSetting colored = add(new BoolSetting("Nach Ping färben", true));
    public final ColorSetting color = add(new ColorSetting("Farbe", 0xFFE0E0E0));

    /** The vanilla icon is 10px wide, in a 13px slot. */
    private static final int ICON = 10;

    public TabPingModule() {
        super("Tab-Ping", "Der Ping in der Tab-Liste als Zahl statt Balken.", Category.HUD);
    }

    public static TabPingModule active() {
        TabPingModule m = Modules.get(TabPingModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    private String text(int latency) {
        if (latency < 0) {
            return "?";
        }
        return unit.on() ? latency + "ms" : String.valueOf(latency);
    }

    private int colorOf(int latency) {
        if (!colored.on()) {
            return color.color() | 0xFF000000;
        }
        if (latency < 0) {
            return 0xFF8A8A8A;
        }
        if (latency < 80) {
            return 0xFF55E07A;
        }
        if (latency < 150) {
            return 0xFFB8E055;
        }
        if (latency < 300) {
            return 0xFFF2C94C;
        }
        return latency < 600 ? 0xFFF2994A : 0xFFEB5757;
    }

    /** The slot the tab list keeps for the ping: wide enough for the longest number shown. */
    public static int slot(int vanilla) {
        TabPingModule m = active();
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (m == null || connection == null) {
            return vanilla;
        }
        int widest = 0;
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            widest = Math.max(widest, mc.font.width(m.text(info.getLatency())));
        }
        return vanilla - ICON + widest + 3;
    }

    /** Draws the number where the icon would be; false leaves the icon to the game. */
    public static boolean draw(GuiGraphicsExtractor g, int width, int x, int y, PlayerInfo info) {
        TabPingModule m = active();
        if (m == null) {
            return false;
        }
        Font font = Minecraft.getInstance().font;
        int latency = info.getLatency();
        String s = m.text(latency);
        g.text(font, s, x + width - 1 - font.width(s), y, m.colorOf(latency), true);
        return true;
    }
}
