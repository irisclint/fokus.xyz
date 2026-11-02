package xyz.fokus.module.impl.hud;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.Stats;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class KeystrokesModule extends HudModule {
    public final BoolSetting mouse = this.add(new BoolSetting("Maustasten", true));
    public final BoolSetting cps = this.add(new BoolSetting("CPS in Maustasten", true));
    public final BoolSetting space = this.add(new BoolSetting("Leertaste", true));
    private final Anim[] press = new Anim[7];

    public KeystrokesModule() {
        super("Keystrokes", "Zeigt WASD, Maustasten und Leertaste mit Druck-Animation.", 0.005F, Compat.irisClientHud() ? 0.36F : 0.27F);

        for (int i = 0; i < this.press.length; i++) {
            this.press[i] = new Anim(0.0F, 20.0F);
        }

        this.enabledByDefault();
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        Minecraft mc = Minecraft.getInstance();
        float k = 20.0F;
        float gap = 2.0F;
        float total = k * 3.0F + gap * 2.0F;
        this.w = total;
        float y = 0.0F;
        this.key(g, font, 0, mc.options.keyUp, "W", k + gap, y, k, k);
        y += k + gap;
        this.key(g, font, 1, mc.options.keyLeft, "A", 0.0F, y, k, k);
        this.key(g, font, 2, mc.options.keyDown, "S", k + gap, y, k, k);
        this.key(g, font, 3, mc.options.keyRight, "D", (k + gap) * 2.0F, y, k, k);
        y += k + gap;
        if (this.mouse.on()) {
            float mw = (total - gap) / 2.0F;
            String l = this.cps.on() ? Stats.cps(true) + " CPS" : "LMB";
            String r = this.cps.on() ? Stats.cps(false) + " CPS" : "RMB";
            this.key(g, font, 4, mc.options.keyAttack, l, 0.0F, y, mw, k - 4.0F);
            this.key(g, font, 5, mc.options.keyUse, r, mw + gap, y, mw, k - 4.0F);
            y += k - 4.0F + gap;
        }

        if (this.space.on()) {
            this.key(g, font, 6, mc.options.keyJump, "", 0.0F, y, total, 10.0F);
            float barW = total * 0.4F;
            R2D.round(g, total / 2.0F - barW / 2.0F, y + 4.25F, barW, 1.5F, 0.75F, Colors.lerp(this.text(), -15856114, this.press[6].get()));
            y += 10.0F + gap;
        }

        this.h = y - gap;
    }

    private void key(GuiGraphicsExtractor g, Font font, int i, KeyMapping mapping, String label, float x, float y, float kw, float kh) {
        float p = this.press[i].to(mapping.isDown() ? 1.0F : 0.0F).get();
        int idle = Colors.withAlpha(0, Theme.hudOpacity());
        int bg = Colors.lerp(idle, this.text(), p);
        float grow = p * 0.8F;
        if (this.background.on() && !Theme.hudStyle().equals("Ohne") || p > 0.01F) {
            R2D.round(g, x - grow / 2.0F, y - grow / 2.0F, kw + grow, kh + grow, Theme.hudRadius(), bg);
        }

        if (!label.isEmpty()) {
            float scale = label.length() > 3 ? 0.75F : 1.0F;
            int c = Colors.lerp(this.text(), -15856114, p);
            R2D.textCentered(g, font, label, x + kw / 2.0F, y + kh / 2.0F - 4.0F * scale, scale, c, Theme.shadow() && p < 0.5F);
        }
    }
}
