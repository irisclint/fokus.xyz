package xyz.fokus.util;

/** The game's entity overlay texture, given a hurt colour of its own (see OverlayTextureMixin). */
public interface TintableOverlay {
    /** Refills the hurt half of the texture with this colour (ARGB, alpha = how strongly it tints). */
    void fokus$tint(int argb);
}
