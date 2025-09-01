package org.nandayo.dmentions.provider;

import org.bukkit.entity.Player;
import org.nandayo.dmentions.DMentions;

public interface VanishProvider {

    static VanishProvider get() {
        return DMentions.inst().getVanishProvider();
    }

    boolean isVanished(Player player);
}
