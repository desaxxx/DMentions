package dev.dash.dmentions.update;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.MessageService;
import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The answer handling and the join notice; the HTTP request itself is not run in tests. */
class UpdateCheckerTest {

    private static final String RELEASE_2_0_1 = "[{\"version_number\":\"2.0.1\",\"version_type\":\"release\"}]";
    private static final String RELEASE_2_0_0 = "[{\"version_number\":\"2.0.0\",\"version_type\":\"release\"}]";

    private final List<LogRecord> logs = new ArrayList<>();
    private Logger logger;

    @BeforeEach
    void captureLogs() {
        logger = Logger.getLogger("UpdateCheckerTest-" + System.nanoTime());
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { logs.add(record); }
            @Override public void flush() {}
            @Override public void close() {}
        });
    }

    private String lastLog() {
        return logs.getLast().getMessage();
    }

    @Test
    void newerReleaseIsRememberedAndLogged() {
        UpdateChecker checker = new UpdateChecker(logger, "2.0.0");

        checker.handle(RELEASE_2_0_1);

        assertEquals(Optional.of("2.0.1"), checker.newerVersion());
        assertEquals(Level.INFO, logs.getLast().getLevel());
        assertTrue(lastLog().contains("2.0.1") && lastLog().contains(UpdateChecker.PROJECT_PAGE), lastLog());
    }

    @Test
    void sameVersionIsUpToDate() {
        UpdateChecker checker = new UpdateChecker(logger, "2.0.0");

        checker.handle(RELEASE_2_0_0);

        assertEquals(Optional.empty(), checker.newerVersion());
        assertEquals("DMentions is up to date.", lastLog());
    }

    @Test
    void developmentBuildAheadOfModrinthIsNotToldToDowngrade() {
        UpdateChecker checker = new UpdateChecker(logger, "2.1.0-SNAPSHOT");

        checker.handle(RELEASE_2_0_1);

        assertEquals(Optional.empty(), checker.newerVersion());
    }

    @Test
    void answerWithoutAReleaseIsAWarning() {
        UpdateChecker checker = new UpdateChecker(logger, "2.0.0");

        checker.handle("[]");

        assertEquals(Optional.empty(), checker.newerVersion());
        assertEquals(Level.WARNING, logs.getLast().getLevel());
    }

    // ---- join notice ----

    private ServerMock server;

    @TempDir
    Path dir;

    @AfterEach
    void unmock() {
        if (server != null) MockBukkit.unmock();
    }

    private List<String> received(PlayerMock player) {
        List<String> lines = new ArrayList<>();
        Component next;
        while ((next = player.nextComponentMessage()) != null) {
            lines.add(PlainTextComponentSerializer.plainText().serialize(next));
        }
        return lines;
    }

    /** Joins as an operator: MockBukkit only allows op after the player is added, so the join is fired again. */
    private PlayerMock joinAsOperator(String name) {
        PlayerMock player = server.addPlayer(name);
        player.setOp(true);
        received(player);
        server.getPluginManager().callEvent(new org.bukkit.event.player.PlayerJoinEvent(player, (Component) null));
        return player;
    }

    private void listen(UpdateChecker checker) {
        server = MockBukkit.mock();
        XConfig config = XConfig.builder(dir.resolve("config.yml")).keys(ConfigKeys.class).logger(logger).build();
        XConfig lang = XConfig.builder(dir.resolve("lang.yml")).keys(LangKeys.class).logger(logger).build();
        server.getPluginManager().registerEvents(new UpdateNotifyListener(checker, new MessageService(config, lang)),
                MockBukkit.createMockPlugin());
    }

    @Test
    void playersWithThePermissionAreToldAboutANewerVersionWhenTheyJoin() {
        UpdateChecker checker = new UpdateChecker(logger, "2.0.0");
        checker.handle(RELEASE_2_0_1);
        listen(checker);

        PlayerMock admin = joinAsOperator("Admin");
        PlayerMock steve = server.addPlayer("Steve");

        assertEquals(List.of("[DMentions] A new version is available: 2.0.1 (current: 2.0.0) Download"), received(admin));
        assertNull(steve.nextComponentMessage());
    }

    @Test
    void nobodyIsToldWhenUpToDate() {
        UpdateChecker checker = new UpdateChecker(logger, "2.0.0");
        checker.handle(RELEASE_2_0_0);
        listen(checker);

        PlayerMock admin = joinAsOperator("Admin");

        assertNull(admin.nextComponentMessage());
    }
}
