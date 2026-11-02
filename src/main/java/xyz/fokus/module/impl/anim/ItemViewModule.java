package xyz.fokus.module.impl.anim;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.fokus.module.Category;
import xyz.fokus.module.Module;
import xyz.fokus.setting.BoolSetting;
import xyz.fokus.setting.ModeSetting;
import xyz.fokus.setting.NumberSetting;

public class ItemViewModule extends Module {
    public static final String[] GROUPS = new String[]{
        "Alle Items",
        "Schwerter",
        "Äxte",
        "Streitkolben & Speer",
        "Endkristalle",
        "Totems",
        "Enderperlen",
        "Essen & Tränke",
        "Blöcke",
        "Bögen & Armbrüste",
        "Werkzeuge",
        "Schilde",
        "Sonstige"
    };
    private static final int OTHER = GROUPS.length - 1;
    public final ModeSetting group = this.add(new ModeSetting("Bearbeiten", "Alle Items", GROUPS));
    private final ItemViewModule.View[] views = new ItemViewModule.View[GROUPS.length];

    public ItemViewModule() {
        super("Item-Ansicht", "Position, Größe, Drehung - für alle Items oder jede Art einzeln.", Category.ANIMATIONS);

        for (int i = 0; i < GROUPS.length; i++) {
            this.views[i] = new ItemViewModule.View(i);
        }
    }

    public ItemViewModule.View viewFor(ItemStack stack) {
        int g = groupOf(stack);
        ItemViewModule.View v = this.views[g];
        return g != 0 && v.own.on() ? v : this.views[0];
    }

    public static int groupOf(ItemStack s) {
        if (s == null || s.isEmpty()) {
            return OTHER;
        } else if (s.is(ItemTags.SWORDS)) {
            return 1;
        } else if (s.is(ItemTags.AXES)) {
            return 2;
        } else {
            String id = s.getItem().getDescriptionId().toLowerCase(Locale.ROOT);
            if (s.is(Items.MACE) || id.endsWith("spear") || s.is(Items.TRIDENT)) {
                return 3;
            } else if (s.is(Items.END_CRYSTAL)) {
                return 4;
            } else if (s.is(Items.TOTEM_OF_UNDYING)) {
                return 5;
            } else if (s.is(Items.ENDER_PEARL)) {
                return 6;
            } else if (s.has(DataComponents.FOOD)
                || s.is(Items.POTION)
                || s.is(Items.SPLASH_POTION)
                || s.is(Items.LINGERING_POTION)
                || s.is(Items.EXPERIENCE_BOTTLE)) {
                return 7;
            } else if (s.getItem() instanceof BlockItem) {
                return 8;
            } else if (s.is(Items.BOW) || s.is(Items.CROSSBOW)) {
                return 9;
            } else if (s.is(ItemTags.PICKAXES)
                || s.is(ItemTags.SHOVELS)
                || s.is(ItemTags.HOES)
                || s.is(Items.SHEARS)
                || s.is(Items.FLINT_AND_STEEL)
                || s.is(Items.FISHING_ROD)) {
                return 10;
            } else {
                return s.is(Items.SHIELD) ? 11 : OTHER;
            }
        }
    }

    public final class View {
        public final BoolSetting own;
        public final BoolSetting hidden;
        public final NumberSetting x;
        public final NumberSetting y;
        public final NumberSetting z;
        public final NumberSetting scale;
        public final NumberSetting rotX;
        public final NumberSetting rotY;
        public final NumberSetting rotZ;

        View(int index) {
            Objects.requireNonNull(ItemViewModule.this);
            super();
            String g = ItemViewModule.GROUPS[index];
            BooleanSupplier shown = () -> ItemViewModule.this.group.is(g);
            boolean all = index == 0;
            this.own = all
                ? null
                : ItemViewModule.this.add(((BoolSetting)new BoolSetting(g + ": eigene Werte", false).labeled("Eigene Werte")).visibleWhen(shown));
            BooleanSupplier values = all ? shown : () -> ItemViewModule.this.group.is(g) && this.own.on();
            this.hidden = ItemViewModule.this.add(((BoolSetting)new BoolSetting(g + ": ausblenden", false).labeled("Unsichtbar")).visibleWhen(values));
            this.x = this.num(g, "Position X", 0.0, -1.0, 1.0, 0.01, "", values);
            this.y = this.num(g, "Position Y", 0.0, -1.0, 1.0, 0.01, "", values);
            this.z = this.num(g, "Position Z", 0.0, -1.0, 1.0, 0.01, "", values);
            this.scale = this.num(g, "Größe", 1.0, 0.2, 1.6, 0.05, "x", values);
            this.rotX = this.num(g, "Drehung X", 0.0, -180.0, 180.0, 5.0, "°", values);
            this.rotY = this.num(g, "Drehung Y", 0.0, -180.0, 180.0, 5.0, "°", values);
            this.rotZ = this.num(g, "Drehung Z", 0.0, -180.0, 180.0, 5.0, "°", values);
        }

        private NumberSetting num(String g, String label, double def, double min, double max, double step, String suffix, BooleanSupplier visible) {
            return ItemViewModule.this.add(
                ((NumberSetting)new NumberSetting(g + ": " + label, def, min, max, step, suffix).labeled(label)).visibleWhen(visible)
            );
        }
    }
}
