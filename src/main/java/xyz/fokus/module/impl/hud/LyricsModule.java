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
    private boolean lastPause;
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
                this.lastPause = false;
                this.slide.snap(1.0F);
            }

            // Lines that only mark an instrumental ("♪") show as the breathing notes, and the preview
            // skips them for the next sung line.
            String current = index >= 0 && !instrumental(lines.get(index).text()) ? lines.get(index).text() : "";
            String upcoming = "";
            for (int k = index + 1; k < lines.size(); k++) {
                if (!instrumental(lines.get(k).text())) {
                    upcoming = lines.get(k).text();
                    break;
                }
            }
            float progress = 1.0F;
            if (index >= 0) {
                long start = lines.get(index).ms();
                long end = index + 1 < lines.size() ? lines.get(index + 1).ms() : start + 4000L;
                long sung = sungFor(current, end - start);
                progress = Math.max(0.0F, Math.min(1.0F, (float)(now - start) / (float)sung));
                // A long instrumental after the line: the line goes, the notes and the next line show.
                long pauseFrom = start + sung + PAUSE_AFTER;
                boolean pause = !current.isEmpty() && now > pauseFrom && end - pauseFrom > PAUSE_BEFORE;
                if (pause) {
                    current = "";
                }
                if (pause != this.lastPause) {
                    this.lastPause = pause;
                    this.slide.snap(1.0F);
                }
            }

            boolean loading = !track.live() && LyricsSource.loading();
            return new LyricsModule.View(song, current, upcoming, progress, track.playing(), loading, !loading && lines.isEmpty());
        }
    }

    /** How long after a line has been sung it stays before an instrumental pause takes over. */
    private static final long PAUSE_AFTER = 3000L;
    /** The pause shows only when it lasts at least this long; a shorter gap keeps the line. */
    private static final long PAUSE_BEFORE = 1500L;

    /** A line with nothing to sing: empty, or only music symbols. */
    static boolean instrumental(String text) {
        return text.replaceAll("[\\s\u2669\u266a\u266b\u266c\\x{1F3B5}\\x{1F3B6}]", "").isEmpty();
    }

    /**
     * How long the line is sung: the time to the next line, but no longer than its text takes to sing -
     * otherwise the karaoke crawls through the silence before the next line.
     */
    static long sungFor(String text, long gap) {
        long estimate = 400L;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            // In Chinese, Japanese and Korean one character is a whole syllable.
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);
            boolean syllable = script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL;
            estimate += syllable ? 280L : 95L;
        }
        return Math.max(1L, Math.min(gap, Math.max(1200L, estimate)));
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
            // A long line wraps onto a second one instead of being cut off.
            java.util.List<String> rows = wrap(font, v.current, 300.0F, big);
            boolean karaoke = this.style.is("Karaoke") && !R2D.rightToLeft(v.current);
            int total = 0;
            for (String row : rows) {
                total += row.length();
            }
            int before = 0;
            for (String row : rows) {
                left = Math.min(left, cx - R2D.width(font, row) * big / 2.0F);
                if (this.glow.on()) {
                    int halo = Colors.alpha(white, la * 0.12F);
                    for (float[] o : new float[][]{{-1.0F, 0.0F}, {1.0F, 0.0F}, {0.0F, -1.0F}, {0.0F, 1.0F}}) {
                        R2D.textCentered(g, font, row, cx + o[0], ly + o[1], big, halo, false);
                    }
                }
                if (karaoke) {
                    karaoke(g, font, row, cx, ly, big, v.progress, before, total, Colors.alpha(white, la),
                            Colors.alpha(gray, la));
                } else {
                    R2D.textCentered(g, font, row, cx, ly, big, Colors.alpha(white, la), true);
                }
                before += row.length();
                ly += 9.0F * big + 1.0F;
                y += 9.0F * big + 1.0F;
            }
            y += 2.0F;
        } else if (!v.loading && !v.none && v.playing) {
            // Between sung lines: three notes breathing one after another.
            long now = System.currentTimeMillis();
            float step = 7.0F * big;
            for (int i = 0; i < 3; i++) {
                float pulse = (float) (Math.sin(now / 260.0 - i * 0.9) * 0.5 + 0.5);
                R2D.textCentered(g, font, "♪", cx + (i - 1) * step, y - pulse * 1.5F + sl * 8.0F, big,
                        Colors.alpha(gray, a * (1.0F - sl * 0.7F) * (0.35F + pulse * 0.55F)), true);
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

    private static void karaoke(GuiGraphicsExtractor g, Font font, String line, float cx, float y, float scale,
            float progress, int before, int total, int lit, int dim) {
        // Lit by letters, not by words: a long word takes longer to sing than a short one.
        String[] words = line.split(" ");
        float x = cx - R2D.width(font, line) * scale / 2.0F;
        float litChars = progress * total * 1.05F - before;
        int done = 0;
        for (int i = 0; i < words.length; i++) {
            String w = i < words.length - 1 ? words[i] + " " : words[i];
            done += w.length();
            R2D.text(g, font, w, x, y, scale, done - w.length() / 2.0F <= litChars ? lit : dim, true);
            x += R2D.width(font, w) * scale;
        }
    }

    /** {@code text} broken into at most two rows that fit {@code maxWidth}; the rest ends in "…". */
    private static java.util.List<String> wrap(Font font, String text, float maxWidth, float scale) {
        java.util.List<String> rows = new java.util.ArrayList<>();
        if (R2D.width(font, text) * scale <= maxWidth) {
            rows.add(text);
            return rows;
        }
        StringBuilder row = new StringBuilder();
        String[] words = text.split(" ");
        int i = 0;
        for (; i < words.length; i++) {
            String next = row.length() == 0 ? words[i] : row + " " + words[i];
            if (row.length() > 0 && R2D.width(font, next) * scale > maxWidth) {
                break;
            }
            row.setLength(0);
            row.append(next);
        }
        rows.add(R2D.trim(font, row.toString(), maxWidth, scale));
        if (i < words.length) {
            rows.add(R2D.trim(font, String.join(" ", java.util.Arrays.copyOfRange(words, i, words.length)), maxWidth,
                    scale));
        }
        return rows;
    }

    private record View(String title, String current, String upcoming, float progress, boolean playing, boolean loading, boolean none) {
    }
}
