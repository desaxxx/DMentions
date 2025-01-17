package org.nandayo.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.nandayo.Main;
import org.nandayo.utils.Util;

import java.util.*;
import java.util.stream.Collectors;

public class LP {

    private static LuckPerms api = null;
    private final Main plugin;

    private static final Map<UUID, String> playerGroupCache = new HashMap<>();

    public LP(Main plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
        }
    }
    public static LuckPerms getApi() {
        return api;
    }

    public static boolean isConnected() {
        return api != null;
    }

    //GET GROUPS
    public static List<String> getGroups() {
        if(api == null) return new ArrayList<>();
        return api.getGroupManager().getLoadedGroups().stream()
                .map(Group::getName)
                .collect(Collectors.toList());
    }

    //GET ONLINE GROUP MEMBERS
    public static Player[] getOnlinePlayersInGroup(String groupName) {
        if (api == null || groupName == null ||groupName.isEmpty()) return new Player[0];
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> {
                    User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user == null) return false;

                    return getGroup(player).equals(groupName);
                })
                .toArray(Player[]::new);
    }

    //GET PLAYER PRIMARY GROUP
    public static String getGroup(Player player) {
        String group = playerGroupCache.get(player.getUniqueId());
        if(group == null) {
            updatePlayerGroupCache(player);
            group = playerGroupCache.get(player.getUniqueId());
        }
        return group;
    }

    /*
     * CACHING THE PLAYER GROUP WITH UTMOST WEIGHT
     */
    public static void updatePlayerGroupCache(Player player) {
        String group = getHighestPriorityGroup(player);
        playerGroupCache.put(player.getUniqueId(), group);
    }

    private static String getHighestPriorityGroup(Player player) {
        if (api == null) return "";

        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";

        return user.getNodes(NodeType.INHERITANCE).stream()
                .map(InheritanceNode::getGroupName)
                .map(groupName -> api.getGroupManager().getGroup(groupName))
                .filter(group -> group != null && group.getWeight().isPresent())
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .map(Group::getName)
                .orElse(user.getPrimaryGroup());
    }
}
