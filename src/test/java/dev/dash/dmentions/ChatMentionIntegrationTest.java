package dev.dash.dmentions;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private static final String PREFIX = "[DMentions] ";

    private ServerMock server;
    private DMentions plugin;
    private TitlePlayer alex;
    private TitlePlayer steve;

    /** MockBukkit does not record Adventure titles, so the test players keep them. */
    static final class TitlePlayer extends PlayerMock {
        final List<Title> titles = new ArrayList<>();

        TitlePlayer(ServerMock server, String name) {
            super(server, name, UUID.nameUUIDFromBytes(name.getBytes()));
        }

        @Override
        public void showTitle(Title title) {
            titles.add(title);
        }
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DMentions.class);
        alex = addPlayer("Alex");
        steve = addPlayer("Steve");
    }

    private TitlePlayer addPlayer(String name) {
        TitlePlayer player = new TitlePlayer(server, name);
        server.addPlayer(player);
        return player;
    }

    /** Writes files into the plugin folder and restarts the plugin so onEnable reads them. */
    private void restartWith(String configYaml, String... bundledLangs) throws IOException {
        Path data = plugin.getDataPath();
        Files.writeString(data.resolve("config.yml"), configYaml);
        Files.createDirectories(data.resolve("lang"));
        for (String langFile : bundledLangs) {
            try (InputStream in = getClass().getResourceAsStream("/lang/" + langFile + ".yml")) {
                Files.copy(in, data.resolve("lang/" + langFile + ".yml"));
            }
        }
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
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
        assertEquals(PREFIX + "Alex mentioned you.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "You mentioned Steve.", plain(alex.nextActionBar()));
        assertEquals(0, soundsHeard(alex));
    }

    @Test
    void targetGetsTitleWithPrefixAndSubtitle() throws InterruptedException {
        chat(alex, "hi Steve");

        assertEquals(1, steve.titles.size());
        Title title = steve.titles.getFirst();
        assertEquals(PREFIX.strip(), plain(title.title()));
        assertEquals("You have been mentioned in chat.", plain(title.subtitle()));
        assertTrue(alex.titles.isEmpty());
    }

    @Test
    void prefixKeepsItsColours() throws InterruptedException {
        chat(alex, "Steve");
        Component bar = steve.nextActionBar();
        // <dark_gray>[<#ffd16d>DMentions<dark_gray>]
        assertEquals(TextColor.fromHexString("#ffd16d"), colourOf(bar, "DMentions"));
    }

    /** Colour of the first text node with exactly this content, inheriting from parents. */
    private static TextColor colourOf(Component component, String content) {
        return colourOf(component, content, null);
    }

    private static TextColor colourOf(Component component, String content, TextColor inherited) {
        TextColor colour = component.color() != null ? component.color() : inherited;
        if (component instanceof net.kyori.adventure.text.TextComponent text && text.content().equals(content)) {
            return colour;
        }
        for (Component child : component.children()) {
            TextColor found = colourOf(child, content, colour);
            if (found != null) return found;
        }
        return null;
    }

    @Test
    void turkishLanguageFileIsUsed() throws Exception {
        restartWith("lang_file: tr-TR\n", "tr-TR");

        chat(alex, "selam Steve");

        assertEquals(1, soundsHeard(steve));
        assertEquals(PREFIX + "Alex adlı oyuncu sohbette senden bahsetti.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "Steve adlı oyuncudan bahsettin.", plain(alex.nextActionBar()));
        assertEquals("Sohbette biri senden bahsetti.", plain(steve.titles.getFirst().subtitle()));
    }

    @Test
    void missingLanguageFileFallsBackToEnglish() throws Exception {
        restartWith("lang_file: xx-XX\n");

        chat(alex, "Steve");

        assertEquals(PREFIX + "Alex mentioned you.", plain(steve.nextActionBar()));
        assertTrue(Files.exists(plugin.getDataPath().resolve("lang/en-US.yml")));
        assertTrue(Files.notExists(plugin.getDataPath().resolve("lang/xx-XX.yml")));
    }

    @Test
    void customChannelsAndDisabledMessagesAreHonoured() throws Exception {
        Path lang = plugin.getDataPath().resolve("lang");
        Files.createDirectories(lang);
        Files.writeString(lang.resolve("custom.yml"), """
                player:
                  sender_message: []
                  target_message:
                    - "CHAT=<prefix> <sender> pinged you"
                """);
        restartWith("lang_file: custom\nprefix: \"<red>!\"\n");

        chat(alex, "Steve");

        assertEquals("! Alex pinged you", plain(steve.nextComponentMessage()));
        assertNull(steve.nextActionBar());
        assertTrue(steve.titles.isEmpty());
        assertNull(alex.nextActionBar());
        assertNull(alex.nextComponentMessage());
        assertEquals(1, soundsHeard(steve));
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
    void keywordTemplateIsApplied() throws Exception {
        restartWith("""
                player:
                  keyword: "@<player>"
                  display: "<#a9e871>[<player>]"
                """);

        AsyncChatEvent withAt = chat(alex, "hi @Steve");
        assertEquals("hi [Steve]", plain(withAt.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals(PREFIX + "You mentioned Steve.", plain(alex.nextActionBar()));

        PlayerMock bob = addPlayer("Bob");
        AsyncChatEvent withoutAt = chat(alex, "hi Bob");
        assertEquals("hi Bob", plain(withoutAt.message()));
        assertEquals(0, soundsHeard(bob));
    }

    @Test
    void disabledPlayerMentionsLeaveTheMessageAlone() throws Exception {
        restartWith("""
                player:
                  enabled: false
                """);

        AsyncChatEvent event = chat(alex, "hi Steve");

        assertEquals(Component.text("hi Steve"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar());
    }

    @Test
    void playerWhoJoinsLaterCanBeMentioned() throws InterruptedException {
        PlayerMock late = addPlayer("Latecomer");

        assertEquals("hi @Latecomer", plain(chat(alex, "hi Latecomer").message()));
        assertEquals(1, soundsHeard(late));
    }

    @Test
    void caseInsensitiveByDefault() throws InterruptedException {
        assertEquals("hi @Steve", plain(chat(alex, "hi steve").message()));
    }

    @Test
    void mentionLimitIsRespected() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");
        PlayerMock carl = addPlayer("Carl");

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
        assertEquals(PREFIX + "You mentioned Steve.", plain(alex.nextActionBar()));
        assertEquals("You must wait 5s before mentioning.", plain(alex.nextActionBar()));
    }

    @Test
    void cooldownIsPerSender() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");

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

    // ---- Stage 4b: permissions ----

    /** The permission node the generated config.yml uses for player mentions. */
    private String playerPermission() {
        return org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(plugin.getDataPath().resolve("config.yml").toFile())
                .getString("player.permission");
    }

    @Test
    void playerPermissionIsRegisteredForEveryone() {
        String node = playerPermission();
        org.bukkit.permissions.Permission permission = server.getPluginManager().getPermission(node);

        assertNotNull(permission, "player permission was not registered: " + node);
        assertEquals(org.bukkit.permissions.PermissionDefault.TRUE, permission.getDefault());
        assertTrue(alex.hasPermission(node));
    }

    @Test
    void senderWithoutPermissionCannotMention() throws InterruptedException {
        alex.addAttachment(plugin, playerPermission(), false);

        AsyncChatEvent event = chat(alex, "hi Steve");

        assertEquals(Component.text("hi Steve"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar(), "a forbidden mention should be silent");
    }

    @Test
    void permissionOnlyRestrictsTheSender() throws InterruptedException {
        steve.addAttachment(plugin, playerPermission(), false);

        assertEquals("hi @Steve", plain(chat(alex, "hi Steve").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void customPermissionFromConfigIsRegisteredAndOldOneRemoved() throws Exception {
        String oldNode = playerPermission();
        restartWith("""
                player:
                  permission: "server.mention"
                """);

        assertNotNull(server.getPluginManager().getPermission("server.mention"));
        assertNull(server.getPluginManager().getPermission(oldNode), "old node survived the restart");

        alex.addAttachment(plugin, "server.mention", false);
        assertEquals("hi Steve", plain(chat(alex, "hi Steve").message()));
        assertEquals("hi @Alex", plain(chat(steve, "hi Alex").message()));
    }

    @Test
    void emptyPermissionLetsEveryoneMention() throws Exception {
        String oldNode = playerPermission();
        restartWith("""
                player:
                  permission: ""
                """);
        alex.addAttachment(plugin, oldNode, false);

        assertEquals("hi @Steve", plain(chat(alex, "hi Steve").message()));
    }

    @Test
    void disablingThePluginRemovesItsPermission() {
        String node = playerPermission();
        Bukkit.getPluginManager().disablePlugin(plugin);

        assertNull(server.getPluginManager().getPermission(node));
    }

    // ---- Stage 4b: disabled worlds ----

    private org.bukkit.World disableWorld(String name) throws IOException {
        restartWith("disabled_worlds:\n  - " + name + "\n");
        return server.addSimpleWorld(name);
    }

    @Test
    void mentionInDisabledWorldIsBlockedAndWarnedOnce() throws Exception {
        alex.teleport(disableWorld("nomention").getSpawnLocation());
        PlayerMock bob = addPlayer("Bob");

        AsyncChatEvent event = chat(alex, "hi Steve and Bob");

        assertEquals(Component.text("hi Steve and Bob"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertEquals(0, soundsHeard(bob));
        assertEquals(PREFIX + "You can't mention a player in this world.", plain(alex.nextActionBar()));
        assertNull(alex.nextActionBar(), "warned more than once");
    }

    @Test
    void chatWithoutMentionInDisabledWorldIsNotWarned() throws Exception {
        alex.teleport(disableWorld("nomention").getSpawnLocation());

        AsyncChatEvent event = chat(alex, "hello everyone");

        assertEquals(Component.text("hello everyone"), event.message());
        assertNull(alex.nextActionBar());
    }

    @Test
    void blockedMentionStartsNoCooldown() throws Exception {
        org.bukkit.World normal = server.getWorlds().getFirst();
        alex.teleport(disableWorld("nomention").getSpawnLocation());
        chat(alex, "Steve");
        alex.nextActionBar();

        alex.teleport(normal.getSpawnLocation());
        AsyncChatEvent event = chat(alex, "Steve");

        assertEquals("@Steve", plain(event.message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void targetInDisabledWorldCanStillBeMentioned() throws Exception {
        steve.teleport(disableWorld("nomention").getSpawnLocation());

        assertEquals("hi @Steve", plain(chat(alex, "hi Steve").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void disabledWorldWarnFillsWorldPlaceholder() throws Exception {
        Path lang = plugin.getDataPath().resolve("lang");
        Files.createDirectories(lang);
        Files.writeString(lang.resolve("custom.yml"), """
                disabled_world_warn:
                  - "CHAT=no mentions in <world>"
                """);
        restartWith("""
                lang_file: custom
                disabled_worlds:
                  - nomention
                """);
        alex.teleport(server.addSimpleWorld("nomention").getSpawnLocation());

        chat(alex, "Steve");

        assertEquals("no mentions in nomention", plain(alex.nextComponentMessage()));
        assertNull(alex.nextActionBar());
    }

    @Test
    void defaultConfigDoesNotDisableTheMainWorld() throws InterruptedException {
        assertEquals("@Steve", plain(chat(alex, "Steve").message()));
    }

    // ---- Stage 4c: @everyone ----

    private static final String EVERYONE_PERMISSION = "dmentions.mention.everyone";
    private static final TextColor EVERYONE_COLOR = TextColor.fromHexString("#8fb56c");

    /** Grants @everyone without making the player an operator (operators also bypass cooldowns). */
    private void allowEveryone(PlayerMock player) {
        player.addAttachment(plugin, EVERYONE_PERMISSION, true);
    }

    @Test
    void everyoneNotifiesAllOtherPlayers() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");
        allowEveryone(alex);

        AsyncChatEvent event = chat(alex, "hey @everyone!");

        assertEquals("hey @everyone!", plain(event.message()));
        assertEquals(EVERYONE_COLOR, colourOf(event.message(), "@everyone"));
        for (PlayerMock target : List.of(steve, bob)) {
            assertEquals(1, soundsHeard(target), target.getName());
            assertTrue(target.getHeardSounds().getFirst().getSound().contains("entity.wither.spawn"));
            assertEquals(PREFIX + "Player Alex mentioned everyone.", plain(target.nextActionBar()));
        }
        assertEquals("You have been mentioned in chat.", plain(steve.titles.getFirst().subtitle()));
        assertEquals(PREFIX + "You mentioned everyone.", plain(alex.nextActionBar()));
        assertEquals(0, soundsHeard(alex));
        assertTrue(alex.titles.isEmpty());
    }

    @Test
    void everyoneNeedsPermissionThatOnlyOperatorsHaveByDefault() throws InterruptedException {
        org.bukkit.permissions.Permission permission = server.getPluginManager().getPermission(EVERYONE_PERMISSION);
        assertNotNull(permission);
        assertEquals(org.bukkit.permissions.PermissionDefault.OP, permission.getDefault());

        AsyncChatEvent event = chat(alex, "hey @everyone");

        assertEquals(Component.text("hey @everyone"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar());
    }

    @Test
    void operatorCanMentionEveryone() throws InterruptedException {
        alex.setOp(true);

        assertEquals("@everyone", plain(chat(alex, "@everyone").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void everyoneHasItsOwnCooldown() throws InterruptedException {
        allowEveryone(alex);

        chat(alex, "@everyone");
        alex.nextActionBar();
        AsyncChatEvent second = chat(alex, "@everyone again");

        assertEquals("@everyone again", plain(second.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals("You must wait 10s before mentioning.", plain(alex.nextActionBar()));

        // A player mention is not blocked by the @everyone cooldown.
        assertEquals("@Steve", plain(chat(alex, "Steve").message()));
        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void everyoneCooldownIsPerSender() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");
        allowEveryone(alex);
        allowEveryone(bob);

        chat(alex, "@everyone");
        AsyncChatEvent fromBob = chat(bob, "@everyone");

        assertEquals("@everyone", plain(fromBob.message()));
        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void everyoneAloneOnTheServerSendsNoConfirmation() throws InterruptedException {
        steve.disconnect();
        allowEveryone(alex);

        AsyncChatEvent event = chat(alex, "@everyone");

        assertEquals("@everyone", plain(event.message()));
        assertNull(alex.nextActionBar());
    }

    @Test
    void disabledEveryoneLeavesTheKeywordAlone() throws Exception {
        restartWith("""
                everyone:
                  enabled: false
                """);
        alex.setOp(true);

        AsyncChatEvent event = chat(alex, "hey @everyone");

        assertEquals(Component.text("hey @everyone"), event.message());
        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void customEveryoneKeywordAndDisplay() throws Exception {
        restartWith("""
                everyone:
                  keyword: "@all"
                  display: "<gold>[ALL]"
                """);
        allowEveryone(alex);

        assertEquals("hi [ALL]", plain(chat(alex, "hi @all").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void everyoneWinsOverAPlayerNamedEveryone() throws Exception {
        restartWith("""
                player:
                  keyword: "@<player>"
                """);
        PlayerMock namedEveryone = addPlayer("everyone");
        allowEveryone(alex);

        AsyncChatEvent event = chat(alex, "@everyone");

        assertEquals(EVERYONE_COLOR, colourOf(event.message(), "@everyone"));
        assertEquals(PREFIX + "You mentioned everyone.", plain(alex.nextActionBar()));
        assertEquals(PREFIX + "Player Alex mentioned everyone.", plain(namedEveryone.nextActionBar()));
    }

    @Test
    void everyoneIsBlockedInDisabledWorld() throws Exception {
        allowEveryone(alex);
        alex.teleport(disableWorld("nomention").getSpawnLocation());

        AsyncChatEvent event = chat(alex, "@everyone");

        assertEquals(Component.text("@everyone"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertEquals(PREFIX + "You can't mention a player in this world.", plain(alex.nextActionBar()));
    }

    @Test
    void turkishEveryoneMessages() throws Exception {
        restartWith("lang_file: tr-TR\n", "tr-TR");
        allowEveryone(alex);

        chat(alex, "@everyone");

        assertEquals(PREFIX + "Alex adlı oyuncu herkesten bahsetti.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "Herkesten bahsettin.", plain(alex.nextActionBar()));
    }
}
