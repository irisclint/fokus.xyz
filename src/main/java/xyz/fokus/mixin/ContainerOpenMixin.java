package xyz.fokus.mixin;

import java.lang.ref.WeakReference;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.anim.InventoryAnimModule;

@Mixin({Screen.class})
public class ContainerOpenMixin {
    private static WeakReference<Screen> fokus$last = new WeakReference<>(null);
    private static long fokus$openedAt;
    private static boolean fokus$active;
    private static int fokus$backgroundDepth;

    @Inject(
        method = {"extractRenderStateWithTooltipAndSubtitles"},
        at = {@At("HEAD")}
    )
    private void fokus$begin(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        Screen self = (Screen)(Object)this;
        fokus$active = false;
        if (self instanceof AbstractContainerScreen) {
            InventoryAnimModule m = Modules.get(InventoryAnimModule.class);
            if (m != null && m.isEnabled()) {
                long now = System.currentTimeMillis();
                if (fokus$last.get() != self) {
                    fokus$last = new WeakReference<>(self);
                    fokus$openedAt = now;
                }

                float[] t = m.transform(now - fokus$openedAt, self.height);
                if (t != null) {
                    Matrix3x2fStack pose = g.pose();
                    pose.pushMatrix();
                    pose.translate(self.width / 2.0F, self.height / 2.0F + t[1]);
                    pose.scale(t[0], t[0]);
                    pose.translate(-self.width / 2.0F, -self.height / 2.0F);
                    fokus$active = true;
                }
            }
        }
    }

    @Inject(
        method = {"extractRenderStateWithTooltipAndSubtitles"},
        at = {@At("RETURN")}
    )
    private void fokus$end(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        if (fokus$active) {
            g.pose().popMatrix();
            fokus$active = false;
        }
    }

    @Inject(
        method = {"extractBackground"},
        at = {@At("HEAD")}
    )
    private void fokus$backgroundBegin(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        if (fokus$active && fokus$backgroundDepth++ == 0) {
            g.pose().pushMatrix();
            g.pose().identity();
        }
    }

    @Inject(
        method = {"extractBackground"},
        at = {@At("RETURN")}
    )
    private void fokus$backgroundEnd(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        if (fokus$active && --fokus$backgroundDepth == 0) {
            g.pose().popMatrix();
        }
    }
}
