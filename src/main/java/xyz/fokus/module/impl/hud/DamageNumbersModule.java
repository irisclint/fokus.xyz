package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xyz.fokus.SkyProjection;
import xyz.fokus.Stats;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

/** The damage of every hit rises from the one hit and fades: read off the health the server sends. */
public class DamageNumbersModule extends HudModule {
    private static final long LIFE = 1000L;
    private static final double RANGE = 32.0;

    public final BoolSetting yoursOnly = this.add(new BoolSetting("Nur deine Treffer", true));
    public final ModeSetting unit = this.add(new ModeSetting("Einheit", "Herzen", "Herzen", "Lebenspunkte"));
    public final BoolSetting healing = this.add(new BoolSetting("Heilung zeigen", false));
    public final ColorSetting color = this.add(new ColorSetting("Farbe", 0xFFFF5555));
    public final NumberSetting size = this.add(new NumberSetting("Größe", 1.0, 0.5, 2.5, 0.1, "x"));

    private final Map<Integer, Float> health = new HashMap<>();
    private final List<Pop> pops = new ArrayList<>();

    private record Pop(double x, double y, double z, String text, int color, long born, float drift) {
    }

    public DamageNumbersModule() {
        super("Schadenszahlen", "Der Schaden jedes Treffers steigt als Zahl über dem Getroffenen auf.", 0.5F, 0.5F);
    }

    @Override
    public boolean placesItself() {
        return true;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            this.health.clear();
            this.pops.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Set<Integer> seen = new HashSet<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living) || e == mc.player || e.distanceToSqr(mc.player) > RANGE * RANGE) {
                continue;
            }
            int id = e.getId();
            seen.add(id);
            float hp = living.getHealth();
            Float before = this.health.put(id, hp);
            if (before == null || Math.abs(before - hp) < 0.05F) {
                continue;
            }
            boolean damage = hp < before;
            if (!damage && !this.healing.on()) {
                continue;
            }
            // "Yours": the entity you hit within the last second.
            if (damage && this.yoursOnly.on() && (Stats.target != living || now - Stats.lastHitAt > 1000L)) {
                continue;
            }
            float amount = Math.abs(before - hp);
            boolean hearts = this.unit.is("Herzen");
            String text = (damage ? "" : "+") + number(hearts ? amount / 2.0F : amount) + (hearts ? "❤" : "");
            int c = damage ? this.color.color() : 0xFF55E07A;
            float drift = (float) ((id * 37 + now / 50 % 7) % 9 - 4) * 0.06F;
            this.pops.add(new Pop(e.getX(), e.getY() + e.getBbHeight() + 0.25, e.getZ(), text, c, now, drift));
        }
        this.health.keySet().retainAll(seen);
        this.pops.removeIf(p -> now - p.born > LIFE);
        if (this.pops.size() > 40) {
            this.pops.subList(0, this.pops.size() - 40).clear();
        }
    }

    private static String number(float v) {
        float r = Math.round(v * 10.0F) / 10.0F;
        return r == Math.floor(r) ? String.valueOf((int) r) : String.format(Locale.ROOT, "%.1f", r);
    }

    @Override
    public void drawPlaced(GuiGraphicsExtractor g, Font font) {
        long now = System.currentTimeMillis();
        for (Iterator<Pop> it = this.pops.iterator(); it.hasNext(); ) {
            Pop p = it.next();
            float t = Math.min(1.0F, (now - p.born) / (float) LIFE);
            // Rises quickly, then slows; fades over the last 40%.
            double rise = 0.9 * (1.0 - (1.0 - t) * (1.0 - t));
            float[] s = SkyProjection.projectWorld(p.x + p.drift * t, p.y + rise, p.z, g.guiWidth(), g.guiHeight());
            if (s == null) {
                continue;
            }
            float alpha = t < 0.6F ? 1.0F : 1.0F - (t - 0.6F) / 0.4F;
            // A pop at the start, then the size of the distance.
            float pop = t < 0.12F ? 1.0F + (0.12F - t) * 3.0F : 1.0F;
            float scale = Math.max(0.45F, Math.min(1.3F, 4.0F / Math.max(1.0F, s[2]))) * this.size.f() * pop;
            R2D.textCentered(g, font, p.text, s[0], s[1] - 4.0F * scale, scale, Colors.alpha(p.color | 0xFF000000, alpha), true);
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        // Drawn in the world by drawPlaced; nothing to place in the HUD editor.
        this.w = 0.0F;
        this.h = 0.0F;
    }
}
