package xyz.fokus.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import xyz.fokus.module.impl.anim.BlockHitModule;
import xyz.fokus.module.impl.anim.EquipModule;
import xyz.fokus.module.impl.anim.InventoryAnimModule;
import xyz.fokus.module.impl.anim.ItemViewModule;
import xyz.fokus.module.impl.anim.SwingModule;
import xyz.fokus.module.impl.hud.ArmorModule;
import xyz.fokus.module.impl.hud.BowChargeModule;
import xyz.fokus.module.impl.hud.ChatStackModule;
import xyz.fokus.module.impl.hud.ChatTimestampModule;
import xyz.fokus.module.impl.hud.CombatTimerModule;
import xyz.fokus.module.impl.hud.DamageNumbersModule;
import xyz.fokus.module.impl.hud.DeathPointModule;
import xyz.fokus.module.impl.hud.ItemCounterModule;
import xyz.fokus.module.impl.hud.KeystrokesModule;
import xyz.fokus.module.impl.hud.LyricsModule;
import xyz.fokus.module.impl.hud.ModuleListModule;
import xyz.fokus.module.impl.hud.MoreHudModules;
import xyz.fokus.module.impl.hud.NowPlayingModule;
import xyz.fokus.module.impl.hud.PotionsModule;
import xyz.fokus.module.impl.hud.SessionStatsModule;
import xyz.fokus.module.impl.hud.SimpleHudModules;
import xyz.fokus.module.impl.hud.TabPingModule;
import xyz.fokus.module.impl.hud.TargetHudModule;
import xyz.fokus.module.impl.hud.TotemPopsModule;
import xyz.fokus.module.impl.hud.WatermarkModule;
import xyz.fokus.module.impl.pvp.CrosshairModule;
import xyz.fokus.module.impl.pvp.FreelookModule;
import xyz.fokus.module.impl.pvp.HitSoundsModule;
import xyz.fokus.module.impl.pvp.HitmarkerModule;
import xyz.fokus.module.impl.pvp.NametagHealthModule;
import xyz.fokus.module.impl.pvp.StaticFovModule;
import xyz.fokus.module.impl.pvp.NoHurtCamModule;
import xyz.fokus.module.impl.pvp.ToggleSneakModule;
import xyz.fokus.module.impl.pvp.ToggleSprintModule;
import xyz.fokus.module.impl.pvp.ZoomModule;
import xyz.fokus.module.impl.settings.InterfaceModule;
import xyz.fokus.module.impl.settings.ProfilesModule;
import xyz.fokus.module.impl.visual.BlockOutlineModule;
import xyz.fokus.module.impl.visual.ClearViewModule;
import xyz.fokus.module.impl.visual.ClearWeatherModule;
import xyz.fokus.module.impl.visual.CrystalModule;
import xyz.fokus.module.impl.visual.ItemAuraModule;
import xyz.fokus.module.impl.visual.FullbrightModule;
import xyz.fokus.module.impl.visual.HitColorModule;
import xyz.fokus.module.impl.visual.LowFireModule;
import xyz.fokus.module.impl.visual.NoBobModule;
import xyz.fokus.module.impl.visual.SkyModule;
import xyz.fokus.module.impl.visual.TexturesModule;
import xyz.fokus.module.impl.visual.TotemModule;

public final class Modules {
    private static final List<Module> ALL = new ArrayList<>();
    private static final List<HudModule> HUD = new ArrayList<>();
    private static final Map<Class<?>, Module> BY_CLASS = new HashMap<>();

    private Modules() {
    }

    public static void init() {
        register(new ToggleSprintModule());
        register(new ToggleSneakModule());
        register(new ZoomModule());
        register(new FreelookModule());
        register(new StaticFovModule());
        register(new CrosshairModule());
        register(new HitSoundsModule());
        register(new HitmarkerModule());
        register(new NametagHealthModule());
        register(new NoHurtCamModule());
        register(new WatermarkModule());
        register(new ModuleListModule());
        register(new SimpleHudModules.Fps());
        register(new SimpleHudModules.Cps());
        register(new SimpleHudModules.Ping());
        register(new SimpleHudModules.Coordinates());
        register(new SimpleHudModules.Clock());
        register(new SimpleHudModules.Speed());
        register(new SimpleHudModules.ServerInfo());
        register(new KeystrokesModule());
        register(new ArmorModule());
        register(new PotionsModule());
        register(new TargetHudModule());
        register(new TotemPopsModule());
        register(new CombatTimerModule());
        register(new SessionStatsModule());
        register(new DamageNumbersModule());
        register(new BowChargeModule());
        register(new SimpleHudModules.Combo());
        register(new SimpleHudModules.Reach());
        register(new ItemCounterModule());
        register(new LyricsModule());
        register(new NowPlayingModule());
        register(new ChatTimestampModule());
        register(new ChatStackModule());
        register(new TabPingModule());
        register(new MoreHudModules.Compass());
        register(new DeathPointModule());
        register(new MoreHudModules.NearbyPlayers());
        register(new MoreHudModules.Hunger());
        register(new MoreHudModules.ArmorWarning());
        register(new MoreHudModules.PlayTime());
        register(new MoreHudModules.DayTime());
        register(new MoreHudModules.Biome());
        register(new MoreHudModules.Memory());
        register(new MoreHudModules.Cooldowns());
        register(new MoreHudModules.Tps());
        register(new MoreHudModules.LightLevel());
        register(new MoreHudModules.HeldDurability());
        register(new SwingModule());
        register(new BlockHitModule());
        register(new ItemViewModule());
        register(new EquipModule());
        register(new InventoryAnimModule());
        register(new FullbrightModule());
        register(new NoBobModule());
        register(new LowFireModule());
        register(new CrystalModule());
        register(new TotemModule());
        register(new ItemAuraModule());
        register(new HitColorModule());
        register(new TexturesModule());
        register(new SkyModule());
        register(new ClearWeatherModule());
        register(new ClearViewModule());
        register(new BlockOutlineModule());
        register(new InterfaceModule());
        register(new ProfilesModule());
    }

    private static void register(Module m) {
        ALL.add(m);
        BY_CLASS.put(m.getClass(), m);
        if (m instanceof HudModule hud) {
            HUD.add(hud);
        }
    }

    public static <T extends Module> T get(Class<T> type) {
        return (T)BY_CLASS.get(type);
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static List<HudModule> hud() {
        return Collections.unmodifiableList(HUD);
    }

    public static List<Module> in(Category c) {
        List<Module> out = new ArrayList<>();

        for (Module m : ALL) {
            if (m.category == c) {
                out.add(m);
            }
        }

        return out;
    }

    public static void resetAll() {
        for (Module m : ALL) {
            m.resetToDefaults();
        }
    }
}
