package dev.dash.dmentions.mention;

import dev.dash.dmentions.user.UserService;
import org.bukkit.entity.Player;

public final class TargetFilter {

    public static final String RESTRICTED = "dmentions.restricted";
    public static final String RESTRICTED_BYPASS = "dmentions.restricted.bypass";

    private final UserService userService;

    public TargetFilter(UserService userService) {
        this.userService = userService;
    }

    public boolean allows(Player sender, Player target, MentionType type) {
        if (!type.respectsToggle()) return true;
        if (!userService.isMentionable(target.getUniqueId())) return false;
        if (target.hasPermission(RESTRICTED) && !sender.hasPermission(RESTRICTED_BYPASS)) return false;
        return true;
    }
}
