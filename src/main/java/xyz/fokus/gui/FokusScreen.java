package xyz.fokus.gui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix3x2fStack;
import xyz.fokus.FokusMod;
import xyz.fokus.Lang;
import xyz.fokus.compat.Compat;
import xyz.fokus.config.ConfigIO;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.module.Theme;
import xyz.fokus.module.impl.settings.InterfaceModule;
import xyz.fokus.setting.ActionSetting;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.TextSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.setting.Setting;
import xyz.fokus.update.Updater;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.Ease;
import xyz.fokus.util.R2D;

public class FokusScreen extends Screen {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int MIDDLE = 2;
    private static Category lastCategory = Category.PVP;
    private final Screen parent;
    private final long openedAt = System.currentTimeMillis();
    private long closingAt = -1L;
    private boolean switched;
    private Category category = lastCategory;
    private long categoryAt = System.currentTimeMillis();
    private final Anim catIndicator = new Anim(-1.0F, 16.0F);
    private final Anim scroll = new Anim(0.0F, 16.0F);
    private float scrollTarget;
    private float maxScroll;
    /** The scrollbar as last drawn, for clicks and drags. */
    private float trackX;
    private float thumbY;
    private float thumbH;
    private boolean scrollDragging;
    private float scrollGrab;
    private final Anim thumbHover = new Anim(0.0F, 16.0F);
    private String search = "";
    private boolean searchFocus;
    private NumberSetting dragging;
    private float dragX;
    private float dragW;
    private ColorSetting hueDragging;
    private float hueX;
    private float hueW;
    private KeySetting listening;
    /** The text setting being typed into, if any. */
    private TextSetting editing;
    private final List<FokusScreen.Hit> hits = new ArrayList<>();
    private Module hoveredModule;
    private long hoverStart;
    private float px;
    private float py;
    private float pw;
    private float ph;
    private float sw;
    private float cx;
    private float cw;
    private float ct;
    private float cb;
    private float scale = 1.0F;
    private float lift;

    public FokusScreen(Screen parent) {
        super(Component.literal("fokus.xyz"));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        this.close();
    }

    private void close() {
        if (this.closingAt < 0L) {
            this.closingAt = System.currentTimeMillis();
            this.listening = null;
            ConfigIO.save();
        }
    }

    @Override
    public void tick() {
        if (this.closingAt >= 0L && !this.switched && this.progress() <= 0.0F) {
            this.switched = true;
            if (this.parent != null) {
                this.minecraft.setScreenAndShow(this.parent);
            } else {
                super.onClose();
            }
        }
    }

    @Override
    public void removed() {
        ConfigIO.save();
    }

    private float progress() {
        long now = System.currentTimeMillis();
        float t = Theme.animTime();
        return this.closingAt >= 0L
            ? 1.0F - Ease.clamp01((float)(now - this.closingAt) / (140.0F * t))
            : Ease.clamp01((float)(now - this.openedAt) / (260.0F * t));
    }

    private void layout() {
        this.pw = Math.max(300, Math.min(this.width - 30, 520));
        this.ph = Math.max(200, Math.min(this.height - 30, 320));
        this.px = (this.width - this.pw) / 2.0F;
        this.py = (this.height - this.ph) / 2.0F;
        this.sw = this.pw < 380.0F ? 104.0F : 124.0F;
        this.cx = this.px + this.sw + 12.0F;
        this.cw = this.pw - this.sw - 24.0F;
        this.ct = this.py + 46.0F;
        this.cb = this.py + this.ph - 18.0F;
    }

    private float localX(double mx) {
        float c = this.px + this.pw / 2.0F;
        return (float)(c + (mx - c) / this.scale);
    }

    private float localY(double my) {
        float c = this.py + this.ph / 2.0F;
        return (float)(c + (my - c) / this.scale) - this.lift;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        float a = Ease.outCubic(this.progress());
        if (Theme.blur()) {
            super.extractBackground(g, mouseX, mouseY, partial);
        }

        R2D.rect(g, 0.0F, 0.0F, this.width, this.height, Colors.alpha(-1946157056, a));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        this.layout();
        this.hits.clear();
        float p = this.progress();
        float a = Ease.outCubic(p);
        float fit = Math.min((this.width - 8) / this.pw, (this.height - 8) / this.ph);
        this.scale = (0.975F + 0.025F * a) * Math.min(Theme.menuScale(), Math.max(0.5F, fit));
        this.lift = (1.0F - a) * 6.0F;
        float centerX = this.px + this.pw / 2.0F;
        float centerY = this.py + this.ph / 2.0F;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY + this.lift);
        pose.scale(this.scale, this.scale);
        pose.translate(-centerX, -centerY);

        try {
            this.drawPanel(g, this.localX(mouseX), this.localY(mouseY), a);
        } catch (RuntimeException var12) {
            FokusMod.LOG.error("fokus.xyz: menu drawing failed", var12);
        }

