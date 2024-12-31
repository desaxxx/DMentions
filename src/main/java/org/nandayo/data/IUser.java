package org.nandayo.data;

import org.bukkit.entity.Player;

public interface IUser {

    boolean getMentionMode(Player player);
    void setMentionMode(Player player, boolean mode);
}
