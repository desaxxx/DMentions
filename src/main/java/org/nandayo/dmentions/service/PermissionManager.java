package org.nandayo.dmentions.service;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LP;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    public PermissionManager(@NotNull DMentions plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }
    //PERMISSION SETUP
    public void setupPermissions() {
        String playerPermission = plugin.getPermission(config.getConfig().getString("player.permission", "dmentions.mention.player"));
        String everyonePermission = plugin.getPermission(config.getConfig().getString("everyone.permission", "dmentions.mention.everyone"));
        String nearbyPermission = plugin.getPermission(config.getConfig().getString("nearby.permission", "dmentions.mention.nearby"));

        Bukkit.getPluginManager().addPermission(new Permission(playerPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(nearbyPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(everyonePermission, PermissionDefault.OP));
        plugin.afterLoadPermissions.add(playerPermission);
        plugin.afterLoadPermissions.add(nearbyPermission);
        plugin.afterLoadPermissions.add(everyonePermission);

        if (LP.isConnected()) {
            Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
            if(adminPermission == null) return;

            Map<String, Boolean> children = new HashMap<>();
            children.put(playerPermission, true);
            children.put(everyonePermission, true);

            for (String group : LP.getGroups()) {
                String groupPermission = plugin.getPermission(config.getConfig().getString("group.permission", "")).replace("{group}", group);
                // REGISTERING GROUP PERMISSIONS
                Bukkit.getPluginManager().addPermission(new Permission(groupPermission, PermissionDefault.OP));
                plugin.afterLoadPermissions.add(groupPermission);
                // ADDING GROUP PERMISSIONS AS CHILDREN TO 'dmentions.admin'
                children.put(groupPermission, true);
            }
            adminPermission.getChildren().putAll(children);
            adminPermission.recalculatePermissibles();
        }
    }
    public void clearAfterLoadPermissions() {
        if(plugin.afterLoadPermissions.isEmpty()) return;
        Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
        for(String perm : plugin.afterLoadPermissions) {
            Permission permission = Bukkit.getPluginManager().getPermission(perm);
            if(permission != null) {
                Bukkit.getPluginManager().removePermission(permission);
            }
            if(adminPermission != null && adminPermission.getChildren().containsKey(perm)) {
                adminPermission.getChildren().remove(perm);
                adminPermission.recalculatePermissibles();
            }
        }
        plugin.afterLoadPermissions.clear();
    }
}
