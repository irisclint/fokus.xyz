package xyz.fokus.module.impl.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;

public class HitSoundsModule extends Module {
    public final ModeSetting sound = this.add(new ModeSetting("Sound", "Ding", "Ding", "Glocke", "Hi-Hat", "Pling", "Pfeil", "Klick"));
    public final NumberSetting volume = this.add(new NumberSetting("Lautstärke", 0.7, 0.1, 1.0, 0.05));
    public final NumberSetting pitch = this.add(new NumberSetting("Tonhöhe", 1.0, 0.5, 2.0, 0.05));

    public HitSoundsModule() {
        super("Hit Sounds", "Spielt bei jedem Treffer einen Sound - nur für dich hörbar.", Category.PVP);
    }

    public void play() {
        if (this.isEnabled()) {
            String var2 = this.sound.get();

            SoundEvent event = switch (var2) {
                case "Glocke" -> (SoundEvent)SoundEvents.NOTE_BLOCK_BELL.value();
                case "Hi-Hat" -> (SoundEvent)SoundEvents.NOTE_BLOCK_HAT.value();
                case "Pling" -> (SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value();
                case "Pfeil" -> SoundEvents.ARROW_HIT_PLAYER;
                case "Klick" -> (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value();
                default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            };
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, this.pitch.f(), this.volume.f()));
        }
    }
}
