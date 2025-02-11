package org.nandayo.DMentions.mention.Events;

import org.bukkit.entity.Player;

public class MentionPlayerEvent extends MentionEvent {

    private final Player target;

    public MentionPlayerEvent(Player sender, Player target) {
        super(sender);
        this.target = target;
    }

    public Player getTarget() {
        return target;
    }
}
