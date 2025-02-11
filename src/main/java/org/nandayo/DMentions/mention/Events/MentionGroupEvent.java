package org.nandayo.DMentions.mention.Events;

import org.bukkit.entity.Player;

public class MentionGroupEvent extends MentionEvent {

    private final String groupName;
    private final Player[] targets;

    public MentionGroupEvent(Player sender, String groupName, Player[] targets) {
        super(sender);
        this.groupName = groupName;
        this.targets = targets;
    }

    public String  getGroup() {
        return groupName;
    }
    public Player[] getTargets() {
        return targets;
    }
}
