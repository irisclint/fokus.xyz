package xyz.fokus.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class Compat {
    public static final boolean SDL;
    private static final MethodHandle IS_KEY_DOWN;
    private static final MethodHandle SCREEN_GETTER;
    public static final int MOUSE_LEFT = constant("MOUSE_BUTTON_LEFT", 0);
    public static final int MOUSE_RIGHT = constant("MOUSE_BUTTON_RIGHT", 1);
    public static final int MOUSE_MIDDLE = constant("MOUSE_BUTTON_MIDDLE", 2);
    public static final int PRESS = constant("PRESS", 1);
    public static final int KEY_ESCAPE = constant("KEY_ESCAPE", 256);
    public static final int KEY_BACKSPACE = constant("KEY_BACKSPACE", 259);
    public static final int KEY_DELETE = constant("KEY_DELETE", 261);
    public static final int KEY_RETURN = constant("KEY_RETURN", 257);
    public static final int KEY_RSHIFT = constant("KEY_RSHIFT", 344);
    public static final int KEY_LCONTROL = constant("KEY_LCONTROL", 341);
    public static final int KEY_C = constant("KEY_C", 67);
    public static final int KEY_F = constant("KEY_F", 70);
    public static final int KEY_A = constant("KEY_A", 65);

    private Compat() {
    }

    private static int constant(String name, int fallback) {
        try {
            Field f = InputConstants.class.getField(name);
            return f.getInt(null);
        } catch (ReflectiveOperationException var3) {
            return fallback;
        }
    }

    public static int key(String name) {
        return constant(name, -1);
    }

    public static boolean irisClientHud() {
        return isLoaded("irismenu");
    }

    public static boolean isKeyDown(int key) {
        if (key >= 0 && IS_KEY_DOWN != null) {
            try {
                return SDL ? (boolean)IS_KEY_DOWN.invoke((int)key) : (boolean)IS_KEY_DOWN.invoke((Window)Minecraft.getInstance().getWindow(), (int)key);
            } catch (Throwable var2) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String keyName(int key) {
        if (key < 0) {
            return "—";
        } else {
            try {
                String s = Type.values()[0].getOrCreate(key).getDisplayName().getString();
                return s.length() > 10 ? s.substring(0, 10) : s;
            } catch (Throwable var2) {
                return "#" + key;
            }
        }
    }

    public static Screen currentScreen() {
        if (SCREEN_GETTER == null) {
            return null;
        } else {
            try {
                return (Screen)SCREEN_GETTER.invoke((Minecraft)Minecraft.getInstance());
            } catch (Throwable var1) {
                return null;
            }
        }
    }

    /** The chat: Gui.getChat() on 26.1, Gui.hud.getChat() from 26.2 on. Null before the game is up. */
    public static ChatComponent chat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) {
            return null;
        }
        Object holder = mc.gui;
        try {
            try {
                return (ChatComponent) holder.getClass().getMethod("getChat").invoke(holder);
            } catch (NoSuchMethodException e) {
                holder = holder.getClass().getField("hud").get(holder);
                return (ChatComponent) holder.getClass().getMethod("getChat").invoke(holder);
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /** A line in the chat that only this player sees. */
    public static void say(Component message) {
        ChatComponent chat = chat();
        if (chat != null) {
            chat.addClientSystemMessage(message);
        }
    }

    public static String backend() {
        return SDL ? "sdl" : "glfw";
    }

    public static boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static String minecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("26.x");
    }

    static {
        Lookup lookup = MethodHandles.publicLookup();
        MethodHandle sdl = null;
        MethodHandle glfw = null;

        try {
            sdl = lookup.findStatic(InputConstants.class, "isKeyDown", MethodType.methodType(boolean.class, int.class));
        } catch (ReflectiveOperationException var10) {
        }

        if (sdl == null) {
            try {
                Class<?> window = Class.forName("com.mojang.blaze3d.platform.Window");
                glfw = lookup.findStatic(InputConstants.class, "isKeyDown", MethodType.methodType(boolean.class, window, int.class));
            } catch (ReflectiveOperationException var9) {
            }
        }

        SDL = sdl != null;
        IS_KEY_DOWN = sdl != null ? sdl : glfw;
        MethodHandle screen = null;

        try {
            Class<?> gui = Class.forName("net.minecraft.client.gui.Gui");
            MethodHandle getter = lookup.findVirtual(gui, "screen", MethodType.methodType(Screen.class));
            MethodHandle guiField = lookup.findGetter(Minecraft.class, "gui", gui);
            screen = MethodHandles.filterReturnValue(guiField, getter);
        } catch (ReflectiveOperationException var8) {
        }

        if (screen == null) {
            try {
                screen = lookup.findGetter(Minecraft.class, "screen", Screen.class);
            } catch (ReflectiveOperationException var7) {
            }
        }

        SCREEN_GETTER = screen;
    }
}
