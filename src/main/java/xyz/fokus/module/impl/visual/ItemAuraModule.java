package xyz.fokus.module.impl.visual;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

/**
 * An aura around the items that matter in a fight - totems, ender pearls, crystals, golden
 * apples, potions, wind charges: a soft glow in two colours behind them in the hotbar and the
 * inventory, and a glowing outline around them in the world (dropped, or a pearl in flight).
 * Off until switched on; which items, where and how it looks is all chosen by hand.
 */
public class ItemAuraModule extends Module {
    public final BoolSetting totems = add(new BoolSetting("Totems", true));
    public final BoolSetting pearls = add(new BoolSetting("Enderperlen", true));
    public final BoolSetting crystals = add(new BoolSetting("Endkristalle", false));
    public final BoolSetting apples = add(new BoolSetting("Goldäpfel", false));
    public final BoolSetting potions = add(new BoolSetting("Tränke", false));
    public final BoolSetting windCharges = add(new BoolSetting("Windladungen", false));
    public final BoolSetting inGui = add(new BoolSetting("In Hotbar & Inventar", true));
    public final BoolSetting inWorld = add(new BoolSetting("In der Welt (Umriss)", true));
    public final ColorSetting inner = add(new ColorSetting("Farbe innen", 0xFFFFE27A));
    public final ColorSetting outer = add(new ColorSetting("Farbe außen", 0xFFB15CFF));
    public final ModeSetting motion = add(new ModeSetting("Verlauf", "Fließend", "Statisch", "Pulsieren", "Fließend"));
    public final NumberSetting size = add(new NumberSetting("Größe", 1.0, 0.5, 2.0, 0.05, "x"));
    public final NumberSetting strength = add(new NumberSetting("Stärke", 85, 10, 100, 5, "%"));
    public final NumberSetting speed = add(new NumberSetting("Tempo", 1.0, 0.2, 3.0, 0.1, "x"));

    public ItemAuraModule() {
        super("Item-Aura", "Leuchten und Farbverlauf um Totems, Enderperlen und mehr - im Inventar und in der Welt.",
                Category.VISUALS);
    }

    private static ItemAuraModule on() {
        ItemAuraModule m = Modules.get(ItemAuraModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    private boolean wants(ItemStack s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return totems.on() && s.is(Items.TOTEM_OF_UNDYING) || pearls.on() && s.is(Items.ENDER_PEARL)
                || crystals.on() && s.is(Items.END_CRYSTAL)
                || apples.on() && (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE))
                || potions.on() && (s.is(Items.POTION) || s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION))
                || windCharges.on() && s.is(Items.WIND_CHARGE);
    }

    /** 0..1, moving with time for the animated looks. */
    private float phase() {
        double t = System.currentTimeMillis() / 1000.0 * speed.get();
        return (float) (Math.sin(t * Math.PI) * 0.5 + 0.5);
    }

    /** The outline colour in the world: the gradient's colours taking turns. */
    private int outlineColor() {
        float p = motion.is("Statisch") ? 0.0F : phase();
        return Colors.lerp(inner.color(0.0F), outer.color(0.5F), p) & 0xFFFFFF;
    }

    // Hooks --------------------------------------------------------------------------------

    /** Behind an item icon in a GUI: a radial glow from the inner to the outer colour. */
    public static void drawBehind(GuiGraphicsExtractor g, ItemStack stack, int x, int y) {
        ItemAuraModule m = on();
        if (m == null || !m.inGui.on() || !m.wants(stack)) {
            return;
        }
        float p = m.phase();
        float pulse = m.motion.is("Pulsieren") ? 0.75F + p * 0.35F : 1.0F;
        float alpha = m.strength.f() / 100.0F * (m.motion.is("Pulsieren") ? 0.6F + p * 0.4F : 1.0F);
        int c1 = m.inner.color(0.0F);
        int c2 = m.outer.color(0.5F);
        if (m.motion.is("Fließend")) {
            // The two colours trade places back and forth.
            int a = Colors.lerp(c1, c2, p);
            int b = Colors.lerp(c2, c1, p);
            c1 = a;
            c2 = b;
        }
        float cx = x + 8.0F;
        float cy = y + 8.0F;
        float radius = 10.0F * m.size.f() * pulse;
        int rings = 7;
        for (int i = 0; i < rings; i++) {
            float f = i / (float) (rings - 1);
            float r = radius * (1.0F - f * 0.82F);
            int c = Colors.lerp(c2, c1, f);
            // Layers add up towards the middle: soft edge, bright core.
            R2D.circle(g, cx, cy, r, Colors.alpha(c, alpha * (0.14F + 0.30F * f)));
        }
    }

    /** Whether a dropped item or thrown item should glow. */
    public static boolean glows(ItemStack stack) {
        ItemAuraModule m = on();
        return m != null && m.inWorld.on() && m.wants(stack);
    }

    public static int glowColor() {
        ItemAuraModule m = on();
        return m == null ? 0xFFFFFF : m.outlineColor();
    }
}
