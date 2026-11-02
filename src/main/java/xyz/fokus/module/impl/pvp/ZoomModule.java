package xyz.fokus.module.impl.pvp;

import net.minecraft.client.Minecraft;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.NumberSetting;
import xyz.fokus.util.Anim;

public class ZoomModule extends Module {
    public final KeySetting zoomKey = this.add(new KeySetting("Zoom-Taste (halten)", Compat.KEY_C));
    public final NumberSetting factor = this.add(new NumberSetting("Zoomstärke", 4.0, 1.5, 12.0, 0.5, "x"));
    public final BoolSetting smooth = this.add(new BoolSetting("Weicher Zoom", true));
    public final BoolSetting scroll = this.add(new BoolSetting("Mausrad verstellt Zoom", true));
    public final BoolSetting cinematic = this.add(new BoolSetting("Kinokamera beim Zoomen", false));
    private final Anim level = new Anim(1.0F, 12.0F);
    private boolean zooming;
    private double scrollFactor = -1.0;
    private boolean setSmoothCamera;

    public ZoomModule() {
        super("Zoom", "Halte die Zoom-Taste, um wie mit einem Fernglas heranzuzoomen.", Category.PVP);
        if (!Compat.isLoaded("ok_zoomer")) {
            this.enabledByDefault();
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        boolean want = this.isEnabled() && mc.player != null && Compat.currentScreen() == null && this.zoomKey.held();
        if (want && !this.zooming) {
            this.scrollFactor = this.factor.get();
        }

        this.zooming = want;
        if (this.cinematic.on()) {
            if (this.zooming && !mc.options.smoothCamera) {
                mc.options.smoothCamera = true;
                this.setSmoothCamera = true;
            } else if (!this.zooming && this.setSmoothCamera) {
                mc.options.smoothCamera = false;
                this.setSmoothCamera = false;
            }
        }
    }

    @Override
    protected void onDisable() {
        this.zooming = false;
        if (this.setSmoothCamera) {
            Minecraft.getInstance().options.smoothCamera = false;
            this.setSmoothCamera = false;
        }
    }

    public boolean isZooming() {
        return this.zooming;
    }

    public boolean onScroll(double amount) {
        if (this.zooming && this.scroll.on()) {
            this.scrollFactor = Math.max(1.5, Math.min(30.0, this.scrollFactor * (amount > 0.0 ? 1.15 : 0.8695652173913044)));
            return true;
        } else {
            return false;
        }
    }

    public float apply(float fov) {
        float target = this.zooming ? (float)(this.scrollFactor > 0.0 ? this.scrollFactor : this.factor.get()) : 1.0F;
        this.level.to(target);
        float z = this.smooth.on() ? this.level.get() : target;
        if (!this.smooth.on()) {
            this.level.snap(target);
        }

        return z <= 1.0005F ? fov : fov / z;
    }
}
