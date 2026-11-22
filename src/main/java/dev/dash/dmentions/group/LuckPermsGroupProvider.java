package dev.dash.dmentions.group;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.List;

public final class LuckPermsGroupProvider implements GroupProvider {

    private final LuckPerms luckPerms;

    /** @throws IllegalStateException if LuckPerms API is not loaded yet */
    public LuckPermsGroupProvider() {
        this.luckPerms = LuckPermsProvider.get();
    }

    @Override
    public List<String> groups() {
        return luckPerms.getGroupManager().getLoadedGroups().stream().map(Group::getName).toList();
    }

    @Override
    public boolean isMember(Player player, String group) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getPrimaryGroup().equals(group);
    }
}
