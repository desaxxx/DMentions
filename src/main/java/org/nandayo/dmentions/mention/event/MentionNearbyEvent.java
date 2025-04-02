package org.nandayo.dmentions.mention.event;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class MentionNearbyEvent extends MentionEvent {

    private final Player[] targets;

    public MentionNearbyEvent(Player sender, Player[] targets) {
        super(sender);
        this.targets = targets;
    }
}
