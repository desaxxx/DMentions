package org.nandayo.dmentions.service;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LuckPermsHook;

import java.util.*;

public class PermissionManager {
    static private final Set<String> LOADED_PERMISSIONS = new HashSet<>();

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final LuckPermsHook luckPermsHook;
    public PermissionManager(@NotNull DMentions plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.luckPermsHook = plugin.getLuckPermsHook();
    }

    //PERMISSION SETUP
    public void setupPermissions() {
        String playerPermission = config.getConfig().getString("player.permission", "dmentions.mention.player");
        String everyonePermission = config.getConfig().getString("everyone.permission", "dmentions.mention.everyone");
        String nearbyPermission = config.getConfig().getString("nearby.permission", "dmentions.mention.nearby");

        Bukkit.getPluginManager().addPermission(new Permission(playerPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(nearbyPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(everyonePermission, PermissionDefault.OP));
        LOADED_PERMISSIONS.add(playerPermission);
        LOADED_PERMISSIONS.add(nearbyPermission);
        LOADED_PERMISSIONS.add(everyonePermission);

        if(!luckPermsHook.isMaskNull()) {
            Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
            if(adminPermission == null) return;

            Map<String, Boolean> children = new HashMap<>();
            children.put(playerPermission, true);
            children.put(everyonePermission, true);

            for (String group : luckPermsHook.getGroups()) {
                String groupPermission = config.getConfig().getString("group.permission", "dmentions.mention.group.{group}").replace("{group}", group);
                // REGISTERING GROUP PERMISSIONS
                Bukkit.getPluginManager().addPermission(new Permission(groupPermission, PermissionDefault.OP));
                LOADED_PERMISSIONS.add(groupPermission);
                // ADDING GROUP PERMISSIONS AS CHILDREN TO 'dmentions.admin'
                children.put(groupPermission, true);
            }
            adminPermission.getChildren().putAll(children);
            adminPermission.recalculatePermissibles();
        }
    }
    public void clearAfterLoadPermissions() {
        if(LOADED_PERMISSIONS.isEmpty()) return;
        Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
        for(String perm : LOADED_PERMISSIONS) {
            Permission permission = Bukkit.getPluginManager().getPermission(perm);
            if(permission != null) {
                Bukkit.getPluginManager().removePermission(permission);
            }
            if(adminPermission != null && adminPermission.getChildren().containsKey(perm)) {
                adminPermission.getChildren().remove(perm);
                adminPermission.recalculatePermissibles();
            }
        }
        LOADED_PERMISSIONS.clear();
    }

    /**
     * Get a copy of {@link #LOADED_PERMISSIONS}.
     * @return Set of Permission name
     * @since 1.8.3
     */
    public Set<String> getLoadedPermissions() {
        return new HashSet<>(LOADED_PERMISSIONS);
    }
}
