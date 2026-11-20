package dev.dash.dmentions.mention;

import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class PendingMentions {

    private final ConcurrentHashMap<AsyncChatEvent, List<PendingMention>> byEvent = new ConcurrentHashMap<>();

    public void put(AsyncChatEvent event, List<PendingMention> pending) {
        if (pending.isEmpty()) return;
        byEvent.put(event, pending);
    }

    public List<PendingMention> take(AsyncChatEvent event) {
        List<PendingMention> pending = byEvent.remove(event);
        if (pending != null) return pending;
        return List.of();
    }
}
