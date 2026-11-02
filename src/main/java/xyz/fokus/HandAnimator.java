package xyz.fokus;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.anim.BlockHitModule;
import xyz.fokus.module.impl.anim.EquipModule;
import xyz.fokus.module.impl.anim.ItemViewModule;
import xyz.fokus.module.impl.anim.SwingModule;

public final class HandAnimator {
    private static final float PI = (float) Math.PI;
    private static HumanoidArm currentArm = HumanoidArm.RIGHT;
    private static ItemViewModule.View currentView;
    private static final Matrix4f ROT = new Matrix4f();

    private HandAnimator() {
    }

    public static float equip(float equip) {
        EquipModule m = Modules.get(EquipModule.class);
        return m.isEnabled() ? m.apply(equip) : equip;
    }

    public static void afterArmTransform(PoseStack pose, HumanoidArm arm) {
        currentArm = arm;
        currentView = null;
        ItemViewModule m = Modules.get(ItemViewModule.class);
        LocalPlayer p = Minecraft.getInstance().player;
        if (m.isEnabled() && p != null) {
            ItemStack stack = arm == p.getMainArm() ? p.getMainHandItem() : p.getOffhandItem();
            currentView = m.viewFor(stack);
            int side = arm == HumanoidArm.RIGHT ? 1 : -1;
            pose.translate(side * currentView.x.f(), currentView.y.f(), currentView.z.f());
        }
    }

    public static boolean beforeItem(PoseStack pose) {
        ItemViewModule.View v = currentView;
        if (v == null) {
            return true;
        } else if (v.hidden.on()) {
            return false;
        } else {
            int side = currentArm == HumanoidArm.RIGHT ? 1 : -1;
            float s = v.scale.f();
            if (s != 1.0F) {
                pose.scale(s, s, s);
            }

            if (v.rotX.f() != 0.0F) {
                rotX(pose, v.rotX.f());
            }

            if (v.rotY.f() != 0.0F) {
                rotY(pose, side * v.rotY.f());
            }

            if (v.rotZ.f() != 0.0F) {
                rotZ(pose, side * v.rotZ.f());
            }

            return true;
        }
    }

    public static boolean swing(PoseStack pose, float s, int side, HumanoidArm arm) {
        SwingModule sw = Modules.get(SwingModule.class);
        BlockHitModule bh = Modules.get(BlockHitModule.class);
        boolean blocking = bh.isBlocking(arm);
        String style = sw.isEnabled() ? sw.style.get() : "Vanilla";
        float k = sw.isEnabled() ? sw.intensity.f() : 1.0F;
        if (!blocking && style.equals("Vanilla") && k == 1.0F) {
            return false;
        } else if (blocking) {
            block(pose, s, side, bh.style.get());
            return true;
        } else {
            apply(pose, style, s, side, k);
            return true;
        }
    }

