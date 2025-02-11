package org.nandayo.DMentions.mention.Events;

import org.bukkit.entity.Player;

public class MentionNearbyEvent extends MentionEvent {

    private final Player[] targets;

    public MentionNearbyEvent(Player sender, Player[] targets) {
        super(sender);
        this.targets = targets;
    }

    public Player[] getTargets() {
        return targets;
    }
}
