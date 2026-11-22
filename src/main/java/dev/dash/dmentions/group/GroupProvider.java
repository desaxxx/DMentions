package dev.dash.dmentions.group;

import org.bukkit.entity.Player;

import java.util.List;

public interface GroupProvider {

    List<String> groups();

    boolean isMember(Player player, String group);
}
