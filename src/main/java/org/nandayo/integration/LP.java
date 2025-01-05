package org.nandayo.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.nandayo.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LP {

    private static LuckPerms api = null;
    private final Main plugin;

    public LP(Main plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
        }
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
        if (api == null) return new Player[0];
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> {
                    // Get the LuckPerms user for the player
                    User user = api.getUserManager().getUser(player.getUniqueId());
                    if (user == null) return false;

                    // Check if the player is in the group
                    return user.getNodes().stream()
                            .filter(InheritanceNode.class::isInstance)
                            .map(InheritanceNode.class::cast)
                            .anyMatch(node -> node.getGroupName().equalsIgnoreCase(groupName));
                })
                .toArray(Player[]::new);
    }

    //GET PLAYER GROUP
    public static String getGroup(Player player) {
        if (api == null) return "";

        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";

        return user.getNodes().stream()
                .filter(InheritanceNode.class::isInstance)
                .map(InheritanceNode.class::cast)
                .map(InheritanceNode::getGroupName)
                .findFirst()
                .orElse("");
    }
}
