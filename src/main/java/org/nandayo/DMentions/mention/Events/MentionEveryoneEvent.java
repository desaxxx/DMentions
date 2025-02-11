package org.nandayo.DMentions.mention.Events;

import org.bukkit.entity.Player;

public class MentionEveryoneEvent extends MentionEvent {

    private final Player[] targets;

    public MentionEveryoneEvent(Player sender, Player[] targets) {
        super(sender);
        this.targets = targets;
    }

    public Player[] getTargets() {
        return targets;
    }
}
