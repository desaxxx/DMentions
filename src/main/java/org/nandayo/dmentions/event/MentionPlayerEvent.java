package org.nandayo.dmentions.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class MentionPlayerEvent extends MentionEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final @NotNull Player target;

    public MentionPlayerEvent(@NotNull Player sender, @NotNull Player target) {
        super(sender);
        this.target = target;
    }
}
