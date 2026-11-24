package dev.dash.dmentions.mention;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.status.PlayerStatusProvider;
import dev.dash.dmentions.user.UserService;
import dev.yoshi.xconfig.XConfig;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public final class TargetFilter {

    public static final String RESTRICTED = "dmentions.restricted";
    public static final String RESTRICTED_BYPASS = "dmentions.restricted.bypass";

    private final XConfig config;
    private final UserService userService;
    private final PlayerStatusProvider statusProvider;

    public TargetFilter(XConfig config, UserService userService, PlayerStatusProvider statusProvider) {
        this.config = config;
        this.userService = userService;
        this.statusProvider = statusProvider;
    }

    /**
     * Whether the sender's mention of this type may reach the target.
     * Vanish and ignore hold for every type; the rest are skipped by types that ignore the toggle (@everyone).
     */
    public boolean allows(Player sender, Player target, MentionType type) {
        if (config.get(ConfigKeys.VANISH_RESPECT) && isVanished(target)) return false;
        if (config.get(ConfigKeys.IGNORE_RESPECT) && statusProvider.isIgnoring(target, sender)) return false;
        if (!type.respectsToggle()) return true;

        if (!userService.isMentionable(target.getUniqueId())) return false;
        if (config.get(ConfigKeys.AFK_RESPECT) && statusProvider.isAFK(target)) return false;
        if (target.hasPermission(RESTRICTED) && !sender.hasPermission(RESTRICTED_BYPASS)) return false;
        return true;
    }

    /** Vanish plugins mark players with "vanished" metadata; any plugin saying true is enough. */
    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
