package xyz.fokus.module.impl.hud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import xyz.fokus.Lang;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Modules;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class TotemPopsModule extends HudModule {
    public final BoolSetting self = this.add(new BoolSetting("Eigene Pops zeigen", true));
    public final BoolSetting announce = this.add(new BoolSetting("Pop ansagen", true));
    public final BoolSetting resetOnDeath = this.add(new BoolSetting("Nach dem Tod zurücksetzen", true));
    public final NumberSetting rows = this.add(new NumberSetting("Höchstens Zeilen", 5.0, 1.0, 10.0, 1.0, ""));
    private static final Map<UUID, TotemPopsModule.Pops> POPS = new LinkedHashMap<>();
    private final List<TotemPopsModule.Pops> shown = new ArrayList<>();
    private static ItemStack totem;

    public TotemPopsModule() {
        super("Totem Pops", "Zählt die Totems, die du und deine Gegner seit dem letzten Tod verbraucht habt.", 0.82F, 0.35F);
        this.enabledByDefault();
    }

    public static void onPop(Entity e) {
        if (e instanceof Player p) {
            TotemPopsModule m = Modules.get(TotemPopsModule.class);
            if (m != null && m.isEnabled()) {
                TotemPopsModule.Pops pops = POPS.computeIfAbsent(p.getUUID(), id -> new TotemPopsModule.Pops());
                pops.name = p.getName().getString();
                pops.count++;
                pops.last = System.currentTimeMillis();
                Minecraft mc = Minecraft.getInstance();
                if (m.announce.on() && mc.player != null && p != mc.player) {
                    String what = pops.count == 1 ? Lang.t("hat 1 Totem gepoppt") : Lang.t("hat {} Totems gepoppt").replace("{}", String.valueOf(pops.count));
                    mc.player.sendOverlayMessage(Component.literal("§f" + pops.name + " §7" + what));
                }
            }
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        this.shown.clear();
        if (mc.level == null) {
            POPS.clear();
        } else {
            if (this.resetOnDeath.on()) {
                for (Player p : mc.level.players()) {
                    if (p.isDeadOrDying()) {
                        POPS.remove(p.getUUID());
                    }
                }
            }

            for (Entry<UUID, TotemPopsModule.Pops> e : POPS.entrySet()) {
                if (this.self.on() || mc.player == null || !e.getKey().equals(mc.player.getUUID())) {
                    this.shown.add(e.getValue());
                }
            }

            this.shown.sort((a, b) -> Long.compare(b.last, a.last));

            while (this.shown.size() > this.rows.i()) {
                this.shown.remove(this.shown.size() - 1);
            }
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        List<TotemPopsModule.Pops> list = this.shown;
        if (list.isEmpty() && editor) {
            TotemPopsModule.Pops a = new TotemPopsModule.Pops();
            a.name = "Steve";
            a.count = 3;
            a.last = System.currentTimeMillis();
            TotemPopsModule.Pops b = new TotemPopsModule.Pops();
            b.name = "Alex";
            b.count = 1;
            list = List.of(a, b);
        }

        float nameW = 0.0F;

        for (TotemPopsModule.Pops p : list) {
            nameW = Math.max(nameW, (float)font.width(p.name));
        }

        this.w = 21.0F + nameW + 22.0F;
        this.h = 4 + Math.max(1, list.size()) * 11;
        if (!list.isEmpty()) {
            this.box(g, this.w, this.h);
            long now = System.currentTimeMillis();

            for (int i = 0; i < list.size(); i++) {
                TotemPopsModule.Pops p = list.get(i);
                float y = 3 + i * 11;
                Matrix3x2fStack pose = g.pose();
                pose.pushMatrix();
                pose.translate(4.0F, y - 0.5F);
                pose.scale(0.6F, 0.6F);
                if (totem == null) {
                    totem = new ItemStack(Items.TOTEM_OF_UNDYING);
                }

                g.item(totem, 0, 0);
                pose.popMatrix();
                R2D.text(g, font, p.name, 17.0F, y + 1.0F, this.text(), Theme.shadow());
                float fresh = Math.max(0.0F, 1.0F - (float)(now - p.last) / 2000.0F);
                int c = Colors.lerp(this.label(), -1, fresh);
                String n = String.valueOf(p.count);
                R2D.text(g, font, n, this.w - 4.5F - font.width(n), y + 1.0F, c, Theme.shadow());
            }
        }
    }

    private static final class Pops {
        String name;
        int count;
        long last;
    }
}
