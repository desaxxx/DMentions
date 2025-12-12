package org.nandayo.dmentions.module;

import org.jetbrains.annotations.ApiStatus;
import org.nandayo.dmentions.user.MentionUser;

/**
 * @since 1.9
 */
public interface BaseModule {

    /**
     * Called after plugin(DMentions) enable and before initializing any {@link MentionUser}.
     * <p></p>
     * This means there is no {@link MentionUser} registered at the moment.
     *
     * @since 1.9
     */
    @ApiStatus.OverrideOnly
    void onEnable();

    /**
     * Called before plugin shutdown and after all possible {@link #onUserLogout(MentionUser)} calls.
     * <p></p>
     * This means there is no MentionUser registered at the moment.
     *
     * @since 1.9
     */
    @ApiStatus.OverrideOnly
    void onDisable();

    /**
     * Called after a User gets registered, specifically after {@link org.bukkit.event.player.PlayerJoinEvent} happens.
     *
     * @param user the user logged in
     * @since 1.9
     */
    @ApiStatus.OverrideOnly
    default void onUserLogin(MentionUser user) {}

    /**
     * Called after a User gets unregistered, specifically after {@link org.bukkit.event.player.PlayerQuitEvent} happens.
     *
     * @param user the user logged out
     * @since 1.9
     */
    @ApiStatus.OverrideOnly
    default void onUserLogout(MentionUser user) {}
}
