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

    /**
     * Writes files into the plugin folder and starts a fresh plugin instance so onEnable reads them.
     * Paper only accepts lifecycle (command) handlers during a plugin instance's first enable,
     * so re-enabling the same instance is not an option.
     */
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
        plugin = MockBukkit.load(DMentions.class);
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
        server.getScheduler().performTicks(2);
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
    void ownNameStaysPlainAndIsNotNotified() throws InterruptedException {
        AsyncChatEvent event = chat(alex, "I am Alex");

        assertEquals(Component.text("I am Alex"), event.message());
        assertEquals(0, soundsHeard(alex));
        assertNull(alex.nextActionBar());
    }

    @Test
    void ownNameCanBeRepeatedWithoutCooldownWarnings() throws InterruptedException {
        chat(alex, "Alex");
        AsyncChatEvent second = chat(alex, "Alex");

        assertEquals(Component.text("Alex"), second.message());
        assertNull(alex.nextActionBar());
    }

    @Test
    void ownNameDoesNotUseUpTheMentionLimit() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");

        AsyncChatEvent event = chat(alex, "Alex Alex Steve Bob");

        assertEquals("Alex Alex @Steve @Bob", plain(event.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals(1, soundsHeard(bob));
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
        server.getScheduler().performTicks(2);

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

    // ---- @nearby ----

    private static final String NEARBY_PERMISSION = "dmentions.mention.nearby";
    private static final TextColor NEARBY_COLOR = TextColor.fromHexString("#ea79b8");

    private void allowNearby(PlayerMock player) {
        player.addAttachment(plugin, NEARBY_PERMISSION, true);
    }

    /** Puts the player in the main world at the given x/z, everyone at the same height. */
    private void placeAt(PlayerMock player, double x, double z) {
        player.teleport(new org.bukkit.Location(server.getWorlds().getFirst(), x, 64, z));
    }

    @Test
    void nearbyNotifiesPlayersInRangeOnly() throws InterruptedException {
        PlayerMock far = addPlayer("Far");
        placeAt(alex, 0, 0);
        placeAt(steve, 10, 0);
        placeAt(far, 50, 0);
        allowNearby(alex);

        AsyncChatEvent event = chat(alex, "help @nearby");

        assertEquals("help @nearby", plain(event.message()));
        assertEquals(NEARBY_COLOR, colourOf(event.message(), "@nearby"));
        assertEquals(1, soundsHeard(steve));
        assertTrue(steve.getHeardSounds().getFirst().getSound().contains("entity.player.levelup"));
        assertEquals(PREFIX + "Player Alex mentioned nearby players.", plain(steve.nextActionBar()));
        assertEquals("You have been mentioned in chat.", plain(steve.titles.getFirst().subtitle()));
        assertEquals(0, soundsHeard(far));
        assertNull(far.nextActionBar());
        assertEquals(PREFIX + "You mentioned nearby players.", plain(alex.nextActionBar()));
        assertEquals(0, soundsHeard(alex));
    }

    @Test
    void nearbyUsesABoxAroundTheSender() throws InterruptedException {
        // Same as 1.8.4: the radius is checked per axis, so a corner 15/15 away (~21.2 blocks) is still in range.
        PlayerMock outside = addPlayer("Outside");
        placeAt(alex, 0, 0);
        placeAt(steve, 15, 15);
        placeAt(outside, 21, 0);
        allowNearby(alex);

        chat(alex, "@nearby");

        assertEquals(1, soundsHeard(steve));
        assertEquals(0, soundsHeard(outside));
    }

    @Test
    void nearbyIgnoresOtherWorlds() throws InterruptedException {
        placeAt(alex, 0, 0);
        steve.teleport(new org.bukkit.Location(server.addSimpleWorld("other"), 0, 64, 0));
        allowNearby(alex);

        chat(alex, "@nearby");

        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar(), "nobody was reached, so no confirmation");
    }

    @Test
    void nearbyRadiusComesFromConfig() throws Exception {
        restartWith("""
                nearby:
                  radius: 5
                """);
        placeAt(alex, 0, 0);
        placeAt(steve, 10, 0);
        allowNearby(alex);

        chat(alex, "@nearby");

        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void nearbyUsesPositionWhenTheMentionIsDelivered() throws InterruptedException {
        // Targets are resolved on the sender's thread one tick after the chat event; delivery happens on the next tick.
        placeAt(alex, 0, 0);
        placeAt(steve, 100, 0);
        allowNearby(alex);

        Set<Audience> viewers = new HashSet<>(server.getOnlinePlayers());
        Component message = Component.text("@nearby");
        AsyncChatEvent event = new AsyncChatEvent(true, alex, viewers, ChatRenderer.defaultRenderer(),
                message, message, null);
        Thread chatThread = new Thread(() -> server.getPluginManager().callEvent(event));
        chatThread.start();
        chatThread.join();

        placeAt(steve, 5, 0);
        server.getScheduler().performTicks(2);

        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void nearbyNeedsPermissionThatOnlyOperatorsHaveByDefault() throws InterruptedException {
        org.bukkit.permissions.Permission permission = server.getPluginManager().getPermission(NEARBY_PERMISSION);
        assertNotNull(permission);
        assertEquals(org.bukkit.permissions.PermissionDefault.OP, permission.getDefault());
        placeAt(alex, 0, 0);
        placeAt(steve, 5, 0);

        AsyncChatEvent event = chat(alex, "@nearby");

        assertEquals(Component.text("@nearby"), event.message());
        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void nearbyHasItsOwnCooldown() throws InterruptedException {
        placeAt(alex, 0, 0);
        placeAt(steve, 5, 0);
        allowNearby(alex);
        allowEveryone(alex);

        chat(alex, "@nearby");
        alex.nextActionBar();
        AsyncChatEvent second = chat(alex, "@nearby");

        assertEquals("@nearby", plain(second.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals("You must wait 10s before mentioning.", plain(alex.nextActionBar()));

        assertEquals("@everyone", plain(chat(alex, "@everyone").message()));
        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void disabledNearbyLeavesTheKeywordAlone() throws Exception {
        restartWith("""
                nearby:
                  enabled: false
                """);
        alex.setOp(true);
        placeAt(alex, 0, 0);
        placeAt(steve, 5, 0);

        assertEquals(Component.text("@nearby"), chat(alex, "@nearby").message());
        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void turkishNearbyMessages() throws Exception {
        restartWith("lang_file: tr-TR\n", "tr-TR");
        placeAt(alex, 0, 0);
        placeAt(steve, 5, 0);
        allowNearby(alex);

        chat(alex, "@nearby");

        assertEquals(PREFIX + "Alex adlı oyuncu yakındaki oyunculardan bahsetti.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "Yakındaki oyunculardan bahsettin.", plain(alex.nextActionBar()));
    }

    // ---- one notification per player per message ----

    @Test
    void playerAlsoCoveredByEveryoneIsNotifiedOnce() throws InterruptedException {
        allowEveryone(alex);

        AsyncChatEvent event = chat(alex, "@everyone Steve");

        assertEquals("@everyone @Steve", plain(event.message()), "both keywords are still coloured");
        assertEquals(1, soundsHeard(steve));
        assertTrue(steve.getHeardSounds().getFirst().getSound().contains("entity.wither.spawn"), "first mention wins");
        assertEquals(PREFIX + "Player Alex mentioned everyone.", plain(steve.nextActionBar()));
        assertNull(steve.nextActionBar());
        assertEquals(1, steve.titles.size());
        assertEquals(PREFIX + "You mentioned everyone.", plain(alex.nextActionBar()));
        assertNull(alex.nextActionBar(), "the player mention reached nobody new, so no confirmation for it");
    }

    @Test
    void firstMentionInTheMessageDecidesTheNotification() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");
        allowEveryone(alex);

        chat(alex, "Steve @everyone");

        assertEquals(1, soundsHeard(steve));
        assertTrue(steve.getHeardSounds().getFirst().getSound().contains("block.note_block.pling"));
        assertEquals(PREFIX + "Alex mentioned you.", plain(steve.nextActionBar()));
        assertNull(steve.nextActionBar());
        assertEquals(PREFIX + "Player Alex mentioned everyone.", plain(bob.nextActionBar()));
        assertEquals(PREFIX + "You mentioned Steve.", plain(alex.nextActionBar()));
        assertEquals(PREFIX + "You mentioned everyone.", plain(alex.nextActionBar()));
    }

    @Test
    void samePlayerTwiceInOneMessageIsNotifiedOnce() throws InterruptedException {
        AsyncChatEvent event = chat(alex, "Steve Steve");

        assertEquals("@Steve @Steve", plain(event.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals(PREFIX + "You mentioned Steve.", plain(alex.nextActionBar()));
        assertNull(alex.nextActionBar());
    }

    @Test
    void skippedDuplicateStillStartsItsCooldown() throws InterruptedException {
        allowEveryone(alex);

        chat(alex, "@everyone Steve");
        alex.nextActionBar();
        AsyncChatEvent next = chat(alex, "Steve");

        assertEquals("Steve", plain(next.message()));
        assertEquals("You must wait 5s before mentioning.", plain(alex.nextActionBar()));
    }

    @Test
    void multipleNotificationsCanBeAllowed() throws Exception {
        restartWith("allow_multiple_notifications: true\n");
        allowEveryone(alex);

        chat(alex, "@everyone Steve");

        assertEquals(2, soundsHeard(steve));
        assertEquals(PREFIX + "Player Alex mentioned everyone.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "Alex mentioned you.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "You mentioned everyone.", plain(alex.nextActionBar()));
        assertEquals(PREFIX + "You mentioned Steve.", plain(alex.nextActionBar()));
    }

    // ---- @group ----

    private static final TextColor GROUP_COLOR = TextColor.fromHexString("#73c7dc");

    /** Stands in for LuckPerms: groups and their members are set by hand. */
    static final class FakeGroups implements dev.dash.dmentions.group.GroupProvider {
        final java.util.Map<String, Set<UUID>> members = new java.util.LinkedHashMap<>();

        FakeGroups group(String name, PlayerMock... players) {
            Set<UUID> ids = members.computeIfAbsent(name, n -> new HashSet<>());
            for (PlayerMock p : players) ids.add(p.getUniqueId());
            return this;
        }

        @Override
        public List<String> groups() {
            return List.copyOf(members.keySet());
        }

        @Override
        public boolean isMember(org.bukkit.entity.Player player, String group) {
            return members.getOrDefault(group, Set.of()).contains(player.getUniqueId());
        }
    }

    /** Registers the fake provider the way another plugin would, then restarts so onEnable picks it up. */
    private void useGroups(FakeGroups groups, String configYaml) throws IOException {
        server.getServicesManager().register(dev.dash.dmentions.group.GroupProvider.class, groups,
                MockBukkit.createMockPlugin(), org.bukkit.plugin.ServicePriority.Normal);
        restartWith(configYaml);
    }

    private void allowGroup(PlayerMock player, String group) {
        player.addAttachment(plugin, "dmentions.mention.group." + group, true);
    }

    @Test
    void groupMentionNotifiesItsMembersOnly() throws Exception {
        PlayerMock bob = addPlayer("Bob");
        PlayerMock carl = addPlayer("Carl");
        useGroups(new FakeGroups().group("vip", steve, bob).group("default", carl), "");
        allowGroup(alex, "vip");

        AsyncChatEvent event = chat(alex, "hi @vip");

        assertEquals("hi @vip", plain(event.message()));
        assertEquals(GROUP_COLOR, colourOf(event.message(), "@vip"));
        for (PlayerMock member : List.of(steve, bob)) {
            assertEquals(1, soundsHeard(member), member.getName());
            assertTrue(member.getHeardSounds().getFirst().getSound().contains("item.totem.use"));
            assertEquals(PREFIX + "Player Alex mentioned vip members.", plain(member.nextActionBar()));
        }
        assertEquals("You have been mentioned in chat.", plain(steve.titles.getFirst().subtitle()));
        assertEquals(0, soundsHeard(carl));
        assertNull(carl.nextActionBar());
        assertEquals(PREFIX + "You mentioned vip members.", plain(alex.nextActionBar()));
    }

    @Test
    void groupPermissionIsPerGroup() throws Exception {
        useGroups(new FakeGroups().group("vip", steve).group("staff", steve), "");
        allowGroup(alex, "vip");

        assertEquals(Component.text("hi @staff"), chat(alex, "hi @staff").message());
        assertEquals(0, soundsHeard(steve));

        assertEquals("hi @vip", plain(chat(alex, "hi @vip").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void groupNeedsPermissionThatOnlyOperatorsHaveByDefault() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), "");

        assertEquals(Component.text("@vip"), chat(alex, "@vip").message());
        assertEquals(0, soundsHeard(steve));

        alex.setOp(true);
        assertEquals("@vip", plain(chat(alex, "@vip").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void groupPermissionNodeComesFromConfig() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  permission: "server.ping.<group>"
                """);
        alex.addAttachment(plugin, "server.ping.vip", true);

        assertEquals("@vip", plain(chat(alex, "@vip").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void disabledGroupsCannotBeMentionedAndIgnoreCase() throws Exception {
        useGroups(new FakeGroups().group("admin", steve).group("vip", steve), """
                group:
                  disabled_groups:
                    - admin
                    - VIP
                """);
        alex.setOp(true);

        assertEquals(Component.text("@admin"), chat(alex, "@admin").message());
        assertEquals(Component.text("@vip"), chat(alex, "@vip").message());
        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void senderInsideTheGroupIsNotNotified() throws Exception {
        useGroups(new FakeGroups().group("vip", alex, steve), "");
        allowGroup(alex, "vip");

        chat(alex, "@vip");

        assertEquals(1, soundsHeard(steve));
        assertEquals(0, soundsHeard(alex));
        assertEquals(PREFIX + "You mentioned vip members.", plain(alex.nextActionBar()));
    }

    @Test
    void groupWithOnlyTheSenderSendsNoConfirmation() throws Exception {
        useGroups(new FakeGroups().group("vip", alex), "");
        allowGroup(alex, "vip");

        assertEquals("@vip", plain(chat(alex, "@vip").message()));
        assertNull(alex.nextActionBar());
    }

    @Test
    void groupAndOneOfItsMembersInOneMessageNotifyOnce() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), "");
        allowGroup(alex, "vip");

        chat(alex, "@vip Steve");

        assertEquals(1, soundsHeard(steve));
        assertTrue(steve.getHeardSounds().getFirst().getSound().contains("item.totem.use"));
    }

    @Test
    void groupCooldownIsPerGroup() throws Exception {
        useGroups(new FakeGroups().group("vip", steve).group("staff", steve), "");
        allowGroup(alex, "vip");
        allowGroup(alex, "staff");

        chat(alex, "@vip");
        alex.nextActionBar();
        AsyncChatEvent second = chat(alex, "@vip");

        assertEquals("@vip", plain(second.message()));
        assertEquals(1, soundsHeard(steve));
        assertEquals("You must wait 5s before mentioning.", plain(alex.nextActionBar()));

        assertEquals("@staff", plain(chat(alex, "@staff").message()));
        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void groupWinsOverAPlayerWithTheSameKeyword() throws Exception {
        PlayerMock namedVip = addPlayer("vip");
        useGroups(new FakeGroups().group("vip", steve), """
                player:
                  keyword: "@<player>"
                """);
        allowGroup(alex, "vip");

        AsyncChatEvent event = chat(alex, "@vip");

        assertEquals(GROUP_COLOR, colourOf(event.message(), "@vip"));
        assertEquals(1, soundsHeard(steve));
        assertEquals(0, soundsHeard(namedVip));
    }

    @Test
    void withoutAGroupProviderGroupKeywordsStayPlain() throws InterruptedException {
        alex.setOp(true);

        assertEquals(Component.text("hi @vip"), chat(alex, "hi @vip").message());
        assertNull(alex.nextActionBar());
    }

    @Test
    void disabledGroupMentionsLeaveTheKeywordAlone() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  enabled: false
                """);
        alex.setOp(true);

        assertEquals(Component.text("@vip"), chat(alex, "@vip").message());
        assertEquals(0, soundsHeard(steve));
    }

    @Test
    void customGroupKeywordAndDisplay() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  keyword: "#<group>"
                  display: "<gold>[<group>]"
                """);
        allowGroup(alex, "vip");

        assertEquals("hi [vip]", plain(chat(alex, "hi #vip").message()));
        assertEquals(1, soundsHeard(steve));
    }

    @Test
    void turkishGroupMessages() throws Exception {
        server.getServicesManager().register(dev.dash.dmentions.group.GroupProvider.class,
                new FakeGroups().group("vip", steve), MockBukkit.createMockPlugin(), org.bukkit.plugin.ServicePriority.Normal);
        restartWith("lang_file: tr-TR\n", "tr-TR");
        allowGroup(alex, "vip");

        chat(alex, "@vip");

        assertEquals(PREFIX + "Alex adlı oyuncu vip üyelerinden bahsetti.", plain(steve.nextActionBar()));
        assertEquals(PREFIX + "vip üyelerinden bahsettin.", plain(alex.nextActionBar()));
    }

    // ---- group.list overrides ----

    @Test
    void groupListDisplayAppliesToThatGroupOnly() throws Exception {
        useGroups(new FakeGroups().group("vip", steve).group("staff", steve), """
                group:
                  list:
                    vip:
                      display: "<gold>[<group>]"
                    ghost:
                      display: "<red>never used"
                """);
        allowGroup(alex, "vip");
        allowGroup(alex, "staff");
        alex.addAttachment(plugin).setPermission("dmentions.mention.group.ghost", true);

        AsyncChatEvent event = chat(alex, "@vip @staff @ghost");

        assertEquals("[vip] @staff @ghost", plain(event.message()));
        assertEquals(TextColor.color(0xFFAA00), colourOf(event.message(), "[vip]"));
        assertEquals(GROUP_COLOR, colourOf(event.message(), "@staff"));
    }

    @Test
    void groupListNameMatchIgnoresCase() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  list:
                    VIP:
                      display: "<gold>[<group>]"
                """);
        allowGroup(alex, "vip");

        assertEquals("[vip]", plain(chat(alex, "@vip").message()));
    }

    @Test
    void groupListCooldownOverridesTheGlobalOne() throws Exception {
        useGroups(new FakeGroups().group("vip", steve).group("staff", steve), """
                group:
                  list:
                    vip:
                      cooldown: 30s
                """);
        allowGroup(alex, "vip");
        allowGroup(alex, "staff");

        chat(alex, "@vip");
        alex.nextActionBar();
        chat(alex, "@vip");
        assertEquals("You must wait 30s before mentioning.", plain(alex.nextActionBar()));

        chat(alex, "@staff");
        alex.nextActionBar();
        chat(alex, "@staff");
        assertEquals("You must wait 5s before mentioning.", plain(alex.nextActionBar()));
    }

    @Test
    void groupListZeroCooldownTurnsTheCooldownOff() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  list:
                    vip:
                      cooldown: 0
                """);
        allowGroup(alex, "vip");

        chat(alex, "@vip");
        chat(alex, "@vip");

        assertEquals(2, soundsHeard(steve));
    }

    @Test
    void groupListSoundOverridesTheGlobalOne() throws Exception {
        useGroups(new FakeGroups().group("vip", steve).group("staff", steve), """
                allow_multiple_notifications: true
                group:
                  list:
                    vip:
                      sound: "minecraft:block.bell.use"
                """);
        allowGroup(alex, "vip");
        allowGroup(alex, "staff");

        chat(alex, "@vip @staff");

        assertEquals(2, soundsHeard(steve));
        assertTrue(steve.getHeardSounds().get(0).getSound().contains("block.bell.use"));
        assertTrue(steve.getHeardSounds().get(1).getSound().contains("item.totem.use"));
    }

    @Test
    void emptyGroupListSoundIsSilentButStillNotifies() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  list:
                    vip:
                      sound: ""
                """);
        allowGroup(alex, "vip");

        chat(alex, "@vip");

        assertEquals(0, soundsHeard(steve));
        assertEquals(PREFIX + "Player Alex mentioned vip members.", plain(steve.nextActionBar()));
    }

    @Test
    void disabledGroupStaysDisabledEvenWithAGroupListEntry() throws Exception {
        useGroups(new FakeGroups().group("vip", steve), """
                group:
                  disabled_groups: [vip]
                  list:
                    vip:
                      display: "<gold>[<group>]"
                """);
        alex.setOp(true);

        assertEquals(Component.text("@vip"), chat(alex, "@vip").message());
        assertEquals(0, soundsHeard(steve));
    }

    // ---- /dms toggle ----

    /** Turns mentions off through the command. Do it before any restart: MockBukkit keeps the first instance's commands. */
    private void toggleOff(PlayerMock player) {
        player.performCommand("dms toggle");
        while (player.nextComponentMessage() != null) ;
    }

    @Test
    void toggledOffPlayerStaysPlainAndGetsNothing() throws InterruptedException {
        toggleOff(steve);

        AsyncChatEvent event = chat(alex, "hi Steve");

        assertEquals(Component.text("hi Steve"), event.message());
        assertEquals(0, soundsHeard(steve));
        assertNull(steve.nextActionBar());
        assertNull(alex.nextActionBar());
    }

    @Test
    void nearbySkipsToggledOffPlayers() throws InterruptedException {
        PlayerMock bob = addPlayer("Bob");
        placeAt(alex, 0, 0);
        placeAt(steve, 5, 0);
        placeAt(bob, 5, 0);
        allowNearby(alex);
        toggleOff(steve);

        chat(alex, "help @nearby");

        assertEquals(0, soundsHeard(steve));
        assertEquals(1, soundsHeard(bob));
    }

    @Test
    void nearbyWithOnlyToggledOffPlayersSendsNoConfirmation() throws InterruptedException {
        placeAt(alex, 0, 0);
        placeAt(steve, 5, 0);
        allowNearby(alex);
        toggleOff(steve);

        chat(alex, "help @nearby");

        assertEquals(0, soundsHeard(steve));
        assertNull(alex.nextActionBar());
    }

    @Test
    void groupSkipsToggledOffMembers() throws Exception {
        PlayerMock bob = addPlayer("Bob");
        toggleOff(steve);
        useGroups(new FakeGroups().group("vip", steve, bob), "");
        server.getScheduler().waitAsyncTasksFinished(); // the new instance loads online players asynchronously
        allowGroup(alex, "vip");

        chat(alex, "hi @vip");

        assertEquals(0, soundsHeard(steve));
        assertEquals(1, soundsHeard(bob));
    }

    @Test
    void everyoneStillReachesToggledOffPlayers() throws InterruptedException {
        allowEveryone(alex);
        toggleOff(steve);

        chat(alex, "hey @everyone");

        assertEquals(1, soundsHeard(steve));
        assertEquals(PREFIX + "Player Alex mentioned everyone.", plain(steve.nextActionBar()));
    }
}
