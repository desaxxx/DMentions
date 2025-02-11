package org.nandayo.DMentions.mention.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MentionEvent extends Event implements Mention {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player sender;

    public MentionEvent(Player sender) {
        this.sender = sender;
    }

    @Override
    public Player getSender() {
        return sender;
    }

}
