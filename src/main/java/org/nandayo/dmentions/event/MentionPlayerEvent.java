package org.nandayo.dmentions.mention.event;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class MentionPlayerEvent extends MentionEvent {

    private final Player target;

    public MentionPlayerEvent(Player sender, Player target) {
        super(sender);
        this.target = target;
    }
}
