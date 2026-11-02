package xyz.fokus;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.anim.SwingModule;
import xyz.fokus.module.impl.visual.LowFireModule;

public final class VersionHooks {
    private static boolean firePushed;

    private VersionHooks() {
    }

    public static void swingDuration(LivingEntity self, CallbackInfoReturnable<Integer> cir) {
        if (self == Minecraft.getInstance().player) {
            SwingModule m = Modules.get(SwingModule.class);
            if (m != null && m.isEnabled()) {
                cir.setReturnValue(m.duration((Integer)cir.getReturnValue()));
            }
        }
    }

    public static void fireStart(PoseStack pose) {
        LowFireModule m = Modules.get(LowFireModule.class);
        firePushed = m != null && m.isEnabled();
        if (firePushed) {
            pose.pushPose();
            pose.translate(0.0F, -m.height.f(), 0.0F);
        }
    }

    public static void fireEnd(PoseStack pose) {
        if (firePushed) {
            pose.popPose();
            firePushed = false;
        }
    }
}
