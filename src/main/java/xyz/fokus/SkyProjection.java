package xyz.fokus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xyz.fokus.module.impl.pvp.FreelookModule;

public final class SkyProjection {
    public static volatile float fov = 70.0F;

    private SkyProjection() {
    }

    public static float[] project(float yaw, float elevation, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) {
            return null;
        } else {
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            boolean free = FreelookModule.active();
            double camYaw = Math.toRadians(free ? FreelookModule.yaw() : p.getViewYRot(partial));
            double camPitch = Math.toRadians(free ? FreelookModule.pitch() : p.getViewXRot(partial));
            double ty = Math.toRadians(yaw);
            double te = Math.toRadians(elevation);
            double dx = -Math.sin(ty) * Math.cos(te);
            double dy = Math.sin(te);
            double dz = Math.cos(ty) * Math.cos(te);
            double fx = -Math.sin(camYaw) * Math.cos(camPitch);
            double fy = -Math.sin(camPitch);
            double fz = Math.cos(camYaw) * Math.cos(camPitch);
            double rx = -Math.cos(camYaw);
            double rz = -Math.sin(camYaw);
            double ux = 0.0 * fz - rz * fy;
            double uy = rz * fx - rx * fz;
            double uz = rx * fy - 0.0 * fx;
            double z = dx * fx + dy * fy + dz * fz;
            if (z < 0.05) {
                return null;
            } else {
                double x = dx * rx + dz * rz;
                double y = dx * ux + dy * uy + dz * uz;
                double tanHalf = Math.tan(Math.toRadians(fov) / 2.0);
                float sx = (float)(w / 2.0 + x / z / tanHalf * (h / 2.0));
                float sy = (float)(h / 2.0 - y / z / tanHalf * (h / 2.0));
                return new float[]{sx, sy, (float)z};
            }
        }
    }

    public static float viewYaw() {
        Minecraft mc = Minecraft.getInstance();
        if (FreelookModule.active()) {
            return FreelookModule.yaw();
        } else {
            return mc.player == null ? 0.0F : mc.player.getViewYRot(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        }
    }
}
