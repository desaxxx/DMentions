package org.nandayo.dmentions.mention.event;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class MentionEveryoneEvent extends MentionEvent {

    private final Player[] targets;

    public MentionEveryoneEvent(Player sender, Player[] targets) {
        super(sender);
        this.targets = targets;
    }
}
