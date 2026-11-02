package xyz.fokus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fokus.compat.Compat;
import xyz.fokus.config.ConfigIO;
import xyz.fokus.module.Modules;
import xyz.fokus.module.impl.settings.InterfaceModule;
import xyz.fokus.update.Updater;

public class FokusMod implements ClientModInitializer {
    public static final String NAME = "fokus.xyz";
    public static final Logger LOG = LoggerFactory.getLogger("fokus");
    public static final String VERSION = FabricLoader.getInstance()
        .getModContainer("fokus")
        .map(c -> c.getMetadata().getVersion().getFriendlyString())
        .orElse("0.0.0");
    public static final String LINK = "https://flexbio.link/1";

    public void onInitializeClient() {
        Modules.init();
        ConfigIO.load();
        if (Modules.get(InterfaceModule.class).autoUpdate.on()) {
            Updater.check();
        }

        LOG.info("{} {} geladen (Minecraft {}, Tasten über {})", new Object[]{"fokus.xyz", VERSION, Compat.minecraftVersion(), Compat.SDL ? "SDL" : "GLFW"});
    }
}