        pose.popMatrix();
    }

    private void drawPanel(GuiGraphicsExtractor g, float mx, float my, float a) {
        R2D.shadow(g, this.px, this.py, this.pw, this.ph, 10.0F, a, 10);
        R2D.round(g, this.px, this.py, this.pw, this.ph, 10.0F, Colors.alpha(-955643381, a));
        R2D.roundEdge(g, this.px, this.py, this.pw, this.ph, 10.0F, Colors.alpha(620756991, a));
        R2D.rect(g, this.px + 14.0F, this.py + 0.5F, this.px + this.pw - 14.0F, this.py + 1.0F, Colors.alpha(352321535, a));
        this.drawSidebar(g, mx, my, a);
        this.drawHeader(g, mx, my, a);
        this.drawContent(g, mx, my, a);
        String hint = Lang.t("Linksklick an/aus   ·   Rechtsklick Einstellungen   ·   Mittelklick Taste");
        R2D.textCentered(g, this.font, hint, this.cx + this.cw / 2.0F, this.py + this.ph - 12.0F, 0.6F, Colors.alpha(-10263709, a), false);
        this.drawTooltip(g, mx, my, a);
    }

    private void drawSidebar(GuiGraphicsExtractor g, float mx, float my, float a) {
        float sx = this.px + 6.0F;
        float sy = this.py + 6.0F;
        float sbw = this.sw - 6.0F;
        R2D.round(g, sx, sy, sbw, this.ph - 12.0F, 7.0F, Colors.alpha(184549375, a));
        float logoScale = 1.35F;
        String word = "fokus";
        float lx = sx + 12.0F;
        float ly = this.py + 17.0F;
        R2D.text(g, this.font, word, lx, ly, logoScale, Colors.alpha(-723724, a), false);
        R2D.text(g, this.font, ".xyz", lx + this.font.width(word) * logoScale, ly, logoScale, Colors.alpha(-10263709, a), false);
        List<Category> cats = List.of(Category.values());
        boolean compact = this.ph < 280.0F;
        float y0 = this.py + (compact ? 40 : 46);
        float footerTop = this.py + this.ph - 56.0F;
        float rowH = Math.max(14.0F, Math.min(20.0F, (footerTop - y0) / cats.size() - 2.0F));
        float target = y0 + cats.indexOf(this.category) * (rowH + 2.0F);
        if (this.catIndicator.target() < 0.0F) {
            this.catIndicator.snap(target);
        }

        this.catIndicator.to(target);
        float indA = this.search.isEmpty() ? a : 0.0F;
        float iy = this.catIndicator.get();
        R2D.round(g, sx + 5.0F, iy, sbw - 10.0F, rowH, 5.0F, Colors.alpha(452984831, indA));
        R2D.round(g, sx + 5.0F, iy + rowH / 2.0F - 4.0F, 1.6F, 8.0F, 0.8F, Colors.alpha(-723724, indA));

        for (int i = 0; i < cats.size(); i++) {
            Category c = cats.get(i);
            float ry = y0 + i * (rowH + 2.0F);
            boolean hover = R2D.inside(mx, my, sx + 5.0F, ry, sbw - 10.0F, rowH);
            boolean selected = c == this.category && this.search.isEmpty();
            if (hover && !selected) {
                R2D.round(g, sx + 5.0F, ry, sbw - 10.0F, rowH, 5.0F, Colors.alpha(218103807, a));
            }

            int tc = selected ? -723724 : (hover ? -2697514 : -6052957);
            float ty = ry + rowH / 2.0F;
            Icons.category(g, c, sx + 13.0F, ty - 5.0F, Colors.alpha(tc, a));
            R2D.text(g, this.font, Lang.t(c.label), sx + 29.0F, ty - 3.5F, 0.85F, Colors.alpha(tc, a), false);
            int on = 0;

            for (Module m : Modules.in(c)) {
                if (m.isEnabled() && !m.isAlwaysOn()) {
                    on++;
                }
            }

            if (on > 0) {
                String n = String.valueOf(on);
                R2D.text(g, this.font, n, sx + sbw - 13.0F - this.font.width(n) * 0.65F, ty - 2.5F, 0.65F, Colors.alpha(-10263709, a), false);
            }

            this.hits.add(new FokusScreen.Hit(sx + 5.0F, ry, sbw - 10.0F, rowH, false, (b, x, y) -> this.selectCategory(c)));
        }

        float bx = sx + 6.0F;
        float by = this.py + this.ph - 50.0F;
        float bw = sbw - 12.0F;
        boolean hover = R2D.inside(mx, my, bx, by, bw, 16.0F);
        R2D.round(g, bx, by, bw, 16.0F, 5.0F, Colors.alpha(hover ? 520093695 : 318767103, a));
        R2D.roundEdge(g, bx, by, bw, 16.0F, 5.0F, Colors.alpha(285212671, a));
        R2D.textCentered(g, this.font, Lang.t("HUD-Editor"), bx + bw / 2.0F, by + 4.5F, 0.8F, Colors.alpha(hover ? -723724 : -3092272, a), false);
        this.hits.add(new FokusScreen.Hit(bx, by, bw, 16.0F, false, (b, x, y) -> this.minecraft.setScreenAndShow(new HudEditorScreen(this))));
        String link = "https://flexbio.link/1".replace("https://", "");
        float tw = this.font.width(link) * 0.65F;
        float tx = sx + sbw / 2.0F - tw / 2.0F;
        float tyy = this.py + this.ph - 28.0F;
        boolean linkHover = R2D.inside(mx, my, tx - 3.0F, tyy - 2.0F, tw + 6.0F, 9.0F);
        int lc = linkHover ? -723724 : -6052957;
        R2D.text(g, this.font, link, tx, tyy, 0.65F, Colors.alpha(lc, a), false);
        R2D.rect(g, tx, tyy + 6.4F, tx + tw, tyy + 6.9F, Colors.alpha(linkHover ? -723724 : 1090519039, a));
        this.hits
            .add(
                new FokusScreen.Hit(
                    tx - 3.0F, tyy - 2.0F, tw + 6.0F, 9.0F, false, (b, x, y) -> ConfirmLinkScreen.confirmLinkNow(this, URI.create("https://flexbio.link/1"))
                )
            );

        String ver = switch (Updater.state()) {
            case READY -> "v" + Updater.latestVersion() + "  ·  " + Lang.t("Update bereit - beim Neustart installiert");
            case DOWNLOADING -> Lang.t("Lädt Update…");
            case CHECKING -> Lang.t("Suche nach Updates…");
            default -> "v" + FokusMod.VERSION + "  ·  " + Compat.minecraftVersion();
        };
        R2D.textCentered(g, this.font, ver, sx + sbw / 2.0F, this.py + this.ph - 18.0F, 0.55F, Colors.alpha(-10263709, a), false);
    }

    private void drawHeader(GuiGraphicsExtractor g, float mx, float my, float a) {
        List<Module> list = this.visibleModules();
        String title = this.search.isEmpty() ? Lang.t(this.category.label) : Lang.t("Suche");
        R2D.text(g, this.font, title, this.cx, this.py + 13.0F, 1.3F, Colors.alpha(-723724, a), false);
        int on = 0;
        int total = 0;

        for (Module m : list) {
            if (!m.isAlwaysOn()) {
                total++;
                if (m.isEnabled()) {
                    on++;
                }
            }
        }

        String sub = this.search.isEmpty()
            ? (total == 0 ? Lang.t("Einstellungen der Mod") : on + " " + Lang.t("von") + " " + total + " " + Lang.t("aktiv"))
            : list.size() + " " + (list.size() == 1 ? Lang.t("Treffer").replace("results", "result") : Lang.t("Treffer"));
        R2D.text(g, this.font, sub, this.cx, this.py + 27.0F, 0.65F, Colors.alpha(-10263709, a), false);
        float bw = Math.min(130.0F, this.cw * 0.42F);
        float bx = this.px + this.pw - 12.0F - bw;
        float by = this.py + 13.0F;
        float bh = 17.0F;
        boolean hover = R2D.inside(mx, my, bx, by, bw, bh);
        R2D.round(g, bx, by, bw, bh, 5.0F, Colors.alpha(!hover && !this.searchFocus ? 318767103 : 520093695, a));
        R2D.roundEdge(g, bx, by, bw, bh, 5.0F, Colors.alpha(this.searchFocus ? 1442840575 : 285212671, a));
        Icons.search(g, bx + 6.0F, by + 4.0F, Colors.alpha(this.searchFocus ? -723724 : -6052957, a));
        String shown = this.search.isEmpty() && !this.searchFocus ? Lang.t("Suchen") : this.search;
        int tc = this.search.isEmpty() ? -10263709 : -723724;
        String trimmed = shown;

        while (this.font.width(trimmed) * 0.8F > bw - 26.0F && trimmed.length() > 1) {
            trimmed = trimmed.substring(1);
        }

        R2D.text(g, this.font, trimmed, bx + 19.0F, by + 5.0F, 0.8F, Colors.alpha(tc, a), false);
        if (this.searchFocus && System.currentTimeMillis() / 500L % 2L == 0L) {
            float caretX = bx + 19.0F + (this.search.isEmpty() ? 0.0F : this.font.width(trimmed) * 0.8F) + 1.0F;
            R2D.rect(g, caretX, by + 4.0F, caretX + 0.6F, by + 12.5F, Colors.alpha(-723724, a));
        }

        this.hits.add(new FokusScreen.Hit(bx, by, bw, bh, false, (b, x, y) -> {
            this.searchFocus = true;
            if (b == 1) {
                this.search = "";
            }
        }));
    }

    private List<Module> visibleModules() {
        if (this.search.isEmpty()) {
            return Modules.in(this.category);
        } else {
            String q = fold(this.search.trim());
            List<Module> out = new ArrayList<>();

            for (Module m : Modules.all()) {
                if (matches(m.name, q) || matches(m.description, q) || matches(m.category.label, q) || matches(m.group, q)
                        || m.settings().stream().anyMatch(st -> matches(st.name, q))) {
                    out.add(m);
                }
            }

            return out;
        }
    }

    private void drawContent(GuiGraphicsExtractor g, float mx, float my, float a) {
        List<Module> list = this.visibleModules();
        boolean overContent = R2D.inside(mx, my, this.cx - 2.0F, this.ct, this.cw + 8.0F, this.cb - this.ct);
        g.enableScissor(Math.round(this.cx - 4.0F), Math.round(this.ct), Math.round(this.cx + this.cw + 6.0F), Math.round(this.cb));
        if (list.isEmpty()) {
            R2D.textCentered(g, this.font, Lang.t("Nichts gefunden"), this.cx + this.cw / 2.0F, this.ct + 30.0F, 0.9F, Colors.alpha(-10263709, a), false);
        }

        int cols = this.cw >= 290.0F ? 2 : 1;
        float gap = 6.0F;
        float colW = (this.cw - gap * (cols - 1)) / cols;
        this.scrollTarget = Math.max(0.0F, Math.min(this.scrollTarget, this.maxScroll));
        this.scroll.to(this.scrollTarget);
        float y = this.ct + 2.0F - this.scroll.get();
        Module hovered = null;
        // Under headings when the category has groups and nothing is searched; one list otherwise.
        java.util.Map<String, List<Module>> groups = new java.util.LinkedHashMap<>();
        for (Module m : list) {
            groups.computeIfAbsent(this.search.isEmpty() && m.group != null ? m.group : "", k -> new ArrayList<>()).add(m);
        }
        boolean headings = groups.size() > 1 || !groups.containsKey("");
        int index = 0;

        for (java.util.Map.Entry<String, List<Module>> group : groups.entrySet()) {
            List<Module> part = group.getValue();
            if (headings && !group.getKey().isEmpty()) {
                float hy = y + 3.0F;
                String title = Lang.t(group.getKey()).toUpperCase(java.util.Locale.ROOT);
                R2D.text(g, this.font, title, this.cx + 2.0F, hy, 0.62F, Colors.alpha(-6052957, a), false);
                float lineX = this.cx + 2.0F + R2D.width(this.font, title) * 0.62F + 6.0F;
                R2D.rect(g, lineX, hy + 2.5F, this.cx + this.cw, hy + 3.0F, Colors.alpha(285212671, a));
                y += 14.0F;
            }

            for (int i = 0; i < part.size(); i += cols) {
                float rowH = 0.0F;

                for (int c = 0; c < cols && i + c < part.size(); c++) {
                    rowH = Math.max(rowH, this.cardHeight(part.get(i + c)));
                }

                for (int c = 0; c < cols && i + c < part.size(); c++) {
                    Module m = part.get(i + c);
                    if (this.drawCard(g, m, this.cx + c * (colW + gap), y, colW, index + i + c, mx, my, a, overContent)) {
                        hovered = m;
                    }
                }

                y += rowH + gap;
            }
            index += part.size();
        }

        this.maxScroll = Math.max(0.0F, y + this.scroll.get() - this.ct - (this.cb - this.ct));
        g.disableScissor();
        if (hovered != this.hoveredModule) {
            this.hoveredModule = hovered;
            this.hoverStart = System.currentTimeMillis();
        }

        if (this.maxScroll > 0.0F) {
            float trackH = this.cb - this.ct;
            this.trackX = this.cx + this.cw + 3.0F;
            this.thumbH = Math.max(18.0F, trackH * trackH / (trackH + this.maxScroll));
            this.thumbY = this.ct + (trackH - this.thumbH) * Math.min(1.0F, this.scroll.get() / this.maxScroll);
            boolean overBar = this.overScrollbar(mx, my);
            float hv = this.thumbHover.to(overBar || this.scrollDragging ? 1.0F : 0.0F).get();
            float w = 2.0F + hv * 1.5F;
            R2D.round(g, this.trackX, this.ct, w, trackH, w / 2.0F, Colors.alpha(0x10FFFFFF, a * hv));
            R2D.round(g, this.trackX, this.thumbY, w, this.thumbH, w / 2.0F,
                    Colors.alpha(Colors.lerp(0x50FFFFFF, 0xB0FFFFFF, hv), a));
        }
    }

    /** The scrollbar's strip, a little wider than drawn so it is easy to grab. */
    private boolean overScrollbar(float mx, float my) {
        return this.maxScroll > 0.0F && mx >= this.trackX - 3.0F && mx <= this.trackX + 7.0F && my >= this.ct
                && my <= this.cb;
    }

    /** Scrolls so the thumb's top sits at {@code top} (GUI units). */
    private void dragThumbTo(float top) {
        float room = this.cb - this.ct - this.thumbH;
        float f = room <= 0.0F ? 0.0F : (top - this.ct) / room;
        this.scrollTarget = Math.max(0.0F, Math.min(1.0F, f)) * this.maxScroll;
        // Follow the pointer at once while dragging, not with the soft scroll.
        this.scroll.snap(this.scrollTarget);
    }

    private float cardHeight(Module m) {
        float e = m.expandAnim.get();
        return 30.0F + (e > 0.001F ? this.settingsHeight(m) * e : 0.0F);
    }

    private List<Setting<?>> rows(Module m) {
        List<Setting<?>> out = new ArrayList<>();

        for (Setting<?> s : m.settings()) {
            if (s.isVisible()) {
                out.add(s);
            }
        }

        if (!m.isAlwaysOn()) {
            out.add(m.bind);
        }

        return out;
    }

    private float settingsHeight(Module m) {
        float h = 6.0F;

        for (Setting<?> s : this.rows(m)) {
            h += rowHeight(s);
        }

        return h + 2.0F;
    }

    private static float rowHeight(Setting<?> s) {
        if (s instanceof NumberSetting) {
            return 22.0F;
        } else if (s instanceof ColorSetting) {
            return 30.0F;
        } else {
            return s instanceof ActionSetting ? 20.0F : 15.0F;
        }
    }

    private boolean drawCard(GuiGraphicsExtractor g, Module m, float x, float y, float w, int index, float mx, float my, float a, boolean overContent) {
        float appear = Ease.outCubic((float)(System.currentTimeMillis() - this.categoryAt - index * 22L) / (220.0F * Theme.animTime()));
        float ca = a * appear;
        float yy = y + (1.0F - appear) * 6.0F;
        float h = this.cardHeight(m);
        if (!(yy > this.cb + 4.0F) && !(yy + h < this.ct - 4.0F) && !(ca <= 0.01F)) {
            boolean hover = overContent && R2D.inside(mx, my, x, yy, w, 28.0F);
            float hv = m.hoverAnim.to(hover ? 1.0F : 0.0F).get();
            float tog = m.toggleAnim.to(m.isEnabled() ? 1.0F : 0.0F).get();
            R2D.round(g, x, yy, w, h, 6.0F, Colors.alpha(Colors.lerp(201326591, 385875967, hv), ca));
            R2D.roundEdge(g, x, yy, w, h, 6.0F, Colors.alpha(Colors.lerp(285212671, 587202559, Math.max(hv, tog * 0.6F)), ca));
            int nameColor = m.isAlwaysOn() ? -723724 : Colors.lerp(-3552823, -723724, tog);
            R2D.text(g, this.font, Lang.t(m.name), x + 9.0F, yy + 7.0F, 0.9F, Colors.alpha(nameColor, ca), false);
            if (m.fresh) {
                String badge = Lang.t("NEU");
                float bx = x + 9.0F + R2D.width(this.font, Lang.t(m.name)) * 0.9F + 4.0F;
                float bw = R2D.width(this.font, badge) * 0.5F + 6.0F;
                R2D.round(g, bx, yy + 7.0F, bw, 7.0F, 3.5F, Colors.alpha(Theme.accent(0.0F), ca));
                R2D.textCentered(g, this.font, badge, bx + bw / 2.0F, yy + 8.6F, 0.5F, Colors.alpha(-15856114, ca), false);
            }
            R2D.text(
                g, this.font, R2D.trim(this.font, Lang.t(m.description), w - 62.0F, 0.62F), x + 9.0F, yy + 17.5F, 0.62F, Colors.alpha(-10263709, ca), false
            );
            float rightEdge = x + w - 9.0F;
            if (!m.isAlwaysOn()) {
                float tw = 18.0F;
                float th = 9.0F;
                float tx = rightEdge - tw;
                float ty = yy + 9.5F;
                R2D.round(g, tx, ty, tw, th, 4.5F, Colors.alpha(Colors.lerp(788529151, Theme.accent(0.0F), tog), ca));
                R2D.circle(g, tx + 4.5F + tog * 9.0F, ty + 4.5F, 3.2F, Colors.alpha(Colors.lerp(-4342339, -15856114, tog), ca));
                rightEdge = tx - 6.0F;
            }

            boolean hasSettings = !this.rows(m).isEmpty();
            float e = m.expandAnim.get();
            if (hasSettings) {
                float chx = rightEdge - 3.0F;
                float chy = yy + 14.0F;
                float d = 1.4F * (1.0F - 2.0F * e);
                boolean chHover = overContent && R2D.inside(mx, my, chx - 6.0F, yy + 6.0F, 12.0F, 16.0F);
                int cc = Colors.alpha(chHover ? -723724 : -6052957, ca);
                R2D.line(g, chx - 2.5F, chy - d, chx, chy + d, 0.9F, cc);
                R2D.line(g, chx, chy + d, chx + 2.5F, chy - d, 0.9F, cc);
                rightEdge = chx - 8.0F;
            }

            if (!m.isAlwaysOn() && (m.bind.get() >= 0 || this.listening == m.bind)) {
                boolean listen = this.listening == m.bind;
                String label = listen ? "…" : m.bind.display();
                float lw = this.font.width(label) * 0.6F + 6.0F;
                float lx = rightEdge - lw;
                R2D.round(g, lx, yy + 9.5F, lw, 9.0F, 3.0F, Colors.alpha(listen ? 1090519039 : 318767103, ca));
                R2D.textCentered(g, this.font, label, lx + lw / 2.0F, yy + 11.8F, 0.6F, Colors.alpha(-6052957, ca), false);
            }

            this.hits.add(new FokusScreen.Hit(x, yy, w, 28.0F, true, (b, x2, y2) -> {
                this.seen(m);
                if (b == 1) {
                    this.toggleExpand(m);
                } else if (b == 2 && !m.isAlwaysOn()) {
                    this.listening = m.bind;
                } else if (b == 0) {
                    if (m.isAlwaysOn()) {
                        this.toggleExpand(m);
                    } else {
                        m.toggle();
                        this.click();
                        ConfigIO.save();
                    }
                }
            }));
            if (hasSettings) {
                float chx = (m.isAlwaysOn() ? x + w - 9.0F : x + w - 9.0F - 18.0F - 6.0F) - 3.0F;
                this.hits.add(new FokusScreen.Hit(chx - 7.0F, yy + 4.0F, 14.0F, 20.0F, true, (b, x2, y2) -> this.toggleExpand(m)));
            }

            if (e > 0.005F) {
                float top = yy + 28.0F;
                g.enableScissor(Math.round(x), Math.round(top), Math.round(x + w), Math.round(yy + h));
                R2D.rect(g, x + 9.0F, top, x + w - 9.0F, top + 0.5F, Colors.alpha(285212671, ca * e));
                float sy = top + 5.0F;
                boolean interactive = e > 0.85F && overContent;

                for (Setting<?> s : this.rows(m)) {
                    this.drawSetting(g, s, x + 10.0F, sy, w - 20.0F, ca * e, mx, my, interactive, m);
                    sy += rowHeight(s);
                }

                g.disableScissor();
            }

            return hover;
        } else {
            return false;
        }
    }

    private void toggleExpand(Module m) {
        this.seen(m);
        m.expandAnim.to(m.expandAnim.target() > 0.5F ? 0.0F : 1.0F);
    }

    /** Touching a new module takes its "NEU" away for good. */
    private void seen(Module m) {
        if (m.fresh) {
            m.fresh = false;
            ConfigIO.save();
        }
    }

    private void drawSetting(GuiGraphicsExtractor g, Setting<?> s, float x, float y, float w, float a, float mx, float my, boolean interactive, Module owner) {
        int label = Colors.alpha(-4276546, a);
        boolean hover = interactive && R2D.inside(mx, my, x - 4.0F, y, w + 8.0F, rowHeight(s) - 1.0F);
        float hv = s.hover.to(hover ? 1.0F : 0.0F).get();
        if (hv > 0.01F) {
            R2D.round(g, x - 4.0F, y - 1.0F, w + 8.0F, rowHeight(s) - 1.0F, 4.0F, Colors.alpha(184549375, a * hv));
        }

        String name = Lang.t(s == owner.bind ? "Taste zum An/Aus" : s.label());
        int accent = Theme.accent(0.0F);
        if (s instanceof BoolSetting b) {
            float k = b.knob.to(b.on() ? 1.0F : 0.0F).get();
            R2D.text(g, this.font, R2D.trim(this.font, name, w - 20.0F, 0.75F), x, y + 4.0F, 0.75F, label, false);
            float tx = x + w - 14.0F;
            float ty = y + 3.5F;
            R2D.round(g, tx, ty, 14.0F, 7.0F, 3.5F, Colors.alpha(Colors.lerp(788529151, accent, k), a));
            R2D.circle(g, tx + 3.5F + k * 7.0F, ty + 3.5F, 2.5F, Colors.alpha(Colors.lerp(-4342339, -15856114, k), a));
            if (interactive) {
                this.hits.add(new FokusScreen.Hit(x - 4.0F, y, w + 8.0F, 14.0F, true, (btn, a2, b2) -> b.toggle()));
            }
        } else if (s instanceof NumberSetting n) {
            String v = n.display();
            R2D.text(g, this.font, R2D.trim(this.font, name, w - this.font.width(v) * 0.75F - 6.0F, 0.75F), x, y + 2.0F, 0.75F, label, false);
            R2D.text(g, this.font, v, x + w - this.font.width(v) * 0.75F, y + 2.0F, 0.75F, Colors.alpha(-723724, a), false);
            float f = n.fill.to((float)n.progress()).get();
            float ty = y + 13.0F;
            R2D.round(g, x, ty, w, 2.0F, 1.0F, Colors.alpha(788529151, a));
            R2D.round(g, x, ty, Math.max(2.0F, w * f), 2.0F, 1.0F, Colors.alpha(accent, a));
            boolean active = this.dragging == n || hover;
            R2D.circle(g, x + w * f, ty + 1.0F, active ? 3.6F : 3.0F, Colors.alpha(-723724, a));
            if (interactive) {
                this.hits.add(new FokusScreen.Hit(x - 4.0F, y + 7.0F, w + 8.0F, 13.0F, true, (btn, px2, py2x) -> {
                    this.dragging = n;
                    this.dragX = x;
                    this.dragW = w;
                    n.setProgress((px2 - x) / w);
                }));
            }
        } else if (s instanceof ModeSetting mode) {
            String v = R2D.trim(this.font, Lang.t(mode.get()), w * 0.62F - 20.0F, 0.75F);
            float pw2 = R2D.width(this.font, v) * 0.75F + 20.0F;
            R2D.text(g, this.font, R2D.trim(this.font, name, w - pw2 - 6.0F, 0.75F), x, y + 4.0F, 0.75F, label, false);
            float bx = x + w - pw2;
            R2D.round(g, bx, y + 1.5F, pw2, 11.0F, 4.0F, Colors.alpha(318767103, a));
            R2D.textCentered(g, this.font, v, bx + pw2 / 2.0F, y + 4.2F, 0.75F, Colors.alpha(-723724, a), false);
            R2D.text(g, this.font, "‹", bx + 3.5F, y + 3.2F, 0.8F, Colors.alpha(-6052957, a), false);
            R2D.text(g, this.font, "›", bx + pw2 - 6.5F, y + 3.2F, 0.8F, Colors.alpha(-6052957, a), false);
            if (interactive) {
                this.hits
                    .add(new FokusScreen.Hit(x - 4.0F, y, w + 8.0F, 14.0F, true, (btn, px2, py2x) -> mode.cycle(btn != 1 && !(px2 < bx + pw2 / 2.0F) ? 1 : -1)));
            }
        } else if (s instanceof ColorSetting c) {
            R2D.text(g, this.font, R2D.trim(this.font, name, w - 34.0F, 0.75F), x, y + 2.0F, 0.75F, label, false);
            float sx = x + w - 4.0F;
            R2D.circle(g, sx, y + 5.0F, 4.2F, Colors.alpha(1627389951, a));
            R2D.circle(g, sx, y + 5.0F, 3.4F, Colors.alpha(c.color(0.0F), a));
            String rgb = "RGB";
            float rw = this.font.width(rgb) * 0.6F + 6.0F;
            float rx = sx - 8.0F - rw;
            R2D.round(g, rx, y + 1.0F, rw, 8.0F, 3.0F, Colors.alpha(c.rainbow() ? 1442840575 : 318767103, a));
            R2D.textCentered(g, this.font, rgb, rx + rw / 2.0F, y + 2.8F, 0.6F, Colors.alpha(-6052957, a), false);
            float py2 = y + 18.0F;
            int[] presets = ColorSetting.PRESETS;

            for (int i = 0; i < presets.length; i++) {
                float ccx = x + 4.0F + i * 10.5F;
                boolean sel = !c.rainbow() && c.get() == presets[i];
                if (sel) {
                    Icons.ring(g, ccx, py2, 4.6F, 0.6F, Colors.alpha(-723724, a));
                }

                R2D.circle(g, ccx, py2, 3.5F, Colors.alpha(presets[i], a));
                if (interactive) {
                    int preset = presets[i];
                    this.hits.add(new FokusScreen.Hit(ccx - 5.0F, py2 - 5.0F, 10.0F, 10.0F, true, (btn, a2, b2) -> {
                        c.set(preset);
                        c.setRainbow(false);
                    }));
                }
            }

            float hx = x + 4.0F + presets.length * 10.5F + 2.0F;
            float hw = x + w - hx;
            if (hw > 12.0F) {
                int strips = (int)(hw * 2.0F);

                for (int i = 0; i < strips; i++) {
                    float f0 = (float)i / strips;
                    R2D.rect(g, hx + f0 * hw, py2 - 1.5F, hx + (float)(i + 1) / strips * hw, py2 + 1.5F, Colors.alpha(Colors.hsv(f0, 0.55F, 1.0F), a * 0.9F));
                }

                if (!c.rainbow()) {
                    float mxh = hx + Colors.hue(c.get()) * hw;
                    R2D.round(g, mxh - 1.0F, py2 - 3.5F, 2.0F, 7.0F, 1.0F, Colors.alpha(-723724, a));
                }

                if (interactive) {
                    this.hits.add(new FokusScreen.Hit(hx - 2.0F, py2 - 5.0F, hw + 4.0F, 10.0F, true, (btn, px3, py3) -> {
                        this.hueDragging = c;
                        this.hueX = hx;
                        this.hueW = hw;
                        setHue(c, (px3 - hx) / hw);
                    }));
                }
            }

            if (interactive) {
                this.hits.add(new FokusScreen.Hit(rx - 2.0F, y, rw + 4.0F, 10.0F, true, (btn, a2, b2) -> c.setRainbow(!c.rainbow())));
            }
        } else if (s instanceof KeySetting k) {
            boolean listen = this.listening == k;
            String v = listen ? Lang.t("Taste drücken…") : k.display();
            float kw = this.font.width(v) * 0.75F + 10.0F;
            R2D.text(g, this.font, R2D.trim(this.font, name, w - kw - 6.0F, 0.75F), x, y + 4.0F, 0.75F, label, false);
            float bx = x + w - kw;
            R2D.round(g, bx, y + 1.5F, kw, 11.0F, 3.5F, Colors.alpha(listen ? 1090519039 : 318767103, a));
            R2D.textCentered(g, this.font, v, bx + kw / 2.0F, y + 4.2F, 0.75F, Colors.alpha(-723724, a), false);
            if (interactive) {
                this.hits.add(new FokusScreen.Hit(x - 4.0F, y, w + 8.0F, 14.0F, true, (btn, px2, py2x) -> {
                    if (btn == 1) {
                        k.set(-1);
                        this.listening = null;
                    } else {
                        this.listening = k;
                    }
                }));
            }
        } else if (s instanceof TextSetting t) {
            boolean typing = this.editing == t;
            float fw = Math.max(60.0F, w * 0.55F);
            float fx = x + w - fw;
            R2D.text(g, this.font, R2D.trim(this.font, name, w - fw - 6.0F, 0.75F), x, y + 4.0F, 0.75F, label, false);
            R2D.round(g, fx, y + 1.5F, fw, 11.0F, 3.5F, Colors.alpha(typing ? 520093695 : 318767103, a));
            R2D.roundEdge(g, fx, y + 1.5F, fw, 11.0F, 3.5F, Colors.alpha(typing ? 1442840575 : 285212671, a));
            String v = t.get();
            boolean empty = v.isEmpty() && !typing;
            String shown = empty ? Lang.t("leer") : v;
            // The end of a long text stays in view while typing.
            String fit = typing ? tail(shown, fw - 10.0F, 0.72F) : R2D.trim(this.font, shown, fw - 8.0F, 0.72F);
            R2D.text(g, this.font, fit, fx + 4.0F, y + 4.4F, 0.72F, Colors.alpha(empty ? -10263709 : -723724, a), false);
            if (typing && System.currentTimeMillis() / 500L % 2L == 0L) {
                float cx = fx + 4.0F + R2D.width(this.font, fit) * 0.72F + 0.5F;
                R2D.rect(g, cx, y + 3.5F, cx + 0.6F, y + 10.5F, Colors.alpha(-723724, a));
            }
            if (interactive) {
                this.hits.add(new FokusScreen.Hit(x - 4.0F, y, w + 8.0F, 14.0F, true, (btn, px2, py2x) -> {
                    if (btn == 1) {
                        t.set("");
                        ConfigIO.save();
                    } else {
                        this.editing = t;
                    }
                }));
            }
        } else if (s instanceof ActionSetting act) {
            float bh = 15.0F;
            R2D.round(g, x, y + 1.0F, w, bh, 4.0F, Colors.alpha(hover ? 520093695 : 318767103, a));
            R2D.roundEdge(g, x, y + 1.0F, w, bh, 4.0F, Colors.alpha(285212671, a));
            R2D.textCentered(g, this.font, R2D.trim(this.font, name, w - 8.0F, 0.75F), x + w / 2.0F, y + 5.0F, 0.75F, Colors.alpha(-723724, a), false);
            if (interactive) {
                this.hits.add(new FokusScreen.Hit(x, y + 1.0F, w, bh, true, (btn, px2, py2x) -> act.run()));
            }
        }
    }

    /** As much of the end of s as fits: what is being typed stays visible. */
    private String tail(String s, float maxWidth, float scale) {
        String out = s;
        while (!out.isEmpty() && R2D.width(this.font, out) * scale > maxWidth) {
            out = out.substring(out.offsetByCodePoints(0, 1));
        }
        return out;
    }

    private static void setHue(ColorSetting c, float f) {
        c.setRainbow(false);
        c.set(Colors.hsv(Ease.clamp01(f), 0.55F, 1.0F));
    }

    private void drawTooltip(GuiGraphicsExtractor g, float mx, float my, float a) {
        if (this.hoveredModule != null && System.currentTimeMillis() - this.hoverStart >= 700L) {
            Module m = this.hoveredModule;
            String line2 = m.isAlwaysOn() ? Lang.t("Klicken zum Öffnen") : Lang.t("Taste: ") + m.bind.display();
            String desc = Lang.t(m.description);
            float w = Math.max(this.font.width(desc) * 0.7F, this.font.width(line2) * 0.65F) + 12.0F;
            float h = 22.0F;
            float tx = Math.min(mx + 10.0F, this.px + this.pw - w - 4.0F);
            float ty = my + 12.0F;
            float fade = Ease.clamp01((float)(System.currentTimeMillis() - this.hoverStart - 700L) / 140.0F) * a;
            R2D.round(g, tx, ty, w, h, 5.0F, Colors.alpha(-301200371, fade));
            R2D.roundEdge(g, tx, ty, w, h, 5.0F, Colors.alpha(620756991, fade));
            R2D.text(g, this.font, desc, tx + 6.0F, ty + 4.5F, 0.7F, Colors.alpha(-723724, fade), false);
            R2D.text(g, this.font, line2, tx + 6.0F, ty + 13.0F, 0.65F, Colors.alpha(-10263709, fade), false);
        }
    }

    /** Found in German or English, whichever language is chosen. */
    private static boolean matches(String german, String q) {
        return german != null && (fold(german).contains(q) || fold(Lang.en(german)).contains(q));
    }

    /** Lower case, umlauts and ß spelt out, so "aussen" finds "außen". */
    private static String fold(String s) {
        return s.toLowerCase(Locale.ROOT).replace("ä", "a").replace("ö", "o").replace("ü", "u").replace("ß", "ss");
    }

    private void click() {
        if (Theme.clickSound()) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void selectCategory(Category c) {
        if (c != this.category || !this.search.isEmpty()) {
            this.category = c;
            lastCategory = c;
            this.categoryAt = System.currentTimeMillis();
            this.scrollTarget = 0.0F;
            this.scroll.snap(0.0F);
        }

        this.search = "";
        this.searchFocus = false;
    }

    private static int button(MouseButtonEvent e) {
        int b = e.button();
        if (b == Compat.MOUSE_RIGHT) {
            return 1;
        } else if (b == Compat.MOUSE_MIDDLE) {
            return 2;
        } else {
            return b == Compat.MOUSE_LEFT ? 0 : -1;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.closingAt >= 0L) {
            return true;
        } else {
            float mx = this.localX(event.x());
            float my = this.localY(event.y());
            int b = button(event);
            this.listening = null;
            this.searchFocus = false;
            if (this.editing != null) {
                this.editing = null;
                ConfigIO.save();
            }
            if (b == 0 && this.overScrollbar(mx, my)) {
                this.scrollDragging = true;
                boolean onThumb = my >= this.thumbY && my <= this.thumbY + this.thumbH;
                this.scrollGrab = onThumb ? my - this.thumbY : this.thumbH / 2.0F;
                if (!onThumb) {
                    this.dragThumbTo(my - this.scrollGrab);
                }
                return true;
            }

            boolean inContent = R2D.inside(mx, my, this.cx - 4.0F, this.ct, this.cw + 10.0F, this.cb - this.ct);

            for (int i = this.hits.size() - 1; i >= 0; i--) {
                FokusScreen.Hit h = this.hits.get(i);
                if ((!h.clipped || inContent) && R2D.inside(mx, my, h.x, h.y, h.w, h.h)) {
                    h.action.click(b, mx, my);
                    return true;
                }
            }

            if (!R2D.inside(mx, my, this.px, this.py, this.pw, this.ph)) {
                this.close();
            }

            return true;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrollDragging = false;
        if (this.dragging != null || this.hueDragging != null) {
            this.dragging = null;
            this.hueDragging = null;
            ConfigIO.save();
        }

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        float mx = this.localX(event.x());
        if (this.scrollDragging) {
            this.dragThumbTo(this.localY(event.y()) - this.scrollGrab);
            return true;
        }
        if (this.dragging != null) {
            this.dragging.setProgress((mx - this.dragX) / this.dragW);
        } else if (this.hueDragging != null) {
            setHue(this.hueDragging, (mx - this.hueX) / this.hueW);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float steps = (float)Math.max(-2.0, Math.min(2.0, scrollY));
        float step = 14.0F * Theme.scrollSpeed();
        this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget - steps * step));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (this.editing != null) {
            if (key == Compat.KEY_ESCAPE || key == Compat.KEY_RETURN) {
                this.editing = null;
                ConfigIO.save();
            } else if (key == Compat.KEY_BACKSPACE) {
                this.editing.backspace();
            }
            return true;
        } else if (this.listening != null) {
            if (key != Compat.KEY_ESCAPE && key != Compat.KEY_BACKSPACE && key != Compat.KEY_DELETE) {
                this.listening.set(key);
            } else {
                this.listening.set(-1);
            }

            this.listening.clearEdge();
            this.listening = null;
            ConfigIO.save();
            return true;
        } else if (this.searchFocus) {
            if (key == Compat.KEY_ESCAPE) {
                if (this.search.isEmpty()) {
                    this.searchFocus = false;
                } else {
                    this.search = "";
                }
            } else if (key == Compat.KEY_BACKSPACE && !this.search.isEmpty()) {
                this.search = this.search.substring(0, this.search.length() - 1);
                this.categoryAt = System.currentTimeMillis();
            } else if (key == Compat.KEY_RETURN) {
                this.searchFocus = false;
            }

            return true;
        } else {
            if (key == Compat.KEY_ESCAPE || key == Modules.get(InterfaceModule.class).menuKey.get()) {
                this.close();
            }

            return true;
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.listening != null || this.closingAt >= 0L) {
            return true;
        } else if (this.editing != null) {
            if (event.isAllowedChatCharacter()) {
                this.editing.type(event.codepointAsString());
            }
            return true;
        } else if (!event.isAllowedChatCharacter()) {
            return false;
        } else {
            String ch = event.codepointAsString();
            if (!this.searchFocus && ch.isBlank()) {
                return true;
            } else {
                this.searchFocus = true;
                if (this.search.length() < 24) {
                    this.search = this.search + ch;
                    this.scrollTarget = 0.0F;
                    this.categoryAt = System.currentTimeMillis();
                }

                return true;
            }
        }
    }

    private interface Click {
        void click(int var1, float var2, float var3);
    }

    private record Hit(float x, float y, float w, float h, boolean clipped, FokusScreen.Click action) {
    }
}
