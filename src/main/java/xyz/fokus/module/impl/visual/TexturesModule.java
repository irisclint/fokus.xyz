package xyz.fokus.module.impl.visual;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.module.Modules;
import xyz.fokus.setting.ActionSetting;
import xyz.fokus.setting.ColorSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.util.Recolor;
import xyz.fokus.util.TexturePack;

public class TexturesModule extends Module {
    private static final String TIERS = "(wooden|stone|copper|iron|golden|diamond|netherite)";
    private static final String WORN = "entity/equipment/humanoid(_leggings)?/";
    private static final String PIECES = "_(helmet|chestplate|leggings|boots)";
    private static final TexturesModule.Group[] GROUPS = new TexturesModule.Group[]{
        group("Schwerter", "item/(wooden|stone|copper|iron|golden|diamond|netherite)_sword"),
        group("Äxte", "item/(wooden|stone|copper|iron|golden|diamond|netherite)_axe"),
        group("Werkzeuge", "item/(wooden|stone|copper|iron|golden|diamond|netherite)_(pickaxe|shovel|hoe)"),
        group("Speere", "item/[a-z]+_spear(_in_hand)?"),
        group("Streitkolben & Dreizack", "item/(mace|trident)"),
        group("Bögen & Armbrüste", "item/(bow|bow_pulling_[0-2]|crossbow_[a-z0-9_]+)"),
        group("Diamant-Rüstung", "(item/diamond_(helmet|chestplate|leggings|boots)|entity/equipment/humanoid(_leggings)?/diamond)"),
        group("Netherite-Rüstung", "(item/netherite_(helmet|chestplate|leggings|boots)|entity/equipment/humanoid(_leggings)?/netherite)"),
        group("Eisen-Rüstung", "(item/iron_(helmet|chestplate|leggings|boots)|entity/equipment/humanoid(_leggings)?/iron)"),
        group("Gold-Rüstung", "(item/golden_(helmet|chestplate|leggings|boots)|entity/equipment/humanoid(_leggings)?/gold)"),
        group(
            "Andere Rüstung",
            "(item/(chainmail|copper)_(helmet|chestplate|leggings|boots)|item/turtle_helmet|entity/equipment/humanoid(_leggings)?/(chainmail|copper|turtle_scute))"
        ),
        group("Elytra", "(item/elytra(_broken)?|entity/equipment/wings/elytra)"),
        group("Schilde", "entity/shield/shield_base(_nopattern)?"),
        group("Goldäpfel", "item/golden_apple"),
        group("Enderperlen", "item/(ender_pearl|ender_eye)"),
        group("Flaschen", "item/(potion|splash_potion|lingering_potion|experience_bottle)"),
        group("Obsidian & Anker", "block/(obsidian|crying_obsidian|respawn_anchor_[a-z0-9_]+|glowstone)")
    };
    private static final String[][] PRESETS = new String[][]{
        {"Clean Mono", "Mono", "", ""},
        {"Schwarz & Weiß", "Kontur", "1A1A1A", "FFFFFF"},
        {"Eis", "Eis", "", ""},
        {"Gold", "Gold", "", ""},
        {"Feuer", "Feuer", "", ""},
        {"Neon", "Neon", "", ""},
        {"Pastell", "Pastell", "", ""},
        {"Kräftig", "Kräftig", "", ""},
        {"Dunkel", "Dunkel", "", ""},
        {"Regenbogen", "Regenbogen", "", ""},
        {"Vanilla", "Vanilla", "", ""}
    };
    public final ModeSetting preset = this.add(new ModeSetting("Vorlage", "Clean Mono", presetNames()));
    public final ActionSetting usePreset = this.add(new ActionSetting("Vorlage auf alles anwenden", this::applyPreset));
    public final ModeSetting edit = this.add(new ModeSetting("Bearbeiten", GROUPS[0].name(), groupNames()));
    private final ModeSetting[] styles = new ModeSetting[GROUPS.length];
    private final ColorSetting[] darks = new ColorSetting[GROUPS.length];
    private final ColorSetting[] lights = new ColorSetting[GROUPS.length];
    public final ActionSetting apply;

    private static TexturesModule.Group group(String name, String regex) {
        return new TexturesModule.Group(name, Pattern.compile(regex + "\\.png"));
    }

    private static String[] groupNames() {
        String[] out = new String[GROUPS.length];

        for (int i = 0; i < GROUPS.length; i++) {
            out[i] = GROUPS[i].name();
        }

        return out;
    }

