package xyz.fokus.lyrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import xyz.fokus.FokusMod;

/**
 * Presses a Windows media key (play/pause, next, previous). Windows hands it to whatever is playing -
 * Spotify, a browser tab, any app the media watcher sees - so no app needs to be named.
 *
 * <p>One PowerShell is started on the first press and kept: it reads key codes from its input, so
 * only the first press waits for PowerShell to start; every later one is pressed at once.
 */
public final class MediaKeys {
    public static final int PLAY_PAUSE = 0xB3;
    public static final int NEXT = 0xB0;
    public static final int PREVIOUS = 0xB1;

    private static final String SCRIPT = "Add-Type -Namespace Fokus -Name Keys -MemberDefinition "
            + "'[DllImport(\"user32.dll\")] public static extern void keybd_event(byte v, byte s, uint f, System.UIntPtr e);';"
            + "while (($line = [Console]::In.ReadLine()) -ne $null) {"
            + " $vk = [int]$line;"
            + " [Fokus.Keys]::keybd_event($vk, 0, 1, [System.UIntPtr]::Zero);"
            + " [Fokus.Keys]::keybd_event($vk, 0, 3, [System.UIntPtr]::Zero) }";

    private static Process helper;
    private static BufferedWriter input;

    private MediaKeys() {
    }

    private static synchronized BufferedWriter helper() throws IOException {
        if (helper == null || !helper.isAlive()) {
            // Encoded, so no quote in the script has to survive the Windows command line.
            String encoded = Base64.getEncoder().encodeToString(SCRIPT.getBytes(StandardCharsets.UTF_16LE));
            helper = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden",
                    "-EncodedCommand", encoded)
                    .redirectOutput(Redirect.DISCARD)
                    .redirectError(Redirect.DISCARD)
                    .start();
            input = new BufferedWriter(new OutputStreamWriter(helper.getOutputStream(), StandardCharsets.US_ASCII));
            Process started = helper;
            Runtime.getRuntime().addShutdownHook(new Thread(started::destroy, "fokus-media-keys-stop"));
        }
        return input;
    }

    /** Presses and releases the key; false when it could not be sent. */
    public static synchronized boolean press(int vk) {
        if (!MediaWatcher.supported()) {
            return false;
        }
        try {
            BufferedWriter w = helper();
            w.write(Integer.toString(vk));
            w.newLine();
            w.flush();
            return true;
        } catch (IOException e) {
            // The helper went away: start a fresh one next time.
            helper = null;
            FokusMod.LOG.warn("fokus.xyz: could not press media key {}", vk, e);
            return false;
        }
    }

    /** The same, off the game thread: the first press starts PowerShell. */
    public static void pressLater(int vk) {
        Thread t = new Thread(() -> press(vk), "fokus-media-key");
        t.setDaemon(true);
        t.start();
    }
}
