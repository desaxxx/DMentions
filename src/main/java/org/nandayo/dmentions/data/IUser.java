package org.nandayo.dmentions.data;

import org.bukkit.entity.Player;

public interface IUser {

    boolean getMentionMode(Player player);
    void setMentionMode(Player player, boolean mode);

    String getMentionDisplay(Player player);
    void setMentionDisplay(Player player, String display);
}
