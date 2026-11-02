package xyz.fokus.module.impl.hud;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.TextSetting;

/** Your name shown as another in chat, the tab list and your name tag: for streams and screenshots. */
public class NickHiderModule extends Module {
    public final TextSetting alias = this.add(new TextSetting("Ersatzname", "Spieler", 16));

    private String builtFor;
    private Pattern pattern;

    public NickHiderModule() {
        super("Namen verstecken", "Dein Name erscheint im Chat, in der Tab-Liste und am Kopf als Ersatzname.", Category.HUD);
    }

    /** The player's name as a whole word, any case; rebuilt when the name changes. */
    private Pattern pattern(String name) {
        if (!name.equals(this.builtFor)) {
            this.pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])", Pattern.CASE_INSENSITIVE);
            this.builtFor = name;
        }
        return this.pattern;
    }

    /**
     * The text with the name replaced, every piece keeping its own style (colours, click and hover),
     * or the text as it was when the module is off or the name is not in it.
     */
    public static Component replace(Component text) {
        NickHiderModule m = Modules.get(NickHiderModule.class);
        Minecraft mc = Minecraft.getInstance();
        if (m == null || !m.isEnabled() || text == null || mc.getUser() == null) {
            return text;
        }
        String alias = m.alias.get().trim();
        String name = mc.getUser().getName();
        if (alias.isEmpty() || name == null || name.isEmpty()) {
            return text;
        }
        Pattern p = m.pattern(name);
        if (!p.matcher(text.getString()).find()) {
            return text;
        }
        String replacement = Matcher.quoteReplacement(alias);
        MutableComponent out = Component.empty();
        text.visit((style, piece) -> {
            out.append(Component.literal(p.matcher(piece).replaceAll(replacement)).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
