package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.TextSetting;

/** Chat lines with words you do not want to see never reach the chat: ads, spam, announcements. */
public class ChatFilterModule extends Module {
    public final TextSetting words = this.add(new TextSetting("Wörter (mit Komma)", "discord.gg, kaufe jetzt", 200));
    public final BoolSetting wholeWords = this.add(new BoolSetting("Nur ganze Wörter", false));

    /** The patterns of the last word list, rebuilt only when the list changes. */
    private String builtFrom;
    private List<Pattern> patterns = List.of();

    public ChatFilterModule() {
        super("Chat-Filter", "Zeilen mit bestimmten Wörtern verschwinden aus dem Chat (z. B. Werbung).", Category.HUD);
    }

    private List<Pattern> patterns() {
        String list = this.words.get() + "|" + this.wholeWords.on();
        if (!list.equals(this.builtFrom)) {
            List<Pattern> out = new ArrayList<>();
            for (String w : this.words.get().split(",")) {
                String word = w.trim().toLowerCase(Locale.ROOT);
                if (!word.isEmpty()) {
                    String q = Pattern.quote(word);
                    out.add(Pattern.compile(this.wholeWords.on() ? "(?<![\\p{L}\\p{N}_])" + q + "(?![\\p{L}\\p{N}_])" : q));
                }
            }
            this.patterns = out;
            this.builtFrom = list;
        }
        return this.patterns;
    }

    /** Whether the line is left out of the chat. */
    public static boolean hides(String text) {
        ChatFilterModule m = Modules.get(ChatFilterModule.class);
        if (m == null || !m.isEnabled() || text == null) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (Pattern p : m.patterns()) {
            if (p.matcher(lower).find()) {
                return true;
            }
        }
        return false;
    }
}
