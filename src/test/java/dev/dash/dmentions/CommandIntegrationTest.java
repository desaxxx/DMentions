package dev.dash.dmentions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
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

        assertEquals(List.of(HEADER, VERSION_LINE), received(alex));
    }

    @Test
    void helpLiteralAndAliasesShowTheSameHelp() {
        for (String command : List.of("dmentions help", "dms", "dms help", "mentions", "mentions help")) {
            alex.performCommand(command);
            assertEquals(List.of(HEADER, VERSION_LINE), received(alex), command);
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
        assertEquals(List.of(HEADER), received(alex));

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
            assertEquals(List.of("help", "version"), clientSubCommands(alex, label), label);
        }

        alex.addAttachment(plugin, "dmentions.command.version", false);
        assertEquals(List.of("help"), clientSubCommands(alex, "dms"));
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
        assertEquals(List.of(PREFIX + "Komutlar:", "/dms version - Eklenti sürümünü gösterir"), received(alex));

        alex.performCommand("dms version");
        assertEquals(List.of(PREFIX + "DMentions " + version() + " sürümü çalışıyor"), received(alex));
    }
}
