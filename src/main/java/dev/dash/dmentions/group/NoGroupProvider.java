package dev.dash.dmentions.group;

import org.bukkit.entity.Player;

import java.util.List;

public final class NoGroupProvider implements GroupProvider {

    public NoGroupProvider() {}

    @Override
    public List<String> groups() {
        return List.of();
    }

    @Override
    public boolean isMember(Player player, String group) {
        return false;
    }
}
