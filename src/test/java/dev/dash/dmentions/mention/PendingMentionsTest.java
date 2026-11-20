package dev.dash.dmentions.mention;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingMentionsTest {

    private static AsyncChatEvent event() {
        // Only identity matters; Player fields of PendingMention stay null in these tests.
        return new AsyncChatEvent(true, null, Set.of(), null, Component.empty(), Component.empty(), null);
    }

    private final PendingMentions pendingMentions = new PendingMentions();

    @Test
    void takeReturnsWhatWasPut() {
        AsyncChatEvent event = event();
        List<PendingMention> list = List.of(new PendingMention(null, null));
        pendingMentions.put(event, list);
        assertEquals(list, pendingMentions.take(event));
    }

    @Test
    void takeRemovesEntry() {
        AsyncChatEvent event = event();
        pendingMentions.put(event, List.of(new PendingMention(null, null)));
        pendingMentions.take(event);
        assertTrue(pendingMentions.take(event).isEmpty());
    }

    @Test
    void unknownEventGivesEmptyList() {
        assertTrue(pendingMentions.take(event()).isEmpty());
    }

    @Test
    void eventsAreKeptApart() {
        AsyncChatEvent first = event();
        AsyncChatEvent second = event();
        List<PendingMention> list = List.of(new PendingMention(null, null));
        pendingMentions.put(first, list);
        assertTrue(pendingMentions.take(second).isEmpty());
        assertEquals(list, pendingMentions.take(first));
    }
}
