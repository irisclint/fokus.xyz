package xyz.fokus.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class FokusMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, String> NEEDS = Map.of(
        "GuiHudMixin",
        "extractCrosshair",
        "SwingDurationLegacyMixin",
        "getCurrentSwingDuration",
        "SwingDurationMixin",
        "getModifiedSwingDuration",
        "FireOverlayLegacyMixin",
        "renderFire",
        "FireOverlayMixin",
        "submitFire",
        "ClearViewGuiMixin",
        "extractVignette",
        "CloudColorLegacyMixin",
        "render(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V",
        "CloudColorMixin",
        "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V"
    );

    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String resource = targetClassName.replace('.', '/') + ".class";
        ClassLoader loader = FokusMixinPlugin.class.getClassLoader();
        if (loader.getResource(resource) == null) {
            return false;
        } else {
            String simple = mixinClassName.substring(mixinClassName.lastIndexOf(46) + 1);
            String method = NEEDS.get(simple);
            return method == null || hasMethod(loader, resource, method);
        }
    }

    private static boolean hasMethod(ClassLoader loader, String resource, final String method) {
        try {
            boolean var5;
            try (InputStream in = loader.getResourceAsStream(resource)) {
                if (in == null) {
                    return false;
                }

                final boolean[] found = new boolean[]{false};
                new ClassReader(in).accept(new ClassVisitor(589824) {
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        if (method.contains("(") ? method.equals(name + descriptor) : name.equals(method)) {
                            found[0] = true;
                        }

                        return null;
                    }
                }, 7);
                var5 = found[0];
            }

            return var5;
        } catch (IOException var8) {
            return false;
        }
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
