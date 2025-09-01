package org.nandayo.dmentions.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dmentions.DMentions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @since 1.8.3
 */
public class LuckPermsHook implements IHook {
    static private final Map<UUID, String> playerGroupCache =  new HashMap<>();

    private final @NotNull DMentions plugin;
    private Object mask = null;
    public LuckPermsHook(@NotNull DMentions plugin) {
        this.plugin = plugin;
        pluginCondition("LuckPerms", lp -> {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if(provider != null) {
                this.mask = provider.getProvider();
            }
        });
    }

    @Override
    public Object mask() {
        return mask;
    }

    /**
     * Get LuckPerms. Use with caution as it may not be present. Check with {@link #isMaskNull()} beforehand.
     * @return LuckPerms
     * @since 1.8.3
     */
    public LuckPerms getAPI() {
        return (LuckPerms) this.mask;
    }


    /**
     * Get LuckPerms group name list.
     * @return List of Group name
     * @since 1.8.3
     */
    public List<String> getGroups() {
        if(isMaskNull()) return new ArrayList<>();
        return getAPI().getGroupManager().getLoadedGroups().stream()
                .map(Group::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get online players with given group name.
     * @param groupName Group name
     * @return Players
     * @since 1.8.3
     */
    public Player[] getOnlinePlayersInGroup(String groupName) {
        if (isMaskNull() || groupName == null || groupName.isEmpty()) return new Player[0];
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> {
                    User user = getAPI().getUserManager().getUser(player.getUniqueId());
                    if (user == null) return false;

                    return getGroup(player).equals(groupName);
                })
                .toArray(Player[]::new);
    }

    /**
     * Get cached group of player. Cached group represents the utmost weighted group of player.
     * @param player Player
     * @return Group name if found, else {@code null}.
     * @since 1.8.3
     */
    @Nullable
    public String getGroup(Player player) {
        String group = playerGroupCache.get(player.getUniqueId());
        return group != null ? group : updatePlayerGroupCache(player);
    }

    /**
     * Update cached group of player. Cached group represents the utmost weighted group of player.
     * @param player Player
     * @return Group name if updated, else {@code null}.
     * @since 1.8.3
     */
    public String updatePlayerGroupCache(Player player) {
        String group = getHighestPriorityGroup(player);
        if(group != null) playerGroupCache.put(player.getUniqueId(), group);
        return group;
    }

    /**
     * Fetch utmost weighted group of a player.
     * @param player Player
     * @return Group name
     * @since 1.8.3
     */
    @Nullable
    private String getHighestPriorityGroup(Player player) {
        if(isMaskNull()) return null;
        LuckPerms api = getAPI();
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;
        QueryOptions queryOptions = api.getContextManager().getQueryOptions(user).orElse(null);
        if (queryOptions == null) return null;

        return user.getInheritedGroups(queryOptions).stream()
                .max(Comparator.comparingInt(g -> g.getWeight().orElse(0)))
                .map(Group::getName)
                .orElse(user.getPrimaryGroup());
    }
}
