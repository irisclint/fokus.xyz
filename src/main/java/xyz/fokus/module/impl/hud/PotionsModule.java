package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import xyz.fokus.Lang;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.util.R2D;

public class PotionsModule extends HudModule {
    private static final String[] ROMAN = new String[]{"", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private final List<PotionsModule.Row> rows = new ArrayList<>();
    private float textW;

    public PotionsModule() {
        super("Effekte", "Aktive Trank-Effekte mit Restzeit.", 1.0F, 0.2F);
        this.enabledByDefault();
    }

    @Override
    public void onTick(Minecraft mc) {
        this.rows.clear();
        LocalPlayer p = mc.player;
        if (p != null) {
            List<MobEffectInstance> effects = new ArrayList<>(p.getActiveEffects());
            effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration).reversed());
            Font font = mc.font;
            this.textW = 0.0F;

            for (MobEffectInstance e : effects) {
                int amp = e.getAmplifier();
                String name = e.getEffect().value().getDisplayName().getString()
                    + (amp > 0 && amp < ROMAN.length ? " " + ROMAN[amp] : (amp >= ROMAN.length ? " " + (amp + 1) : ""));
                String time;
                if (e.isInfiniteDuration()) {
                    time = "∞";
                } else {
                    int secs = e.getDuration() / 20;
                    time = secs / 60 + ":" + (secs % 60 < 10 ? "0" : "") + secs % 60;
                }

                this.rows.add(new PotionsModule.Row(name, time, !e.isInfiniteDuration() && e.endsWithin(200)));
                this.textW = Math.max(this.textW, (float)(font.width(name) + 8 + font.width(time)));
            }
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        List<PotionsModule.Row> shown = this.rows;
        float tw = this.textW;
        if (shown.isEmpty() && editor) {
            String strength = Lang.t("Stärke II");
            String speed = Lang.t("Schnelligkeit II");
            shown = List.of(new PotionsModule.Row(strength, "1:30", false), new PotionsModule.Row(speed, "0:08", true));
            tw = Math.max(font.width(strength), font.width(speed)) + 8 + font.width("0:08");
        }

        if (shown.isEmpty()) {
            this.w = 0.0F;
            this.h = 0.0F;
        } else {
            this.w = tw + 10.0F;
            this.h = shown.size() * 11 + 3;
            this.box(g, this.w, this.h);
            boolean blink = System.currentTimeMillis() / 300L % 2L == 0L;

            for (int i = 0; i < shown.size(); i++) {
                PotionsModule.Row r = shown.get(i);
                float y = 2.5F + i * 11;
                R2D.text(g, font, r.name, 5.0F, y + 0.5F, this.text(), Theme.shadow());
                int tc = r.ending && blink ? this.text() : this.label();
                R2D.text(g, font, r.time, this.w - 5.0F - font.width(r.time), y + 0.5F, tc, Theme.shadow());
            }
        }
    }

    private record Row(String name, String time, boolean ending) {
    }
}
