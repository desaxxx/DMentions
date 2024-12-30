package org.nandayo.mention.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MentionGroupEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player sender;
    private final String groupName;
    private final Player[] targets;

    public MentionGroupEvent(Player sender, String groupName, Player[] targets) {
        this.sender = sender;
        this.groupName = groupName;
        this.targets = targets;
    }

    public Player getSender() {
        return sender;
    }
    public String  getGroup() {
        return groupName;
    }
    public Player[] getTargets() {
        return targets;
    }
}
