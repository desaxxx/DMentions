package org.nandayo.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.nandayo.ConfigManager;
import org.nandayo.Main;
import org.nandayo.integration.LP;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {

    private final Main plugin;
    private final ConfigManager configManager;
    public PermissionManager(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }
    //PERMISSION SETUP
    public void setupPermissions() {
        String playerPermission = plugin.getPermission(configManager.getString("player.permission", "dmentions.mention.player"));
        String everyonePermission = plugin.getPermission(configManager.getString("everyone.permission", "dmentions.mention.everyone"));
        String nearbyPermission = plugin.getPermission(configManager.getString("nearby.permission", "dmentions.mention.nearby"));

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
                String groupPermission = plugin.getPermission(configManager.getString("group.permission", null)).replace("{group}", group);
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
        for(String perm : plugin.afterLoadPermissions) {
            Permission permission = Bukkit.getPluginManager().getPermission(perm);
            if(permission != null) {
                Bukkit.getPluginManager().removePermission(permission);
            }
            Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
            if(adminPermission != null) {
                adminPermission.getChildren().remove(perm);
                adminPermission.recalculatePermissibles();
            }
        }
        plugin.afterLoadPermissions.clear();
    }
}