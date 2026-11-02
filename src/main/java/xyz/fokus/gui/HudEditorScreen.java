package xyz.fokus.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import xyz.fokus.HudRenderer;
import xyz.fokus.Lang;
import xyz.fokus.compat.Compat;
import xyz.fokus.config.ConfigIO;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Modules;
import xyz.fokus.module.Theme;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.Ease;
import xyz.fokus.util.R2D;

public class HudEditorScreen extends Screen {
    private static final float SNAP = 5.0F;
    private final Screen parent;
    private final long openedAt = System.currentTimeMillis();
    private HudModule dragging;
    private float offX;
    private float offY;
    private boolean guideX;
    private boolean guideY;
    private final Anim guideAlpha = new Anim(0.0F, 14.0F);
    private static final String HINT = "Ziehen: verschieben · Mausrad: Größe · Rechtsklick: ausblenden · ESC: fertig";

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD-Editor"));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        ConfigIO.save();
        if (this.parent != null) {
            this.minecraft.setScreenAndShow(this.parent);
        } else {
            super.onClose();
        }
    }

    private float fade() {
        return Ease.outCubic((float)(System.currentTimeMillis() - this.openedAt) / 250.0F);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        float a = this.fade();
        R2D.rect(g, 0.0F, 0.0F, this.width, this.height, Colors.alpha(1879048192, a));
        int grid = Colors.alpha(251658239, a);

        for (int x = 0; x < this.width; x += 20) {
            R2D.rect(g, x, 0.0F, x + 0.5F, this.height, grid);
        }

        for (int y = 0; y < this.height; y += 20) {
            R2D.rect(g, 0.0F, y, this.width, y + 0.5F, grid);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        float a = this.fade();
        float ga = this.guideAlpha.to(this.dragging == null || !this.guideX && !this.guideY ? 0.0F : 1.0F).get();
        if (ga > 0.01F) {
            int gc = Colors.alpha(Theme.accent(0.0F), ga * 0.8F);
            if (this.guideX) {
                R2D.rect(g, this.width / 2.0F - 0.5F, 0.0F, this.width / 2.0F + 0.5F, this.height, gc);
            }

            if (this.guideY) {
                R2D.rect(g, 0.0F, this.height / 2.0F - 0.5F, this.width, this.height / 2.0F + 0.5F, gc);
            }
        }

        HudModule hovered = this.pick(mouseX, mouseY);

        for (HudModule m : Modules.hud()) {
            if (m.isEnabled()) {
                HudRenderer.drawModule(g, m, true);
                float x = m.screenX(this.width);
                float y = m.screenY(this.height);
                float w = Math.max(10.0F, m.scaledW());
                float h = Math.max(8.0F, m.scaledH());
                boolean active = m == hovered || m == this.dragging;
                int oc = active ? Theme.accent(0.0F) : 1358954495;
                dashed(g, x - 1.5F, y - 1.5F, w + 3.0F, h + 3.0F, Colors.alpha(oc, a));
                if (active) {
                    String label = Lang.t(m.name) + "  " + Math.round(m.scale.get() * 100.0) + "%";
                    float lw = this.font.width(label) * 0.7F + 8.0F;
                    float ly = y - 12.0F < 0.0F ? y + h + 3.0F : y - 12.0F;
                    R2D.round(g, x - 1.5F, ly, lw, 9.5F, 3.0F, Colors.alpha(-419430401, a));
                    R2D.text(g, this.font, label, x + 2.5F, ly + 2.0F, 0.7F, -15856114, false);
                }
            }
        }

        List<HudModule> all = Modules.hud();
        float chipH = 13.0F;
        float rowW = this.chipRowWidth();
        float x = this.width / 2.0F - rowW / 2.0F;
        float y = this.chipsTop();

        for (HudModule mx : all) {
            float cw = this.font.width(Lang.t(mx.name)) * 0.7F + 12.0F;
            if (x + cw > this.width / 2.0F + rowW / 2.0F + 1.0F) {
                x = this.width / 2.0F - rowW / 2.0F;
                y += chipH + 4.0F;
            }

            boolean on = mx.isEnabled();
            boolean hov = R2D.inside(mouseX, mouseY, x, y, cw, chipH);
            int bg = on ? (hov ? -1 : -419430401) : (hov ? -1339677146 : -1728053248);
            R2D.round(g, x, y, cw, chipH, 6.5F, Colors.alpha(bg, a));
            R2D.textCentered(g, this.font, Lang.t(mx.name), x + cw / 2.0F, y + 3.5F, 0.7F, Colors.alpha(on ? -15856114 : -6052957, a), false);
            x += cw + 4.0F;
        }

        float[] p = this.panelRect();
        float[] r = this.resetRect();
        float pa = a * (this.dragging != null ? 0.35F : 1.0F);
        R2D.shadow(g, p[0], p[1], p[2], p[3], 6.0F, pa, 6);
        R2D.round(g, p[0], p[1], p[2], p[3], 6.0F, Colors.alpha(-955643381, pa));
        R2D.roundEdge(g, p[0], p[1], p[2], p[3], 6.0F, Colors.alpha(620756991, pa));
        R2D.textCentered(g, this.font, Lang.t("HUD-Editor"), p[0] + p[2] / 2.0F, p[1] + 5.0F, 1.0F, Colors.alpha(-723724, pa), false);
        R2D.textCentered(
            g,
            this.font,
            Lang.t("Ziehen: verschieben · Mausrad: Größe · Rechtsklick: ausblenden · ESC: fertig"),
            p[0] + p[2] / 2.0F,
            p[1] + 19.0F,
            0.7F,
            Colors.alpha(-6052957, pa),
            false
        );
        boolean hov = R2D.inside(mouseX, mouseY, r[0], r[1], r[2], r[3]);
        R2D.round(g, r[0], r[1], r[2], r[3], 5.0F, Colors.alpha(hov ? -1071241690 : -955643381, pa));
        R2D.roundEdge(g, r[0], r[1], r[2], r[3], 5.0F, Colors.alpha(620756991, pa));
        R2D.textCentered(g, this.font, Lang.t("Zurücksetzen"), r[0] + r[2] / 2.0F, r[1] + 4.5F, 0.75F, Colors.alpha(-723724, pa), false);
    }

    private float chipRowWidth() {
        float total = 0.0F;

        for (HudModule m : Modules.hud()) {
            total += this.font.width(Lang.t(m.name)) * 0.7F + 12.0F + 4.0F;
        }

        return Math.min(total, (float)(this.width - 20));
    }

    private float chipsTop() {
        float total = 0.0F;

        for (HudModule m : Modules.hud()) {
            total += this.font.width(Lang.t(m.name)) * 0.7F + 12.0F + 4.0F;
        }

        int rows = (int)Math.ceil(total / Math.max(1, this.width - 20));
        return this.height - 9 - rows * 17;
    }

    private float[] panelRect() {
        float pw = Math.max(this.font.width(Lang.t("Ziehen: verschieben · Mausrad: Größe · Rechtsklick: ausblenden · ESC: fertig")) * 0.7F + 20.0F, 160.0F);
        float group = pw + 6.0F + 70.0F;
        float px = this.width / 2.0F - group / 2.0F;
        return new float[]{px, this.chipsTop() - 38.0F, pw, 30.0F};
    }

    private float[] resetRect() {
        float[] p = this.panelRect();
        return new float[]{p[0] + p[2] + 6.0F, p[1] + 7.0F, 70.0F, 16.0F};
    }

    private static void dashed(GuiGraphicsExtractor g, float x, float y, float w, float h, int c) {
        float dash = 3.0F;

        for (float i = 0.0F; i < w; i += dash * 2.0F) {
            R2D.rect(g, x + i, y, Math.min(x + i + dash, x + w), y + 0.5F, c);
            R2D.rect(g, x + i, y + h - 0.5F, Math.min(x + i + dash, x + w), y + h, c);
        }

        for (float i = 0.0F; i < h; i += dash * 2.0F) {
            R2D.rect(g, x, y + i, x + 0.5F, Math.min(y + i + dash, y + h), c);
            R2D.rect(g, x + w - 0.5F, y + i, x + w, Math.min(y + i + dash, y + h), c);
        }
    }

    private HudModule pick(double mx, double my) {
        List<HudModule> all = Modules.hud();

        for (int i = all.size() - 1; i >= 0; i--) {
            HudModule m = all.get(i);
            if (m.isEnabled()
                && R2D.inside(
                    mx,
                    my,
                    m.screenX(this.width) - 2.0F,
                    m.screenY(this.height) - 2.0F,
                    Math.max(10.0F, m.scaledW()) + 4.0F,
                    Math.max(8.0F, m.scaledH()) + 4.0F
                )) {
                return m;
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        boolean right = event.button() == Compat.MOUSE_RIGHT;
        if (R2D.inside(mx, my, this.width - 78, 8.0F, 70.0F, 16.0F)) {
            for (HudModule m : Modules.hud()) {
                m.px = m.defaultX;
                m.py = m.defaultY;
                m.scale.reset();
            }

            ConfigIO.save();
            return true;
        } else {
            List<HudModule> all = Modules.hud();
            float chipH = 13.0F;
            float total = 0.0F;

            for (HudModule m : all) {
                total += this.font.width(Lang.t(m.name)) * 0.7F + 12.0F + 4.0F;
            }

            float rowW = Math.min(total, (float)(this.width - 20));
            float x = this.width / 2.0F - rowW / 2.0F;
            float y = this.height - 22 - (total > this.width - 20 ? chipH + 4.0F : 0.0F);

            for (HudModule m : all) {
                float cw = this.font.width(Lang.t(m.name)) * 0.7F + 12.0F;
                if (x + cw > this.width / 2.0F + rowW / 2.0F + 1.0F) {
                    x = this.width / 2.0F - rowW / 2.0F;
                    y += chipH + 4.0F;
                }

                if (R2D.inside(mx, my, x, y, cw, chipH)) {
                    m.toggle();
                    return true;
                }

                x += cw + 4.0F;
            }

            HudModule m = this.pick(mx, my);
            if (m == null) {
                return true;
            } else if (right) {
                m.setEnabled(false);
                return true;
            } else {
                this.dragging = m;
                this.offX = (float)mx - m.screenX(this.width);
                this.offY = (float)my - m.screenY(this.height);
                return true;
            }
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging == null) {
            return true;
        } else {
            float w = this.dragging.scaledW();
            float h = this.dragging.scaledH();
            float nx = (float)event.x() - this.offX;
            float ny = (float)event.y() - this.offY;
            this.guideX = Math.abs(nx + w / 2.0F - this.width / 2.0F) < 5.0F;
            this.guideY = Math.abs(ny + h / 2.0F - this.height / 2.0F) < 5.0F;
            if (this.guideX) {
                nx = this.width / 2.0F - w / 2.0F;
            }

            if (this.guideY) {
                ny = this.height / 2.0F - h / 2.0F;
            }

            if (nx < 5.0F) {
                nx = 0.0F;
            } else if (nx + w > this.width - 5.0F) {
                nx = this.width - w;
            }

            if (ny < 5.0F) {
                ny = 0.0F;
            } else if (ny + h > this.height - 5.0F) {
                ny = this.height - h;
            }

            if (this.dragging.isCentered()) {
                this.dragging.px = (nx + w / 2.0F) / this.width;
            } else {
                this.dragging.px = nx + w >= this.width - 0.5F ? 1.0F : nx / this.width;
            }

            this.dragging.py = ny + h >= this.height - 0.5F ? 1.0F : ny / this.height;
            return true;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging != null) {
            this.dragging = null;
            ConfigIO.save();
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        HudModule m = this.pick(mouseX, mouseY);
        if (m != null) {
            m.scale.set(m.scale.get() + (scrollY > 0.0 ? 0.05 : -0.05));
        }

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == Compat.KEY_ESCAPE) {
            this.onClose();
            return true;
        } else {
            return true;
        }
    }
}
