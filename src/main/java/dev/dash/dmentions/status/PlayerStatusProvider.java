package dev.dash.dmentions.status;

import org.bukkit.entity.Player;

public interface PlayerStatusProvider {

    boolean isAFK(Player player);

    boolean isIgnoring(Player player, Player other);
}
