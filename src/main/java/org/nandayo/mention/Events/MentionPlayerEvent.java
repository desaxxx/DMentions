package org.nandayo.mention.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MentionPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player sender;
    private final Player target;

    public MentionPlayerEvent(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
    }

    public Player getSender() {
        return sender;
    }
    public Player getTarget() {
        return target;
    }
}
