package xyz.fokus.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import xyz.fokus.Lang;

/** Direction and distance from the player to a block, as the HUD shows them: "↗ 128 m". */
public final class Pointer {
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    private Pointer() {
    }

    /** Horizontal distance to the middle of the block. */
    public static double distance(LocalPlayer p, int x, int z) {
        double dx = x + 0.5 - p.getX();
        double dz = z + 0.5 - p.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** An arrow relative to where the player looks (↑ = straight ahead), and the distance. */
    public static String toward(LocalPlayer p, int x, int z) {
        double dx = x + 0.5 - p.getX();
        double dz = z + 0.5 - p.getZ();
        float target = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relative = Mth.wrapDegrees(target - p.getYRot());
        return ARROWS[Math.floorMod(Math.round(relative / 45.0F), 8)] + " " + Math.round(distance(p, x, z)) + " m";
    }

    /** The dimension's name for the player: Oberwelt, Nether, End, or its id. */
    public static String dimensionName(String path) {
        return switch (path) {
            case "overworld" -> Lang.t("Oberwelt");
            case "the_nether" -> "Nether";
            case "the_end" -> "End";
            default -> path;
        };
    }
}
