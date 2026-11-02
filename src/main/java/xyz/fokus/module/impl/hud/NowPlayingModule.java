package xyz.fokus.module.impl.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import xyz.fokus.SkyAnchor;
import xyz.fokus.lyrics.CoverArt;
import xyz.fokus.lyrics.MediaWatcher;
import xyz.fokus.module.HudModule;
import xyz.fokus.module.Theme;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Anim;
import xyz.fokus.util.Colors;
import xyz.fokus.util.R2D;

public class NowPlayingModule extends HudModule {
    public final ModeSetting place = this.add(new ModeSetting("Ort", "Bildschirm", "Bildschirm", "Himmel - folgt dem Blick", "Himmel - fest"));
    public final NumberSetting elevation = this.add(new NumberSetting("Höhe am Himmel", 34.0, 3.0, 80.0, 1.0, "°"))
        .visibleWhen(() -> !this.place.is("Bildschirm"));
    public final NumberSetting follow = this.add(new NumberSetting("Nachziehen", 1.5, 0.2, 8.0, 0.1, "x"))
        .visibleWhen(() -> this.place.is("Himmel - folgt dem Blick"));
    public final ModeSetting typeface = this.add(new ModeSetting("Schrift", "Glatt", "Glatt", "Minecraft"));
    public final BoolSetting cover = this.add(new BoolSetting("Cover zeigen", true));
    public final BoolSetting coverTint = this.add(new BoolSetting("Farbe vom Cover", false));
    public final BoolSetting progress = this.add(new BoolSetting("Fortschrittsbalken", true));
    public final BoolSetting source = this.add(new BoolSetting("Quelle zeigen", true));
    private final Anim show = new Anim(0.0F, 6.0F);
    private final Anim bar = new Anim(0.0F, 6.0F);
    private final SkyAnchor anchor = new SkyAnchor();
    private String lastSong = "";

    public NowPlayingModule() {
        super("Now Playing", "Was gerade läuft: jede App und jede Seite im Browser, mit Cover.", 0.005F, 0.84F);
        this.enabledByDefault();
    }

    @Override
    public void onTick(Minecraft mc) {
        MediaWatcher.start();
    }

    @Override
    public boolean placesItself() {
        return !this.place.is("Bildschirm");
    }

    @Override
    public void drawPlaced(GuiGraphicsExtractor g, Font font) {
        MediaWatcher.Track t = MediaWatcher.current();
        if (t != null && !t.title().equals(this.lastSong)) {
            this.lastSong = t.title();
            this.anchor.reset();
        }

        float[] p = this.anchor.place(this.place.get(), this.elevation.f(), this.follow.f(), g.guiWidth(), g.guiHeight());
        if (p != null) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            float s = this.scale.f();
            pose.translate(p[0], p[1]);
            pose.scale(s, s);
            pose.translate(-this.w / 2.0F, -this.h / 2.0F);
            this.paint(g, font, t, p[2]);
            pose.popMatrix();
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Font font, boolean editor) {
        MediaWatcher.Track t = MediaWatcher.current();
        if (t == null && editor) {
            t = new MediaWatcher.Track("Song", "Artist", 83.0, 225.0, System.currentTimeMillis(), true, "Spotify.exe", "", 0);
        }

        this.paint(g, font, t, 1.0F);
    }

    private void paint(GuiGraphicsExtractor g, Font font, MediaWatcher.Track t, float visibility) {
        R2D.face(this.typeface.is("Glatt") ? R2D.SMOOTH : null);

        try {
            this.paintPanel(g, font, t, visibility);
        } finally {
            R2D.face(null);
        }
    }

    private void paintPanel(GuiGraphicsExtractor g, Font font, MediaWatcher.Track t, float visibility) {
        this.show.to(t != null ? 1.0F : 0.0F);
        float a = this.show.get() * visibility;
        boolean art = this.cover.on() && t != null && CoverArt.ready();
        float side = this.progress.on() ? 30.0F : 24.0F;
        int art_ = Math.round(side - 10.0F);
        float x = art ? art_ + 10 : 6.0F;
        this.w = art ? 140 + art_ + 4 : 140.0F;
        this.h = side;
        if (!(a < 0.02F) && t != null) {
            this.box(g, this.w, this.h);
            if (art) {
                CoverArt.draw(g, 5, 5, art_, a);
            }

            int main = this.coverTint.on() && CoverArt.ready() ? Colors.lerp(CoverArt.tint(), -1, 0.45F) : this.text();
            String head = this.source.on() ? t.source() : "";
            if (t.live()) {
                head = head.isEmpty() ? "LIVE" : head + "  ·  LIVE";
            } else if (!t.playing()) {
                head = head.isEmpty() ? "II" : head + "  ·  II";
            }

            float room = this.w - x - 6.0F;
            if (!head.isEmpty()) {
                R2D.text(g, font, R2D.trim(font, head, room * 0.6F, 0.55F), x, 4.0F, 0.55F, Colors.alpha(this.label(), a), Theme.shadow());
            }

            R2D.text(g, font, R2D.trim(font, t.title(), room, 0.75F), x, 9.5F, 0.75F, Colors.alpha(main, a), Theme.shadow());
            String artist = t.cleanArtist();
            if (!artist.isEmpty()) {
                R2D.text(g, font, R2D.trim(font, artist, room, 0.6F), x, 16.5F, 0.6F, Colors.alpha(this.label(), a), Theme.shadow());
            }

            if (this.progress.on()) {
                float by = this.h - 5.5F;
                R2D.round(g, x, by, room, 2.0F, 1.0F, Colors.alpha(822083583, a));
                if (t.live()) {
                    R2D.round(g, x, by, room, 2.0F, 1.0F, Colors.alpha(main, a * 0.55F));
                } else {
                    float f = (float)Math.max(0.0, Math.min(1.0, t.positionNow() / t.duration()));
                    this.bar.to(f);
                    R2D.round(g, x, by, Math.max(2.0F, room * this.bar.get()), 2.0F, 1.0F, Colors.alpha(main, a));
                    String time = clock(t.positionNow()) + " / " + clock(t.duration());
                    R2D.text(g, font, time, this.w - 6.0F - R2D.width(font, time) * 0.5F, 4.0F, 0.5F, Colors.alpha(this.label(), a), false);
                }
            }
        }
    }

    private static String clock(double seconds) {
        int s = (int)Math.max(0.0, seconds);
        return s / 60 + ":" + (s % 60 < 10 ? "0" : "") + s % 60;
    }
}
