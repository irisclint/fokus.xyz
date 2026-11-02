package xyz.fokus.module.impl.hud;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import xyz.fokus.Lang;
import xyz.fokus.Stats;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class TargetHudModule extends HudModule {
    private final Anim health = new Anim(1.0F, 8.0F);
    private final Anim lag = new Anim(1.0F, 3.0F);
    private final Anim show = new Anim(0.0F, 12.0F);
    private String name = "Gegner";

    public TargetHudModule() {
        super("Target HUD", "Name, Leben und Abstand des Gegners, den du zuletzt getroffen hast.", 0.56F, 0.53F);
        this.enabledByDefault();
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        LivingEntity t = Stats.target;
        Minecraft mc = Minecraft.getInstance();
        ItemStack held = ItemStack.EMPTY;
        float hp;
        float max;
        String dist;
        if (t != null) {
            this.name = t.getDisplayName().getString();
            hp = t.getHealth() + t.getAbsorptionAmount();
            max = t.getMaxHealth();
            dist = mc.player == null ? "" : String.format(Locale.ROOT, "%.1f m", mc.player.distanceTo(t));
            held = t.getMainHandItem();
        } else {
            hp = 14.0F;
            max = 20.0F;
            dist = "3.1 m";
        }

        this.show.to(t == null && !editor ? 0.0F : 1.0F);
        float a = this.show.get();
        this.w = 128.0F;
        this.h = 36.0F;
        if (!(a < 0.02F)) {
            float frac = Math.max(0.0F, Math.min(1.0F, hp / Math.max(1.0F, max)));
            this.health.to(frac);
            this.lag.to(frac);
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            float sc = 0.9F + 0.1F * a;
            pose.translate(this.w / 2.0F, this.h / 2.0F);
            pose.scale(sc, sc);
            pose.translate(-this.w / 2.0F, -this.h / 2.0F);
            if (this.background.on()) {
                R2D.round(g, 0.0F, 0.0F, this.w, this.h, 4.0F, Colors.alpha(Colors.withAlpha(0, Math.max(120, Theme.hudOpacity())), a));
                R2D.roundEdge(g, 0.0F, 0.0F, this.w, this.h, 4.0F, Colors.alpha(419430399, a));
            }

            R2D.circle(g, 16.0F, 16.0F, 11.0F, Colors.alpha(1090519039, a));
            R2D.circle(g, 16.0F, 16.0F, 10.2F, Colors.alpha(-15724528, a));
            String name = this.name.equals("Gegner") ? Lang.t("Gegner") : this.name;
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT);
            R2D.textCentered(g, font, initial, 16.0F, 11.5F, 1.2F, Colors.alpha(-1, a), false);
            String shown = R2D.trim(font, name, this.w - 34.0F - 20.0F, 1.0F);
            R2D.text(g, font, shown, 32.0F, 5.0F, Colors.alpha(-1, a), Theme.shadow());
            R2D.text(g, font, dist, 32.0F, 15.0F, 0.75F, Colors.alpha(-6645094, a), false);
            if (!held.isEmpty()) {
                pose.pushMatrix();
                pose.translate(this.w - 20.0F, 3.0F);
                g.item(held, 0, 0);
                pose.popMatrix();
            }

            float bx = 32.0F;
            float by = 25.0F;
            float bw = this.w - bx - 6.0F;
            R2D.round(g, bx, by, bw, 4.0F, 2.0F, Colors.alpha(654311423, a));
            R2D.round(g, bx, by, bw * this.lag.get(), 4.0F, 2.0F, Colors.alpha(-1, a * 0.3F));
            int hc = this.text();
            R2D.round(g, bx, by, bw * this.health.get(), 4.0F, 2.0F, Colors.alpha(hc, a));
            String hpText = String.format(Locale.ROOT, "%.1f", hp / 2.0F) + " ❤";
            R2D.text(g, font, hpText, this.w - 6.0F - font.width(hpText) * 0.7F, 15.0F, 0.7F, Colors.alpha(hc, a), false);
            pose.popMatrix();
        }
    }
}
