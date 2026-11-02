package xyz.fokus.module;

public enum Category {
    PVP("PvP"),
    HUD("HUD"),
    ANIMATIONS("Animationen"),
    VISUALS("Visuals"),
    SETTINGS("Einstellungen");

    public final String label;

    private Category(String label) {
        this.label = label;
    }
}
