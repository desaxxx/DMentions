package dev.dash.dmentions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.CommandSourceStackMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.command.brigadier.PaperCommandsMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** /dmentions on a mocked Paper server: the command tree is registered through the lifecycle API. */
class CommandIntegrationTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final String PREFIX = "[DMentions] ";
    private static final String HEADER = PREFIX + "Commands:";
    private static final String VERSION_LINE = "/dms version - Show the plugin version";
    private static final String TOGGLE_LINE = "/dms toggle - Turn being mentioned on or off";
    private static final String SEND_LINE = "/dms send <keyword> - Send a mention";
    private static final String RELOAD_LINE = "/dms reload - Reload the config and language files";
    private static final String RELOADED = PREFIX + "Reloaded the config and language files.";

    private ServerMock server;
    private DMentions plugin;
    private PlayerMock alex;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DMentions.class);
        alex = server.addPlayer("Alex");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /** All chat lines the player received since the last call, as plain text. */
    private static List<String> received(PlayerMock player) {
        List<String> lines = new ArrayList<>();
        Component next;
        while ((next = player.nextComponentMessage()) != null) {
            lines.add(PLAIN.serialize(next));
        }
        return lines;
    }

    private String version() {
        return plugin.getPluginMeta().getVersion();
    }

    @Test
    void rootCommandShowsHelp() {
        alex.performCommand("dmentions");

        assertEquals(List.of(HEADER, TOGGLE_LINE, VERSION_LINE), received(alex));
    }

    @Test
    void helpLiteralAndAliasesShowTheSameHelp() {
        for (String command : List.of("dmentions help", "dms", "dms help", "mentions", "mentions help")) {
            alex.performCommand(command);
            assertEquals(List.of(HEADER, TOGGLE_LINE, VERSION_LINE), received(alex), command);
        }
    }

    @Test
    void versionPrintsThePluginVersion() {
        alex.performCommand("dms version");

        assertEquals(List.of(PREFIX + "Running DMentions " + version()), received(alex));
    }

    @Test
    void versionWorksFromTheConsole() {
        ConsoleCommandSenderMock console = (ConsoleCommandSenderMock) server.getConsoleSender();

        server.dispatchCommand(console, "dms version");

        Component message = console.nextComponentMessage();
        assertNotNull(message);
        assertEquals(PREFIX + "Running DMentions " + version(), PLAIN.serialize(message));
    }

    @Test
    void withoutPermissionTheSubCommandIsHiddenFromHelpAndCannotRun() {
        alex.addAttachment(plugin, "dmentions.command.version", false);

        alex.performCommand("dms");
        assertEquals(List.of(HEADER, TOGGLE_LINE), received(alex));

        alex.performCommand("dms version");
        assertTrue(received(alex).stream().noneMatch(line -> line.contains("Running DMentions")));
    }

    /**
     * The sub commands the client gets under this label. Paper sends each player a command tree with
     * the nodes they cannot use left out, and the client builds tab completion from that tree; plain
     * Brigadier suggestions skip that filtering, so it is applied here with {@code canUse}.
     * MockBukkit builds the tree on the first dispatched command (like a server finishing startup),
     * so one harmless command is run from the console first.
     */
    private List<String> clientSubCommands(PlayerMock player, String label) {
        server.dispatchCommand(server.getConsoleSender(), "dmentions help");
        CommandDispatcher<CommandSourceStack> dispatcher = PaperCommandsMock.INSTANCE.getDispatcherInternal();
        CommandSourceStack source = CommandSourceStackMock.from(player);
        return dispatcher.getRoot().getChild(label).getChildren().stream()
                .filter(node -> node.canUse(source))
                .map(CommandNode::getName)
                .sorted()
                .toList();
    }

    @Test
    void tabCompletionListsHelpAndPermittedSubCommands() {
        for (String label : List.of("dmentions", "dms", "mentions", "dmentions:dmentions")) {
            assertEquals(List.of("help", "toggle", "version"), clientSubCommands(alex, label), label);
        }

        alex.addAttachment(plugin, "dmentions.command.version", false);
        assertEquals(List.of("help", "toggle"), clientSubCommands(alex, "dms"));
    }

    @Test
    void commandPermissionIsRegisteredWithItsDefaultAndRemovedOnDisable() {
        Permission permission = server.getPluginManager().getPermission("dmentions.command.version");
        assertNotNull(permission);
        assertEquals(PermissionDefault.TRUE, permission.getDefault());

        Bukkit.getPluginManager().disablePlugin(plugin);

        assertNull(server.getPluginManager().getPermission("dmentions.command.version"));
    }

    /** Paper accepts command handlers only on a plugin instance's first enable, so a restart means a new instance. */
    private void restart() {
        Bukkit.getPluginManager().disablePlugin(plugin);
        plugin = MockBukkit.load(DMentions.class);
    }

    @Test
    void restartingThePluginKeepsOneWorkingCommand() {
        restart();

        alex.performCommand("dms version");

        assertEquals(List.of(PREFIX + "Running DMentions " + version()), received(alex));
    }

    @Test
    void turkishCommandMessages() throws IOException {
        Path data = plugin.getDataPath();
        Files.writeString(data.resolve("config.yml"), "lang_file: tr-TR\n");
        Files.createDirectories(data.resolve("lang"));
        try (InputStream in = getClass().getResourceAsStream("/lang/tr-TR.yml")) {
            Files.copy(in, data.resolve("lang/tr-TR.yml"));
        }
        restart();

        alex.performCommand("dms");
        assertEquals(List.of(PREFIX + "Komutlar:", "/dms toggle - Etiketlenmeyi açar veya kapatır", "/dms version - Eklenti sürümünü gösterir"), received(alex));

        alex.performCommand("dms version");
        assertEquals(List.of(PREFIX + "DMentions " + version() + " sürümü çalışıyor"), received(alex));
    }

    // ---- send / reload ----

    @Test
    void operatorSeesEverySubCommandInHelpAndTabCompletion() {
        alex.setOp(true);

        alex.performCommand("dms");
        assertEquals(List.of(HEADER, TOGGLE_LINE, SEND_LINE, RELOAD_LINE, VERSION_LINE), received(alex));
        assertEquals(List.of("help", "reload", "send", "toggle", "version"), clientSubCommands(alex, "dms"));
    }

    @Test
    void sendAndReloadPermissionsDefaultToOp() {
        for (String node : List.of("dmentions.command.send", "dmentions.command.reload")) {
            Permission permission = server.getPluginManager().getPermission(node);
            assertNotNull(permission, node);
            assertEquals(PermissionDefault.OP, permission.getDefault(), node);
        }
    }

    /** Runs the command, then lets the chat event it causes (fired async by MockBukkit) finish and deliver. */
    private void performAndDeliver(PlayerMock player, String command) {
        player.performCommand(command);
        server.getScheduler().waitAsyncEventsFinished();
        server.getScheduler().performTicks(2);
    }

    @Test
    void sendMentionsThroughTheChatPipeline() {
        PlayerMock steve = server.addPlayer("Steve");
        alex.setOp(true);

        performAndDeliver(alex, "dms send Steve");

        assertEquals(1, steve.getHeardSounds().size());
        assertEquals(PREFIX + "Alex mentioned you.", PLAIN.serialize(steve.nextActionBar()));
    }

    @Test
    void sendMatchesTheKeywordIgnoringCaseByDefault() {
        PlayerMock steve = server.addPlayer("Steve");
        alex.setOp(true);

        performAndDeliver(alex, "dms send steve");

        assertEquals(1, steve.getHeardSounds().size());
    }

    @Test
    void sendRejectsAnUnknownKeyword() {
        alex.setOp(true);

        alex.performCommand("dms send Nobody");

        assertEquals(List.of(PREFIX + "Invalid keyword: Nobody"), received(alex));
    }

    @Test
    void sendWithoutAKeywordShowsItsUsage() {
        alex.setOp(true);

        alex.performCommand("dms send");

        assertEquals(List.of(SEND_LINE), received(alex));
    }

    @Test
    void sendFromTheConsoleIsRefused() {
        ConsoleCommandSenderMock console = (ConsoleCommandSenderMock) server.getConsoleSender();

        server.dispatchCommand(console, "dms send Alex");

        assertEquals(PREFIX + "You must be a player to use this command.", PLAIN.serialize(console.nextComponentMessage()));
    }

    @Test
    void sendSuggestsTheKeywordsThePlayerCanUse() throws Exception {
        server.addPlayer("Steve");
        server.addPlayer("Stella");
        alex.setOp(true);
        server.dispatchCommand(server.getConsoleSender(), "dmentions help");
        CommandDispatcher<CommandSourceStack> dispatcher = PaperCommandsMock.INSTANCE.getDispatcherInternal();
        CommandSourceStack source = CommandSourceStackMock.from(alex);

        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("dms send st", source)).get();

        assertEquals(List.of("Stella", "Steve"), suggestions.getList().stream().map(s -> s.getText()).toList());
    }

    private void editConfig(String path, Object value) throws IOException {
        Path file = plugin.getDataPath().resolve("config.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        yaml.set(path, value);
        yaml.save(file.toFile());
    }

    @Test
    void reloadAppliesConfigChangesWithoutARestart() throws IOException {
        alex.setOp(true);
        editConfig("prefix", "<gray>[New]");

        alex.performCommand("dms reload");
        assertEquals(List.of("[New] Reloaded the config and language files."), received(alex));

        alex.performCommand("dms version");
        assertEquals(List.of("[New] Running DMentions " + version()), received(alex));
    }

    @Test
    void reloadSwitchesTheLanguageFile() throws IOException {
        alex.setOp(true);
        Path data = plugin.getDataPath();
        Files.createDirectories(data.resolve("lang"));
        try (InputStream in = getClass().getResourceAsStream("/lang/tr-TR.yml")) {
            Files.copy(in, data.resolve("lang/tr-TR.yml"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        editConfig("lang_file", "tr-TR");

        alex.performCommand("dms reload");

        assertEquals(List.of(PREFIX + "Config ve dil dosyaları yeniden yüklendi."), received(alex));
    }

    @Test
    void reloadReRegistersConfiguredPermissionNodes() throws IOException {
        String oldNode = YamlConfiguration.loadConfiguration(plugin.getDataPath().resolve("config.yml").toFile())
                .getString("player.permission");
        editConfig("player.permission", "custom.mention.player");

        server.dispatchCommand(server.getConsoleSender(), "dms reload");

        assertNull(server.getPluginManager().getPermission(oldNode));
        assertNotNull(server.getPluginManager().getPermission("custom.mention.player"));
        assertNotNull(server.getPluginManager().getPermission("dmentions.command.reload"));
    }

    @Test
    void reloadKeepsMentionsWorking() throws IOException {
        PlayerMock steve = server.addPlayer("Steve");
        alex.setOp(true);

        alex.performCommand("dms reload");
        performAndDeliver(alex, "dms send Steve");

        assertEquals(1, steve.getHeardSounds().size());
    }

    @Test
    void reloadReportsABrokenConfig() throws IOException {
        alex.setOp(true);
        Files.writeString(plugin.getDataPath().resolve("config.yml"), "prefix: [unclosed\n  : :");

        alex.performCommand("dms reload");

        assertEquals(List.of(PREFIX + "Reload failed. See the console for details."), received(alex));
    }

    // ---- toggle ----

    @Test
    void toggleSwitchesBeingMentionedOffAndOn() {
        PlayerMock steve = server.addPlayer("Steve");
        alex.setOp(true);

        steve.performCommand("dms toggle");
        assertEquals(List.of(PREFIX + "You will no longer be mentioned."), received(steve));
        performAndDeliver(alex, "dms send Steve");
        assertEquals(List.of(PREFIX + "Invalid keyword: Steve"), received(alex));
        assertEquals(0, steve.getHeardSounds().size());

        steve.performCommand("dms toggle");
        assertEquals(List.of(PREFIX + "You will now be mentioned."), received(steve));
        performAndDeliver(alex, "dms send Steve");
        assertEquals(1, steve.getHeardSounds().size());
    }

    @Test
    void togglePermissionDefaultsToEveryone() {
        Permission permission = server.getPluginManager().getPermission("dmentions.command.toggle");
        assertNotNull(permission);
        assertEquals(PermissionDefault.TRUE, permission.getDefault());
    }

    @Test
    void toggleFromTheConsoleIsRefused() {
        ConsoleCommandSenderMock console = (ConsoleCommandSenderMock) server.getConsoleSender();

        server.dispatchCommand(console, "dms toggle");

        assertEquals(PREFIX + "You must be a player to use this command.", PLAIN.serialize(console.nextComponentMessage()));
    }

    @Test
    void toggleIsSavedAndSurvivesARestart() throws IOException {
        PlayerMock steve = server.addPlayer("Steve");
        steve.performCommand("dms toggle");
        received(steve);

        restart();
        // the new instance loads players that are already online asynchronously
        server.getScheduler().waitAsyncTasksFinished();

        // checked through chat: MockBukkit keeps the command tree of the first instance after a restart
        alex.chat("hi Steve");
        server.getScheduler().waitAsyncEventsFinished();
        server.getScheduler().performTicks(2);
        assertEquals(0, steve.getHeardSounds().size());
        assertTrue(Files.readString(plugin.getDataPath().resolve("data/users.yml")).contains(steve.getUniqueId().toString()));
    }

    @Test
    void turkishToggleMessages() throws IOException {
        Path data = plugin.getDataPath();
        Files.writeString(data.resolve("config.yml"), "lang_file: tr-TR\n");
        Files.createDirectories(data.resolve("lang"));
        try (InputStream in = getClass().getResourceAsStream("/lang/tr-TR.yml")) {
            Files.copy(in, data.resolve("lang/tr-TR.yml"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        restart();

        alex.performCommand("dms toggle");
        assertEquals(List.of(PREFIX + "Artık etiketlenmeyeceksin."), received(alex));
        alex.performCommand("dms toggle");
        assertEquals(List.of(PREFIX + "Artık etiketlenebilirsin."), received(alex));
    }
}
