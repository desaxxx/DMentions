package dev.dash.dmentions.permission;

import dev.dash.dmentions.mention.MentionType;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class PermissionRegistrar {

    private final PluginManager pluginManager;
    private final Set<String> registeredPermissions = new HashSet<>();

    public PermissionRegistrar(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public void register(Collection<MentionType> types) {
        for (MentionType t : types) {
            String perm = t.permission();
            if (perm.isEmpty()) continue;
            Permission permission = pluginManager.getPermission(perm);
            if (permission != null) continue;

            pluginManager.addPermission(new Permission(perm, "Mention permission for " + t.id(), t.permissionDefault()));
            registeredPermissions.add(perm);
        }
    }

    public void unregisterAll() {
        for (String perm : registeredPermissions) {
            pluginManager.removePermission(perm);
        }
        registeredPermissions.clear();
    }
}
