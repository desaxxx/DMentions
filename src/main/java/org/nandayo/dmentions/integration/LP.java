package org.nandayo.dmentions.integration;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import org.bukkit.entity.Player;
import org.nandayo.dmentions.DMentions;

import java.util.*;

@Deprecated(since = "1.8.3", forRemoval = true)
public class LP {

    @Getter
    @Deprecated(since = "1.8.3", forRemoval = true)
    private static final LuckPerms api = null;

    @Deprecated(since = "1.8.3", forRemoval = true)
    private static final Map<UUID, String> playerGroupCache = new HashMap<>();

    @Deprecated(since = "1.8.3", forRemoval = true)
    public LP() {}

    @Deprecated(since = "1.8.3", forRemoval = true)
    public static boolean isConnected() {
        return !DMentions.inst().getLuckPermsHook().isMaskNull();
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    public static List<String> getGroups() {
        return DMentions.inst().getLuckPermsHook().getGroups();
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    public static Player[] getOnlinePlayersInGroup(String groupName) {
        return DMentions.inst().getLuckPermsHook().getOnlinePlayersInGroup(groupName);
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    public static String getGroup(Player player) {
        return DMentions.inst().getLuckPermsHook().getGroup(player);
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    public static void updatePlayerGroupCache(Player player) {
        DMentions.inst().getLuckPermsHook().updatePlayerGroupCache(player);
    }

    @Deprecated(since = "1.8.3", forRemoval = true)
    private static String getHighestPriorityGroup(Player player) {
        return null;
    }
}
