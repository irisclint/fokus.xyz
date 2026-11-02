package xyz.fokus;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.pvp.HitSoundsModule;
import xyz.fokus.module.impl.pvp.HitmarkerModule;

public final class Stats {
    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    public static int combo;
    public static long lastHitAt;
    public static double lastReach = -1.0;
    public static long lastReachAt;
    public static LivingEntity target;
    public static long targetAt;
    /** When you last hit a player, and when you last took damage: the start of a combat tag. */
    public static long lastHitPlayerAt;
    public static long lastHitAnyAt;
    public static long lastHurtAt;
    public static double speed;
    private static int lastHurtTime;
    private static double lastX;
    private static double lastZ;
    private static boolean havePos;

    private Stats() {
    }

    public static void click(boolean left) {
        long now = System.currentTimeMillis();
        Deque<Long> q = left ? LEFT : RIGHT;
        synchronized (q) {
            q.addLast(now);
        }
    }

    public static int cps(boolean left) {
        Deque<Long> q = left ? LEFT : RIGHT;
        long cutoff = System.currentTimeMillis() - 1000L;
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < cutoff) {
                q.pollFirst();
            }

            return q.size();
        }
    }

    public static void onAttack(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p != null) {
            long now = System.currentTimeMillis();
            if (now - lastHitAt > 3000L) {
                combo = 0;
            }

            combo++;
            lastHitAt = now;
            lastHitAnyAt = now;
            if (entity instanceof net.minecraft.world.entity.player.Player) {
                lastHitPlayerAt = now;
            }
            HitmarkerModule.onHit();
            Vec3 eye = p.getEyePosition();
            AABB box = entity.getBoundingBox();
            double cx = Math.max(box.minX, Math.min(eye.x, box.maxX));
            double cy = Math.max(box.minY, Math.min(eye.y, box.maxY));
            double cz = Math.max(box.minZ, Math.min(eye.z, box.maxZ));
            lastReach = Math.sqrt(eye.distanceToSqr(cx, cy, cz));
            lastReachAt = now;
            if (entity instanceof LivingEntity living) {
                target = living;
                targetAt = now;
            }

            Modules.get(HitSoundsModule.class).play();
        }
    }

    public static void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) {
            havePos = false;
            combo = 0;
            target = null;
        } else {
            if (p.hurtTime > lastHurtTime) {
                combo = 0;
                lastHurtAt = System.currentTimeMillis();
            }

            lastHurtTime = p.hurtTime;
            if (System.currentTimeMillis() - lastHitAt > 3000L) {
                combo = 0;
            }

            if (target != null && (target.isRemoved() || System.currentTimeMillis() - targetAt > 8000L)) {
                target = null;
            }

            Vec3 pos = p.position();
            if (havePos) {
                double dx = pos.x - lastX;
                double dz = pos.z - lastZ;
                double now = Math.sqrt(dx * dx + dz * dz) * 20.0;
                speed = speed * 0.6 + now * 0.4;
            }

            lastX = pos.x;
            lastZ = pos.z;
            havePos = true;
        }
    }
}