    private static void apply(PoseStack pose, String style, float s, int side, float k) {
        float e = (float)Math.sin(s * (float) Math.PI);
        float sq = (float)Math.sin(Math.sqrt(s) * (float) Math.PI);
        switch (style) {
            case "Glatt":
                pose.translate(side * -0.28F * e * k, 0.12F * e * k, -0.16F * e * k);
                rotY(pose, side * 45.0F);
                rotY(pose, side * -20.0F * e * k);
                rotZ(pose, side * -20.0F * e * k);
                rotX(pose, -70.0F * e * k);
                rotY(pose, side * -45.0F);
                break;
            case "Klassisch 1.7":
                pose.translate(side * -0.4F * sq * k, 0.2F * (float)Math.sin(Math.sqrt(s) * (float) Math.PI * 2.0) * k, 0.0F);
                float sn = (float)Math.sin(s * s * (float) Math.PI);
                rotY(pose, side * (45.0F + sn * -20.0F * k));
                rotZ(pose, side * sq * -20.0F * k);
                rotX(pose, sq * -80.0F * k);
                rotY(pose, side * -45.0F);
                break;
            case "Spin":
                pose.translate(side * -0.15F * e * k, 0.05F * e * k, -0.1F * e * k);
                rotX(pose, -360.0F * s);
                break;
            case "Stich":
                pose.translate(side * -0.08F * e, 0.06F * e * k, -0.45F * e * k);
                rotX(pose, -15.0F * e * k);
                break;
            case "Slide":
                pose.translate(side * -0.35F * e * k, 0.0F, -0.1F * e * k);
                rotY(pose, side * 45.0F);
                rotX(pose, -50.0F * sq * k);
                rotY(pose, side * -45.0F);
                break;
            case "Punch":
                pose.translate(0.0F, 0.08F * e * k, -0.38F * e * k);
                rotX(pose, -25.0F * e * k);
                rotZ(pose, side * -10.0F * e * k);
                break;
            case "Exhibition":
                pose.translate(0.0F, -0.1F * sq * k, 0.0F);
                rotY(pose, side * 45.0F);
                rotZ(pose, side * -35.0F * sq * k);
                rotX(pose, -45.0F * sq * k);
                rotY(pose, side * -45.0F);
                break;
            case "Swank":
                rotY(pose, side * (45.0F + sq * 10.0F * k));
                rotZ(pose, side * sq * -35.0F * k);
                rotX(pose, sq * -30.0F * k);
                rotY(pose, side * -45.0F);
                break;
            case "Wackeln":
                pose.translate(0.0F, -0.05F * e * k, 0.0F);
                rotZ(pose, side * (float)Math.sin(s * (float) Math.PI * 3.0F) * 30.0F * k * (1.0F - s));
                break;
            case "Tippen":
                pose.translate(0.0F, -0.12F * e * k, -0.08F * e * k);
                rotX(pose, -20.0F * e * k);
                break;
            default:
                vanilla(pose, s, side, k);
        }
    }

    private static void vanilla(PoseStack pose, float s, int side, float k) {
        float sq = (float)Math.sin(Math.sqrt(s) * (float) Math.PI);
        pose.translate(
            side * -0.4F * sq * k, 0.2F * (float)Math.sin(Math.sqrt(s) * (float) Math.PI * 2.0) * k, -0.2F * (float)Math.sin(s * (float) Math.PI) * k
        );
        float sn = (float)Math.sin(s * s * (float) Math.PI);
        rotY(pose, side * (45.0F + sn * -20.0F * k));
        rotZ(pose, side * sq * -20.0F * k);
        rotX(pose, sq * -80.0F * k);
        rotY(pose, side * -45.0F);
    }

    private static void block(PoseStack pose, float s, int side, String style) {
        float sq = (float)Math.sin(Math.sqrt(s) * (float) Math.PI);
        float e = (float)Math.sin(s * (float) Math.PI);
        switch (style) {
            case "Exhibition":
                pose.translate(side * -0.1F, 0.05F - 0.1F * sq, 0.0F);
                break;
            case "Sanft":
                pose.translate(side * -0.1F, 0.05F - 0.05F * e, -0.05F * e);
                break;
            default:
                pose.translate(side * (-0.1F - 0.25F * sq), 0.05F + 0.1F * (float)Math.sin(Math.sqrt(s) * (float) Math.PI * 2.0), -0.1F * e);
        }

        rotX(pose, -102.25F);
        rotY(pose, side * 13.365F);
        rotZ(pose, side * 78.05F);
        if (style.equals("Exhibition")) {
            rotX(pose, -30.0F * sq);
        } else if (style.equals("1.7")) {
            rotZ(pose, side * -20.0F * sq);
        }
    }

    private static void rotX(PoseStack pose, float deg) {
        pose.mulPose(ROT.rotationX((float)Math.toRadians(deg)));
    }

    private static void rotY(PoseStack pose, float deg) {
        pose.mulPose(ROT.rotationY((float)Math.toRadians(deg)));
    }

    private static void rotZ(PoseStack pose, float deg) {
        pose.mulPose(ROT.rotationZ((float)Math.toRadians(deg)));
    }
}
