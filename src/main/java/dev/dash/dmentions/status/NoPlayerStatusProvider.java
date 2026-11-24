package dev.dash.dmentions.status;

import org.bukkit.entity.Player;

public class NoPlayerStatusProvider implements PlayerStatusProvider {

    @Override
    public boolean isAFK(Player player) {
        return false;
    }

    @Override
    public boolean isIgnoring(Player player, Player other) {
        return false;
    }
}
