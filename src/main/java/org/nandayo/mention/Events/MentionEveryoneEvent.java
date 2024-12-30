package org.nandayo.mention.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MentionEveryoneEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player sender;
    private final Player[] targets;

    public MentionEveryoneEvent(Player sender, Player[] targets) {
        this.sender = sender;
        this.targets = targets;
    }

    public Player getSender() {
        return sender;
    }
    public Player[] getTargets() {
        return targets;
    }
}
