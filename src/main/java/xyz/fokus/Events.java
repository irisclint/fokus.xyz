package xyz.fokus;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import xyz.fokus.compat.Compat;
import xyz.fokus.config.ConfigIO;
import xyz.fokus.gui.FokusScreen;
import xyz.fokus.lyrics.MediaWatcher;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.module.Theme;
import xyz.fokus.module.impl.settings.InterfaceModule;
import xyz.fokus.update.Updater;
import xyz.fokus.util.GlassBox;

public final class Events {
    private static boolean wasInWorld;
    private static boolean updateAnnounced;

    private Events() {
    }

    public static void tick(Minecraft mc) {
        Stats.tick(mc);
        GlassBox.prepare(Theme.hudRadius());
        MediaWatcher.idleCheck();

        for (Module m : Modules.all()) {
            if (m.isEnabled()) {
                try {
                    m.onTick(mc);
                } catch (RuntimeException var6) {
                    FokusMod.LOG.debug("fokus.xyz: tick of {} failed", m.name, var6);
                }
            }
        }

        boolean inWorld = mc.player != null && Compat.currentScreen() == null && mc.mouseHandler.isMouseGrabbed();
        InterfaceModule ui = Modules.get(InterfaceModule.class);
        if (!inWorld) {
            if (wasInWorld) {
                ui.menuKey.clearEdge();
                Modules.all().forEach(mx -> mx.bind.clearEdge());
            }

            wasInWorld = false;
        } else if (!wasInWorld) {
            ui.menuKey.clearEdge();
            Modules.all().forEach(mx -> mx.bind.clearEdge());
            wasInWorld = true;
        } else {
            if (!updateAnnounced && Updater.state() == Updater.State.READY) {
                updateAnnounced = true;
                mc.player
                    .sendOverlayMessage(
                        Component.literal("§7fokus.xyz  §f" + Updater.latestVersion() + "  §7" + Lang.t("Update bereit - beim Neustart installiert"))
                    );
            }

            if (ui.menuKey.pressed()) {
                mc.setScreenAndShow(new FokusScreen(null));
            } else {
                boolean changed = false;

                for (Module mx : Modules.all()) {
                    if (!mx.isAlwaysOn() && mx.bind.pressed()) {
                        mx.toggle();
                        changed = true;
                        if (mc.player != null) {
                            mc.player
                                .sendOverlayMessage(
                                    Component.literal("§7fokus.xyz  §f" + Lang.t(mx.name) + " " + (mx.isEnabled() ? "§f" + Lang.t("an") : "§8" + Lang.t("aus")))
                                );
                        }
                    }
                }

                if (changed) {
                    ConfigIO.save();
                }
            }
        }
    }
}
