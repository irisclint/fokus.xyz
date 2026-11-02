package xyz.fokus.module.impl.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;

/** The time in front of every chat message, as it arrives. */
public class ChatTimestampModule extends Module {
    public final ModeSetting format = add(new ModeSetting("Format", "14:05", "14:05", "14:05:09", "2:05 PM"));
    public final ModeSetting brackets = add(new ModeSetting("Klammern", "[ ]", "[ ]", "( )", "< >", "Keine"));
    public final ColorSetting color = add(new ColorSetting("Farbe", 0xFF8A8A8A));

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter AMPM = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public ChatTimestampModule() {
        super("Chat-Zeitstempel", "Die Uhrzeit vor jeder Chat-Nachricht.", Category.HUD);
    }

    /** The message with the time in front, or the message itself while the module is off. */
    public static Component stamp(Component message) {
        ChatTimestampModule m = Modules.get(ChatTimestampModule.class);
        if (m == null || !m.isEnabled() || message == null) {
            return message;
        }
        String time = LocalTime.now().format(m.format.is("14:05:09") ? LONG : m.format.is("2:05 PM") ? AMPM : SHORT);
        String text = switch (m.brackets.get()) {
            case "( )" -> "(" + time + ")";
            case "< >" -> "<" + time + ">";
            case "Keine" -> time;
            default -> "[" + time + "]";
        };
        return Component.empty()
                .append(Component.literal(text + " ").withColor(m.color.color() & 0xFFFFFF))
                .append(message);
    }
}
