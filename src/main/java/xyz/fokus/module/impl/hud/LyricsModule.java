package xyz.fokus.module.impl.hud;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import xyz.fokus.Lang;
import xyz.fokus.SkyAnchor;
import xyz.fokus.lyrics.CoverArt;
import xyz.fokus.lyrics.LyricsSource;
import xyz.fokus.lyrics.MediaWatcher;
import xyz.fokus.module.HudModule;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class LyricsModule extends HudModule {
    public final ModeSetting place = this.add(new ModeSetting("Ort", "Himmel - folgt dem Blick", "Himmel - folgt dem Blick", "Himmel - fest", "Bildschirm"));
    public final NumberSetting elevation = this.add(new NumberSetting("Höhe am Himmel", 20.0, 5.0, 80.0, 1.0, "°"))
        .visibleWhen(() -> !this.place.is("Bildschirm"));
    public final NumberSetting follow = this.add(new NumberSetting("Nachziehen", 1.5, 0.2, 8.0, 0.1, "x"))
        .visibleWhen(() -> this.place.is("Himmel - folgt dem Blick"));
    public final NumberSetting textSize = this.add(new NumberSetting("Schriftgröße", 1.1, 0.5, 3.0, 0.1, "x"));
    public final ModeSetting typeface = this.add(new ModeSetting("Schrift", "Glatt", "Glatt", "Minecraft"));
    public final ModeSetting style = this.add(new ModeSetting("Darstellung", "Karaoke", "Karaoke", "Ganze Zeile"));
    public final BoolSetting glow = this.add(new BoolSetting("Leuchten", true));
    public final BoolSetting next = this.add(new BoolSetting("Nächste Zeile zeigen", true));
    public final BoolSetting title = this.add(new BoolSetting("Songtitel zeigen", true));
    public final BoolSetting cover = this.add(new BoolSetting("Cover zeigen", true)).visibleWhen(() -> this.place.is("Bildschirm"));
    public final BoolSetting secondSource = this.add(new BoolSetting("Zweite Quelle (NetEase)", true));
    public final NumberSetting offset = this.add(new NumberSetting("Versatz", 0.0, -3.0, 3.0, 0.1, " s"));
    private static final float WIDTH = 300.0F;
    private static final int COVER = 20;
    private final Anim show = new Anim(0.0F, 6.0F);
    private final Anim slide = new Anim(0.0F, 9.0F);
    private int lastIndex = -1;
    private String lastSong = "";
    private final SkyAnchor anchor = new SkyAnchor();

    public LyricsModule() {
        super("Lyrics", "Liedtext des Songs, den du hörst, am Himmel (Spotify, Browser …; Text von lrclib.net).", 0.5F, 0.06F);
        this.centered = true;
        this.background.setDefault(false);
        this.enabledByDefault();
    }

    @Override
    public void onTick(Minecraft mc) {
        LyricsSource.secondSource = this.secondSource.on();
        MediaWatcher.start();
    }

    @Override
    public boolean placesItself() {
        return !this.place.is("Bildschirm");
    }

    private LyricsModule.View view(boolean editor) {
        MediaWatcher.Track track = MediaWatcher.current();
        if (track == null) {
            return editor
                ? new LyricsModule.View("Artist – Song", Lang.t("Die aktuelle Zeile steht hier"), Lang.t("und die nächste darunter"), 0.6F, true, false, false)
                : null;
        } else {
            String artist = track.cleanArtist();
            String song = artist.isEmpty() ? track.title() : artist + " – " + track.title();
            if (!song.equals(this.lastSong)) {
                this.lastSong = song;
                this.anchor.reset();
            }

            List<LyricsSource.Line> lines = track.live() ? List.of() : LyricsSource.forTrack(track);
            long now = Math.round((track.positionNow() + this.offset.get()) * 1000.0);
            int index = -1;
            int i = 0;

            while (i < lines.size() && lines.get(i).ms() <= now) {
                index = i++;
            }

            if (index != this.lastIndex) {
                this.lastIndex = index;
                this.slide.snap(1.0F);
            }

            String current = index >= 0 ? lines.get(index).text() : "";
            String upcoming = index + 1 < lines.size() ? lines.get(index + 1).text() : "";
            float progress = 1.0F;
            if (index >= 0) {
                long start = lines.get(index).ms();
                long end = index + 1 < lines.size() ? lines.get(index + 1).ms() : start + 4000L;
                progress = Math.max(0.0F, Math.min(1.0F, (float)(now - start) / (float)Math.max(1L, end - start)));
            }

            boolean loading = !track.live() && LyricsSource.loading();
            return new LyricsModule.View(song, current, upcoming, progress, track.playing(), loading, !loading && lines.isEmpty());
        }
    }

    @Override
    public void drawPlaced(GuiGraphicsExtractor g, Font font) {
        LyricsModule.View v = this.view(false);
        this.show.to(v != null && v.playing ? 1.0F : 0.0F);
        float a = this.show.get();
        float[] p = this.anchor.place(this.place.get(), this.elevation.f(), this.follow.f(), g.guiWidth(), g.guiHeight());
        if (!(a < 0.02F) && v != null && p != null) {
            float edge = p[2];
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(p[0], p[1]);
            float s = this.scale.f();
            pose.scale(s, s);
            this.paint(g, font, v, a * edge, -150.0F, -12.0F);
            pose.popMatrix();
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        this.w = 300.0F;
        this.h = 34.0F;
        LyricsModule.View v = this.view(editor);
        this.show.to(v != null && v.playing ? 1.0F : 0.0F);
        float a = editor ? 1.0F : this.show.get();
        if (!(a < 0.02F) && v != null) {
            boolean art = this.cover.on() && !editor && CoverArt.ready();
            if (art) {
                this.w = 326.0F;
            }

            if (this.background.on()) {
                this.box(g, this.w, this.h);
            }

            float left = this.paint(g, font, v, a, art ? 26.0F : 0.0F, 2.0F);
            if (art) {
                int x = Math.max(4, Math.round(left - 20.0F - 6.0F));
                CoverArt.draw(g, x, Math.round((this.h - 20.0F) / 2.0F), 20, a);
            }
        }
    }

    private float paint(GuiGraphicsExtractor g, Font font, LyricsModule.View v, float a, float x0, float y0) {
        R2D.face(this.typeface.is("Glatt") ? R2D.SMOOTH : null);

        float var7;
        try {
            var7 = this.paintLines(g, font, v, a, x0, y0);
        } finally {
            R2D.face(null);
        }

        return var7;
    }

    private float paintLines(GuiGraphicsExtractor g, Font font, LyricsModule.View v, float a, float x0, float y0) {
        float left = x0 + 150.0F;
        this.slide.to(0.0F);
        float sl = this.slide.get();
        float cx = x0 + 150.0F;
        int white = this.text();
        int gray = this.label();
        float big = this.textSize.f() * 0.75F;
        float y = y0;
        if (this.title.on() && !v.title.isEmpty()) {
            String t = R2D.trim(font, "♪ " + v.title, 290.0F, 0.55F);
            R2D.textCentered(g, font, t, cx, y0, 0.55F, Colors.alpha(gray, a * 0.9F), true);
            left = Math.min(left, cx - R2D.width(font, t) * 0.55F / 2.0F);
            y = y0 + 7.0F;
        }

        if (!v.current.isEmpty()) {
            float ly = y + sl * 8.0F;
            float la = a * (1.0F - sl * 0.7F);
            String line = R2D.trim(font, v.current, 360.0F, big);
            left = Math.min(left, cx - R2D.width(font, line) * big / 2.0F);
            if (this.glow.on()) {
                int halo = Colors.alpha(white, la * 0.12F);

                for (float[] o : new float[][]{{-1.0F, 0.0F}, {1.0F, 0.0F}, {0.0F, -1.0F}, {0.0F, 1.0F}}) {
                    R2D.textCentered(g, font, line, cx + o[0], ly + o[1], big, halo, false);
                }
            }

            if (this.style.is("Karaoke") && !R2D.rightToLeft(line)) {
                karaoke(g, font, line, cx, ly, big, v.progress, Colors.alpha(white, la), Colors.alpha(gray, la));
            } else {
                R2D.textCentered(g, font, line, cx, ly, big, Colors.alpha(white, la), true);
            }

            y += 9.0F * big + 3.0F;
        } else if (v.loading) {
            R2D.textCentered(g, font, "…", cx, y, big, Colors.alpha(gray, a), true);
            y += 9.0F * big + 3.0F;
        } else if (v.none) {
            String note = Lang.t("Kein Liedtext gefunden");
            R2D.textCentered(g, font, note, cx, y, 0.6F, Colors.alpha(gray, a * 0.6F), true);
            left = Math.min(left, cx - R2D.width(font, note) * 0.6F / 2.0F);
            y += 8.0F;
        }

        if (this.next.on() && !v.upcoming.isEmpty()) {
            float small = Math.max(0.5F, big * 0.6F);
            String linex = R2D.trim(font, v.upcoming, 360.0F, small);
            R2D.textCentered(g, font, linex, cx, y + sl * 6.0F, small, Colors.alpha(gray, a * 0.7F * (1.0F - sl * 0.5F)), true);
        }

        return left;
    }

    private static void karaoke(GuiGraphicsExtractor g, Font font, String line, float cx, float y, float scale, float progress, int lit, int dim) {
        String[] words = line.split(" ");
        float total = R2D.width(font, line) * scale;
        float x = cx - total / 2.0F;
        int litCount = (int)Math.ceil(progress * words.length * 1.1F);

        for (int i = 0; i < words.length; i++) {
            String w = i < words.length - 1 ? words[i] + " " : words[i];
            R2D.text(g, font, w, x, y, scale, i < litCount ? lit : dim, true);
            x += R2D.width(font, w) * scale;
        }
    }

    private record View(String title, String current, String upcoming, float progress, boolean playing, boolean loading, boolean none) {
    }
}
