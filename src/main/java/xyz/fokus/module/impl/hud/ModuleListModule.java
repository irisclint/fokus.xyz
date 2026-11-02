package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class ModuleListModule extends HudModule {
    public final BoolSetting sideBar = this.add(new BoolSetting("Seitenleiste", true));
    public final BoolSetting wave = this.add(new BoolSetting("Verlauf Akzent → Zweitton", false));

    public ModuleListModule() {
        super("Modulliste", "Listet deine aktiven Module, mit Einblend-Animation.", 1.0F, 0.005F);
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        List<Module> shown = new ArrayList<>();

        for (Module m : Modules.all()) {
            if (m.showInList()) {
                m.listAnim.to(m.isEnabled() ? 1.0F : 0.0F);
                if (m.listAnim.get() > 0.01F) {
                    shown.add(m);
                }
            }
        }

        shown.sort(Comparator.<Module>comparingInt(mx -> font.width(mx.name)).reversed());
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        boolean right = this.px * screenW + this.w / 2.0F > screenW / 2.0F;
        float maxW = 30.0F;

        for (Module mx : shown) {
            maxW = Math.max(maxW, (float)(font.width(mx.name) + 8));
        }

        this.w = maxW + (this.sideBar.on() ? 2 : 0);
        float y = 0.0F;
        float lineH = 11.0F;

        for (int i = 0; i < shown.size(); i++) {
            Module mx = shown.get(i);
            float a = mx.listAnim.get();
            float tw = font.width(mx.name) + 8;
            float off = (1.0F - a) * (tw + 4.0F);
            float x = right ? this.w - tw - (this.sideBar.on() ? 2 : 0) + off : (this.sideBar.on() ? 2 : 0) - off;
            float lh = lineH * a;
            int color = this.wave.on() ? Colors.lerp(this.text(), this.label(), shown.size() < 2 ? 0.0F : (float)i / (shown.size() - 1)) : this.text();
            if (this.background.on()) {
                R2D.rect(g, x, y, x + tw, y + lh, Colors.alpha(Colors.withAlpha(0, Theme.hudOpacity()), a));
            }

            if (this.sideBar.on()) {
                float bx = right ? this.w - 1.0F : 1.0F;
                R2D.rect(g, bx, y, bx + 1.0F, y + lh, Colors.alpha(color, a));
            }

            R2D.text(g, font, mx.name, x + 4.0F, y + 1.5F, Colors.alpha(color, a), Theme.shadow());
            y += lh;
        }

        this.h = Math.max(lineH, y);
    }
}
