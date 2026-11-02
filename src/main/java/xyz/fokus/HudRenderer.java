package xyz.fokus;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import xyz.fokus.compat.Compat;
import xyz.fokus.gui.HudEditorScreen;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.pvp.CrosshairModule;
import xyz.fokus.module.impl.pvp.HitmarkerModule;

public final class HudRenderer {
    private static final Set<String> FAILED = new HashSet<>();

    private HudRenderer() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !(Compat.currentScreen() instanceof HudEditorScreen)) {
            for (HudModule m : Modules.hud()) {
                if (m.isEnabled()) {
                    if (m.placesItself()) {
                        try {
                            m.drawPlaced(g, mc.font);
                        } catch (RuntimeException var5) {
                            if (FAILED.add(m.name)) {
                                FokusMod.LOG.warn("fokus.xyz: HUD module {} failed", m.name, var5);
                            }
                        }
                    } else {
                        drawModule(g, m, false);
                    }
                }
            }
        }
    }

    public static void drawModule(GuiGraphicsExtractor g, HudModule m, boolean editor) {
        float x = m.screenX(g.guiWidth());
        float y = m.screenY(g.guiHeight());
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(m.scale.f(), m.scale.f());

        try {
            m.draw(g, Minecraft.getInstance().font, editor);
        } catch (RuntimeException var7) {
            if (FAILED.add(m.name)) {
                FokusMod.LOG.warn("fokus.xyz: HUD module {} failed", m.name, var7);
            }
        }

        pose.popMatrix();
    }

    public static boolean crosshair(GuiGraphicsExtractor g) {
        HitmarkerModule hit = Modules.get(HitmarkerModule.class);
        if (hit.isEnabled()) {
            try {
                hit.draw(g);
            } catch (RuntimeException var5) {
                FokusMod.LOG.debug("fokus.xyz: hitmarker failed", var5);
            }
        }

        CrosshairModule c = Modules.get(CrosshairModule.class);
        if (!c.isEnabled()) {
            return false;
        } else {
            try {
                c.draw(g);
            } catch (RuntimeException var4) {
                FokusMod.LOG.debug("fokus.xyz: crosshair failed", var4);
            }

            return true;
        }
    }
}
