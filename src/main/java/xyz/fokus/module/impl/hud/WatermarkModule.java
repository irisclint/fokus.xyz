package xyz.fokus.module.impl.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.TextSetting;
import xyz.fokus.util.R2D;

public class WatermarkModule extends HudModule {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    public final BoolSetting fps = this.add(new BoolSetting("FPS zeigen", true));
    public final BoolSetting time = this.add(new BoolSetting("Uhrzeit zeigen", false));
    public final TextSetting custom = this.add(new TextSetting("Eigener Text", "", 32));

    public WatermarkModule() {
        super("Wasserzeichen", "„fokus.xyz“ in einer Ecke, optional mit FPS und Uhrzeit.", Compat.irisClientHud() ? 1.0F : 0.005F, 0.005F);
        this.enabledByDefault();
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        String extra = "";
        if (this.fps.on()) {
            extra = extra + Minecraft.getInstance().getFps() + " fps";
        }

        if (this.time.on()) {
            extra = extra + (extra.isEmpty() ? "" : "  ") + LocalTime.now().format(TIME);
        }

        // Your own text, or fokus.xyz; the part after the last dot is drawn grey, like ".xyz".
        String own = this.custom.get().trim();
        String main = own.isEmpty() ? "fokus" : own;
        String rest = own.isEmpty() ? ".xyz" : "";
        int dot = main.lastIndexOf('.');
        if (!own.isEmpty() && dot > 0 && dot < main.length() - 1) {
            rest = main.substring(dot);
            main = main.substring(0, dot);
        }
        float word = font.width(main);
        float tld = font.width(rest);
        float extraW = extra.isEmpty() ? 0.0F : font.width(extra) * 0.8F + 7.0F;
        this.w = 5.0F + word + tld + extraW + 5.0F;
        this.h = 13.0F;
        this.box(g, this.w, this.h);
        R2D.text(g, font, main, 5.0F, 2.5F, this.text(), Theme.shadow());
        R2D.text(g, font, rest, 5.0F + word, 2.5F, this.label(), Theme.shadow());
        if (!extra.isEmpty()) {
            float sx = 5.0F + word + tld + 3.0F;
            R2D.rect(g, sx, 3.5F, sx + 0.5F, this.h - 3.5F, 822083583);
            R2D.text(g, font, extra, sx + 4.0F, 3.3F, 0.8F, this.label(), Theme.shadow());
        }
    }
}
