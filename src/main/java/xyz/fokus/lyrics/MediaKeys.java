package xyz.fokus.lyrics;

import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import xyz.fokus.FokusMod;

/**
 * Presses a Windows media key (play/pause, next, previous). Windows hands it to whatever is playing -
 * Spotify, a browser tab, any app the media watcher sees - so no app needs to be named.
 */
public final class MediaKeys {
    public static final int PLAY_PAUSE = 0xB3;
    public static final int NEXT = 0xB0;
    public static final int PREVIOUS = 0xB1;

    private MediaKeys() {
    }

    /** Presses and releases the key in a short PowerShell, off the game thread; returns the exit code or -1. */
    public static int press(int vk) {
        if (!MediaWatcher.supported()) {
            return -1;
        }
        String script = "Add-Type -Namespace Fokus -Name Keys -MemberDefinition "
                + "'[DllImport(\"user32.dll\")] public static extern void keybd_event(byte v, byte s, uint f, System.UIntPtr e);';"
                + "[Fokus.Keys]::keybd_event(" + vk + ", 0, 1, [System.UIntPtr]::Zero);"
                + "[Fokus.Keys]::keybd_event(" + vk + ", 0, 3, [System.UIntPtr]::Zero)";
        // Encoded, so no quote in the script has to survive the Windows command line.
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        try {
            Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden",
                    "-EncodedCommand", encoded)
                    .redirectOutput(Redirect.DISCARD)
                    .redirectError(Redirect.DISCARD)
                    .start();
            return p.waitFor();
        } catch (Exception e) {
            FokusMod.LOG.warn("fokus.xyz: could not press media key {}", vk, e);
            return -1;
        }
    }

    /** The same, without waiting: for key presses in game. */
    public static void pressLater(int vk) {
        Thread t = new Thread(() -> press(vk), "fokus-media-key");
        t.setDaemon(true);
        t.start();
    }
}
