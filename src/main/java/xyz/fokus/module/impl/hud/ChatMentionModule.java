package xyz.fokus.module.impl.hud;

import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.NumberSetting;

/** A chat line that names you stands out, with a ping. */
public class ChatMentionModule extends Module {
    public final ColorSetting color = this.add(new ColorSetting("Farbe", 0xFFF2C94C));
    public final BoolSetting sound = this.add(new BoolSetting("Ton", true));
    public final NumberSetting volume = this.add(new NumberSetting("Lautstärke", 0.8, 0.1, 1.0, 0.05, ""));

    /** A player's own line starts with their name ("<Name> text", "Name: text", "[Rank] Name » text"). */
    // After a [rank] the sender needs <angle brackets> or a separator ("[VIP] Name » hi"); without one,
    // "[Bob] Name hilf mir" is Bob talking about Name.
    private static final Pattern SENDER = Pattern.compile("^\\s*(?:(?:\\[[^\\]]{0,24}\\]\\s*)+(?:<([A-Za-z0-9_]{1,16})>|([A-Za-z0-9_]{1,16})\\s*(?::|»|>>|\\|))"
            + "|<?([A-Za-z0-9_]{1,16})>?\\s*(?::|»|>>|\\|)?\\s)");
    private static final Pattern BRACKET = Pattern.compile("^\\s*\\[([^\\]]{1,24})\\]");
    private long lastPing;

    public ChatMentionModule() {
        super("Chat-Erwähnungen", "Nennt dich jemand im Chat, wird die Zeile hervorgehoben und es pingt.", Category.HUD);
    }

    /** The line, highlighted when it names the player (not in their own lines), or as it was. */
    public static Component mark(Component message, String text) {
        ChatMentionModule m = Modules.get(ChatMentionModule.class);
        Minecraft mc = Minecraft.getInstance();
        if (m == null || !m.isEnabled() || mc.getUser() == null) {
            return message;
        }
        String name = mc.getUser().getName();
        if (!names(text, name)) {
            return message;
        }
        long now = System.currentTimeMillis();
        if (m.sound.on() && now - m.lastPing > 1000L) {
            m.lastPing = now;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 1.6F, m.volume.f()));
        }
        int c = m.color.color() & 0xFFFFFF;
        return Component.empty().append(Component.literal("» ").withColor(c)).append(message.copy().withColor(c));
    }

    private static String firstNonNull(String... values) {
        for (String v : values) {
            if (v != null) {
                return v;
            }
        }
        return "";
    }

    /** Whether the text names the player as a word, leaving out the sender's own name at the start. */
    static boolean names(String text, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        var sender = SENDER.matcher(text);
        if (sender.find() && name.equalsIgnoreCase(firstNonNull(sender.group(1), sender.group(2), sender.group(3)))) {
            // Your own message: what you wrote about yourself is no mention.
            return false;
        }
        // "[Name] text" (/say, some servers): the name in the first brackets is the sender too.
        var bracket = BRACKET.matcher(text);
        if (bracket.find() && bracket.group(1).trim().equalsIgnoreCase(name)) {
            return false;
        }
        Pattern word = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);
        return word.matcher(text).find();
    }
}
