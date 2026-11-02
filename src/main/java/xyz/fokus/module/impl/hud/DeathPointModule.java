package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import xyz.fokus.Lang;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.util.Pointer;

/** Where you last died: in the chat the moment it happens, and on screen as direction and distance. */
public class DeathPointModule extends HudModule {
    public final BoolSetting chat = this.add(new BoolSetting("Im Chat melden", true));
    public final BoolSetting clearOnReach = this.add(new BoolSetting("Löschen wenn erreicht", true));

    private boolean known;
    private boolean away;
    private int x;
    private int y;
    private int z;
    private String dimension = "";

    public DeathPointModule() {
        super("Todespunkt", "Wo du zuletzt gestorben bist: im Chat und als Pfeil mit Entfernung.", 0.005F, 0.4F);
    }

    /**
     * The server says this player died (ClientboundPlayerCombatKillPacket). It comes on every death,
     * also with immediate respawn, where the dead player is replaced before a tick could see it.
     */
    public static void onDeath(Minecraft mc) {
        DeathPointModule m = Modules.get(DeathPointModule.class);
        LocalPlayer p = mc.player;
        if (m == null || !m.isEnabled() || p == null || mc.level == null) {
            return;
        }
        m.known = true;
        m.away = false;
        m.x = Mth.floor(p.getX());
        m.y = Mth.floor(p.getY());
        m.z = Mth.floor(p.getZ());
        m.dimension = mc.level.dimension().identifier().getPath();
        if (m.chat.on()) {
            Compat.say(Component.literal("☠ " + Lang.t("Gestorben bei") + " ").withColor(0xEB5757)
                    .append(Component.literal(m.x + " " + m.y + " " + m.z).withColor(0xFFFFFF))
                    .append(Component.literal(" (" + dimensionName(m.dimension) + ")").withColor(0x8A8A8A)));
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (!this.known || p == null || p.isDeadOrDying() || !this.sameDimension(mc)) {
            return;
        }
        double d = this.distance(p);
        // Reached only after having been away: respawning next to it (death at the spawn) is not coming back.
        if (d > 8.0) {
            this.away = true;
        } else if (d < 3.0 && this.away && this.clearOnReach.on()) {
            this.known = false;
        }
    }

    private boolean sameDimension(Minecraft mc) {
        return mc.level != null && mc.level.dimension().identifier().getPath().equals(this.dimension);
    }

    private double distance(LocalPlayer p) {
        return Pointer.distance(p, this.x, this.z);
    }

    private static String dimensionName(String path) {
        return Pointer.dimensionName(path);
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (editor && (!this.known || p == null)) {
            this.labelValue(g, font, Lang.t("Tod"), "↗ 128 m");
            return;
        }
        if (!this.known || p == null || p.isDeadOrDying()) {
            this.w = 0.0F;
            this.h = 0.0F;
            return;
        }
        if (!this.sameDimension(mc)) {
            this.labelValue(g, font, Lang.t("Tod"), dimensionName(this.dimension));
            return;
        }
        this.labelValue(g, font, Lang.t("Tod"), Pointer.toward(p, this.x, this.z));
    }
}
