package xyz.fokus;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.resources.Identifier;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.visual.CrystalModule;

public final class CrystalHooks {
    private static final float BOB_FLOOR = -1.4F;
    private static float savedAge;
    private static int depth;

    private CrystalHooks() {
    }

    private static CrystalModule module() {
        CrystalModule m = Modules.get(CrystalModule.class);
        return m != null && m.isEnabled() ? m : null;
    }

    public static void beforeSubmit(PoseStack pose) {
        CrystalModule m = module();
        if (m != null) {
            float s = m.scale.f();
            pose.pushPose();
            pose.scale(s, s, s);
            depth++;
        }
    }

    public static void afterSubmit(PoseStack pose) {
        if (depth > 0) {
            depth--;
            pose.popPose();
        }
    }

    public static void extracted(EndCrystalRenderState state) {
        CrystalModule m = module();
        if (m != null && m.hideBase.on()) {
            state.showsBottom = false;
        }
    }

    public static Identifier texture(Identifier vanilla) {
        CrystalModule m = module();
        if (m == null) {
            return vanilla;
        } else {
            Identifier own = m.textureId();
            return own != null ? own : vanilla;
        }
    }

    public static float bob(float y) {
        CrystalModule m = module();
        return m == null ? y : -1.4F + (y - -1.4F) * m.bounce.f();
    }

    public static void beforeAnim(EndCrystalRenderState state) {
        savedAge = state.ageInTicks;
        CrystalModule m = module();
        if (m != null) {
            state.ageInTicks = state.ageInTicks * m.speed.f();
        }
    }

    public static void afterAnim(EndCrystalRenderState state) {
        state.ageInTicks = savedAge;
    }
}
