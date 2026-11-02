package xyz.fokus.module.impl.pvp;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import xyz.fokus.compat.Compat;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.KeySetting;
import xyz.fokus.setting.ModeSetting;

public class FreelookModule extends Module {
    public final KeySetting key = this.add(new KeySetting("Freelook-Taste", Compat.key("KEY_LALT")));
    public final BoolSetting toggle = this.add(new BoolSetting("Umschalten statt halten", false));
    public final ModeSetting view = this.add(new ModeSetting("Ansicht", "Dritte Person", "Dritte Person", "Erste Person"));
    public final BoolSetting invert = this.add(new BoolSetting("Hoch/runter umkehren", false));
    private static boolean active;
    private static float yaw;
    private static float pitch;
    private CameraType before;
    private boolean wasDown;
    private boolean toggled;

    public FreelookModule() {
        super("Freelook", "Halte die Taste und sieh dich um, ohne dich zu drehen.", Category.PVP);
        this.enabledByDefault();
    }

    public static boolean active() {
        return active;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static void turn(double dx, double dy) {
        FreelookModule m = Modules.get(FreelookModule.class);
        yaw += (float)dx * 0.15F;
        pitch = Mth.clamp(pitch + (float)dy * 0.15F * (m != null && m.invert.on() ? -1 : 1), -90.0F, 90.0F);
    }

    @Override
    public void onTick(Minecraft mc) {
        boolean down = mc.player != null && Compat.currentScreen() == null && this.key.held();
        if (this.toggle.on() && down && !this.wasDown) {
            this.toggled = !this.toggled;
        }

        this.wasDown = down;
        boolean want = mc.player != null && (this.toggle.on() ? this.toggled : down);
        if (want && !active) {
            yaw = mc.player.getYRot();
            pitch = mc.player.getXRot();
            this.before = mc.options.getCameraType();
            if (this.view.is("Dritte Person") && this.before == CameraType.FIRST_PERSON) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }

            active = true;
        } else if (!want && active) {
            this.stop(mc);
        }
    }

    private void stop(Minecraft mc) {
        active = false;
        this.toggled = false;
        if (this.before != null && mc.options.getCameraType() != this.before) {
            mc.options.setCameraType(this.before);
        }

        this.before = null;
    }

    @Override
    protected void onDisable() {
        if (active) {
            this.stop(Minecraft.getInstance());
        }
    }
}
