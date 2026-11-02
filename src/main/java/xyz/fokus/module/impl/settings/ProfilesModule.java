package xyz.fokus.module.impl.settings;

import net.minecraft.client.Minecraft;
import xyz.fokus.config.ConfigIO;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ActionSetting;
import xyz.fokus.setting.ModeSetting;

public class ProfilesModule extends Module {
    public final ModeSetting slot = this.add(new ModeSetting("Profil", "Profil 1", "Profil 1", "Profil 2", "Profil 3"));
    public final ActionSetting saveProfile = this.add(new ActionSetting("In Profil speichern", this::saveProfile));
    public final ActionSetting loadProfile = this.add(new ActionSetting("Profil laden", this::loadProfile));
    public final ActionSetting copy = this.add(new ActionSetting("Einstellungen kopieren", this::copy));
    public final ActionSetting paste = this.add(new ActionSetting("Geteilte Einstellungen einfügen", this::paste));
    private ActionSetting flashed;
    private long flashUntil;

    public ProfilesModule() {
        super("Profile & Teilen", "Bis zu drei Setups speichern - und dein Setup als Text an Freunde schicken.", Category.SETTINGS);
        this.alwaysOn = true;
    }

    private int index() {
        return this.slot.index() + 1;
    }

    private void flash(ActionSetting button, String result) {
        if (this.flashed != null && this.flashed != button) {
            this.flashed.labeled(null);
        }

        button.labeled(result);
        this.flashed = button;
        this.flashUntil = System.currentTimeMillis() + 2500L;
    }

    private void saveProfile() {
        ConfigIO.saveProfile(this.index());
        this.flash(this.saveProfile, "Gespeichert ✓");
    }

    private void loadProfile() {
        if (!ConfigIO.hasProfile(this.index())) {
            this.flash(this.loadProfile, "Dieses Profil ist noch leer");
        } else {
            int chosen = this.slot.index();
            boolean ok = ConfigIO.loadProfile(this.index());
            this.slot.set(this.slot.modes.get(chosen));
            this.flash(this.loadProfile, ok ? "Geladen ✓" : "Konnte nicht geladen werden");
        }
    }

    private void copy() {
        Minecraft.getInstance().keyboardHandler.setClipboard(ConfigIO.shareCode());
        this.flash(this.copy, "Kopiert ✓ - jetzt einfügen und verschicken");
    }

    private void paste() {
        boolean ok = ConfigIO.applyShareCode(Minecraft.getInstance().keyboardHandler.getClipboard());
        this.flash(this.paste, ok ? "Übernommen ✓" : "In der Zwischenablage ist kein fokus.xyz-Setup");
    }

    @Override
    public void onTick(Minecraft mc) {
        if (this.flashed != null && System.currentTimeMillis() > this.flashUntil) {
            this.flashed.labeled(null);
            this.flashed = null;
        }
    }
}
