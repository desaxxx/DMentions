package org.nandayo.DMentions.mention.Events;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class MentionGroupEvent extends MentionEvent {

    private final String group;
    private final Player[] targets;

    public MentionGroupEvent(Player sender, String groupName, Player[] targets) {
        super(sender);
        this.group = groupName;
        this.targets = targets;
    }
}