    private static String[] presetNames() {
        String[] out = new String[PRESETS.length];

        for (int i = 0; i < PRESETS.length; i++) {
            out[i] = PRESETS[i][0];
        }

        return out;
    }

    public TexturesModule() {
        super("Better Items", "Eigene Looks für Waffen, Rüstung, Werkzeuge, Elytra, Schilde und PvP-Items - viele Varianten.", Category.VISUALS);

        for (int i = 0; i < GROUPS.length; i++) {
            String g = GROUPS[i].name();
            BooleanSupplier shown = () -> this.edit.is(g);
            ModeSetting style = this.add(((ModeSetting)new ModeSetting(g + ": Stil", "Vanilla", Recolor.STYLES).labeled("Stil")).visibleWhen(shown));
            BooleanSupplier colors = () -> this.edit.is(g) && Recolor.usesColors(style.get());
            this.styles[i] = style;
            this.darks[i] = this.add(((ColorSetting)new ColorSetting(g + ": dunkel", -14803426).labeled("Farbe dunkel")).visibleWhen(colors));
            this.lights[i] = this.add(((ColorSetting)new ColorSetting(g + ": hell", -1).labeled("Farbe hell")).visibleWhen(colors));
        }

        this.apply = this.add(new ActionSetting("Texturen übernehmen", TexturesModule::applyAll));
    }

    private void applyPreset() {
        for (String[] p : PRESETS) {
            if (p[0].equals(this.preset.get())) {
                for (int i = 0; i < GROUPS.length; i++) {
                    this.styles[i].set(p[1]);
                    if (!p[2].isEmpty()) {
                        this.darks[i].set(0xFF000000 | Integer.parseInt(p[2], 16));
                        this.lights[i].set(0xFF000000 | Integer.parseInt(p[3], 16));
                    }
                }
            }
        }

        applyAll();
    }

    public static void applyAll() {
        Map<String, TexturePack.Look> looks = new LinkedHashMap<>();
        TexturesModule items = Modules.get(TexturesModule.class);
        if (items.isEnabled()) {
            for (int i = 0; i < GROUPS.length; i++) {
                String style = items.styles[i].get();
                if (!style.equals("Vanilla")) {
                    TexturePack.Look look = new TexturePack.Look(style, items.darks[i].color(0.0F) & 16777215, items.lights[i].color(0.0F) & 16777215);

                    for (String file : TexturePack.find(GROUPS[i].files())) {
                        looks.put(file, look);
                    }
                }
            }
        }

        TotemModule totems = Modules.get(TotemModule.class);
        if (totems.isEnabled()) {
            looks.put(
                "item/totem_of_undying.png",
                new TexturePack.Look(totems.texture.get(), totems.dark.color(0.0F) & 16777215, totems.light.color(0.0F) & 16777215)
            );
        }

        CrystalModule crystals = Modules.get(CrystalModule.class);
        if (totems.crystalItem.on() && crystals.isEnabled() && !crystals.texture.is("Vanilla")) {
            int core = crystals.core.color(0.0F) & 16777215;
            looks.put("item/end_crystal.png", new TexturePack.Look("Umfärben", darkenRgb(core), core));
        }

        TexturePack.apply(looks);
    }

    public static String packSignature() {
        StringBuilder sb = new StringBuilder();
        TexturesModule items = Modules.get(TexturesModule.class);
        sb.append(items.isEnabled());

        for (int i = 0; i < GROUPS.length; i++) {
            sb.append('|').append(items.styles[i].get()).append(items.darks[i].get()).append(items.lights[i].get());
        }

        TotemModule totems = Modules.get(TotemModule.class);
        sb.append('|')
            .append(totems.isEnabled())
            .append(totems.texture.get())
            .append(totems.dark.get())
            .append(totems.light.get())
            .append(totems.crystalItem.get());
        CrystalModule crystals = Modules.get(CrystalModule.class);
        sb.append('|').append(crystals.isEnabled()).append(crystals.texture.get()).append(crystals.core.get());
        return sb.toString();
    }

    private static int darkenRgb(int rgb) {
        int r = (rgb >> 16 & 0xFF) / 4;
        int g = (rgb >> 8 & 0xFF) / 4;
        int b = (rgb & 0xFF) / 4;
        return r << 16 | g << 8 | b;
    }

    private record Group(String name, Pattern files) {
    }
}
