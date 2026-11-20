package dev.dash.dmentions;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end chat tests on a mocked Paper server: the real plugin is loaded,
 * players chat through {@link AsyncChatEvent} and we check the resulting
 * message, sounds and action bars.
 */
class ChatMentionIntegrationTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final TextColor MENTION_COLOR = TextColor.fromHexString("#a9e871");

    private ServerMock server;
    private PlayerMock alex;
    private PlayerMock steve;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        MockBukkit.load(DMentions.class);
        alex = server.addPlayer("Alex");
        steve = server.addPlayer("Steve");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /** Fires an async chat event the way Paper does (off the main thread), then runs one tick. */
    private AsyncChatEvent chat(PlayerMock sender, String text) throws InterruptedException {
        Set<Audience> viewers = new HashSet<>(server.getOnlinePlayers());
        Component message = Component.text(text);
        AsyncChatEvent event = new AsyncChatEvent(true, sender, viewers, ChatRenderer.defaultRenderer(),
                message, message, null);
        Thread chatThread = new Thread(() -> server.getPluginManager().callEvent(event));
        chatThread.start();
        chatThread.join();
        server.getScheduler().performOneTick();
        return event;
    }

    private static String plain(Component component) {
        return component == null ? null : PLAIN.serialize(component);
    }

    private static int soundsHeard(PlayerMock player) {
        return player.getHeardSounds().size();
    }

    @Test
    void mentionIsColouredAndBothSidesAreNotified() throws InterruptedException {
        AsyncChatEvent event = chat(alex, "hi Steve!");

        assertEquals("hi @Steve!", plain(event.message()));
        assertEquals(1, soundsHeard(steve));
        assertTrue(steve.getHeardSounds().getFirst().getSound().contains("block.note_block.pling"));
        assertEquals("Alex mentioned you", plain(steve.nextActionBar()));
        assertEquals("You mentioned Steve", plain(alex.nextActionBar()));
        assertEquals(0, soundsHeard(alex));
    }

    @Test
    void mentionUsesConfiguredColour() throws InterruptedException {
        AsyncChatEvent event = chat(alex, "Steve");
        Component mention = event.message().children().getFirst();
        assertEquals(MENTION_COLOR, mention.color());
    }

    @Test
    void messageWithoutMentionIsUntouched() throws InterruptedException {
        AsyncChatEvent event = chat(alex, "hello everyone");

        assertEquals(Component.text("hello everyone"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar());
    }

    @Test
    void selfMentionIsColouredButNotNotified() throws InterruptedException {
        AsyncChatEvent event = chat(alex, "I am Alex");

        assertEquals("I am @Alex", plain(event.message()));
        assertEquals(0, soundsHeard(alex));
        assertNull(alex.nextActionBar());
    }

    @Test
    void caseInsensitiveByDefault() throws InterruptedException {
        assertEquals("hi @Steve", plain(chat(alex, "hi steve").message()));
    }

    @Test
    void mentionLimitIsRespected() throws InterruptedException {
        PlayerMock bob = server.addPlayer("Bob");
        PlayerMock carl = server.addPlayer("Carl");

        AsyncChatEvent event = chat(alex, "Steve Bob Carl");

        assertEquals("@Steve @Bob Carl", plain(event.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals(1, soundsHeard(bob));
        assertEquals(0, soundsHeard(carl));
    }

    @Test
    void cooldownBlocksSecondMentionAndWarnsSender() throws InterruptedException {
        chat(alex, "Steve");
        AsyncChatEvent second = chat(alex, "Steve again");

        assertEquals("Steve again", plain(second.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals("You mentioned Steve", plain(alex.nextActionBar()));
        assertEquals("Wait 5s before mentioning again", plain(alex.nextActionBar()));
    }

    @Test
    void cooldownIsPerSender() throws InterruptedException {
        PlayerMock bob = server.addPlayer("Bob");

        chat(alex, "Steve");
        AsyncChatEvent fromBob = chat(bob, "Steve");

        assertEquals("@Steve", plain(fromBob.message()));
        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void bypassPermissionSkipsCooldown() throws InterruptedException {
        alex.setOp(true);

        chat(alex, "Steve");
        AsyncChatEvent second = chat(alex, "Steve again");

        assertEquals("@Steve again", plain(second.message()));
        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void messageCancelledAfterUsSendsNothingAndStartsNoCooldown() throws InterruptedException {
        AtomicBoolean cancelNext = new AtomicBoolean(true);
        // Registered after DMentions on the same priority, so it runs right after our HIGHEST handler.
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void cancel(AsyncChatEvent event) {
                if (cancelNext.getAndSet(false)) {
                    event.setCancelled(true);
                }
            }
        }, MockBukkit.createMockPlugin());

        AsyncChatEvent cancelled = chat(alex, "Steve");
        assertTrue(cancelled.isCancelled());
        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar());

        AsyncChatEvent next = chat(alex, "Steve");
        assertEquals("@Steve", plain(next.message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void messageCancelledBeforeUsIsIgnored() throws InterruptedException {
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void cancel(AsyncChatEvent event) {
                event.setCancelled(true);
            }
        }, MockBukkit.createMockPlugin());

        AsyncChatEvent event = chat(alex, "Steve");

        assertEquals("Steve", plain(event.message()));
        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void targetWhoLeftBeforeTheTickGetsNothing() throws InterruptedException {
        Set<Audience> viewers = new HashSet<>(server.getOnlinePlayers());
        Component message = Component.text("Steve");
        AsyncChatEvent event = new AsyncChatEvent(true, alex, viewers, ChatRenderer.defaultRenderer(),
                message, message, null);
        Thread chatThread = new Thread(() -> server.getPluginManager().callEvent(event));
        chatThread.start();
        chatThread.join();

        steve.disconnect();
        server.getScheduler().performOneTick();

        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar());
    }
}
