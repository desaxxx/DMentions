package dev.dash.dmentions.migration;

import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.message.Channel;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.XConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyLangMigratorTest {

    private static final Logger SILENT = Logger.getLogger("LegacyLangMigratorTest");

    static {
        SILENT.setLevel(Level.OFF);
    }

    @TempDir
    Path dir;

    private Path shipped(String name) throws IOException {
        Path backupLang = Files.createDirectories(dir.resolve("backup/lang"));
        try (InputStream in = getClass().getResourceAsStream("/legacy-1.8.4/lang/" + name + ".yml")) {
            Files.copy(in, backupLang.resolve(name + ".yml"));
        }
        return backupLang;
    }

    private XConfig read(Path file) {
        return XConfig.builder(file).keys(LangKeys.class).logger(SILENT).build();
    }

    @Test
    void aSingleLineBecomesAListAndALineWithoutChannelIsChat() {
        assertEquals(List.of(new Notification(Channel.CHAT, "<red>Player not found.", "")),
                LegacyLangMigrator.toNotifications("&cPlayer not found.", "p", LegacyLangMigrator.PLACEHOLDERS));
    }

    @Test
    void channelsTitlesAndPlaceholdersAreConverted() {
        List<Notification> result = LegacyLangMigrator.toNotifications(List.of(
                "ACTION_BAR={PREFIX} &ePlayer {sender} waited {remained}",
                "TITLE={PREFIX}||&#f4e4c0Mentioned",
                ""), "p", LegacyLangMigrator.PLACEHOLDERS);

        assertEquals(List.of(
                new Notification(Channel.ACTION_BAR, "<prefix> <yellow>Player <sender> waited <remaining>", ""),
                new Notification(Channel.TITLE, "<prefix>", "<#F4E4C0>Mentioned")), result);
    }

    @Test
    void mappedMessagesOfTheShippedFileAreCarriedOver() throws IOException {
        Path backupLang = shipped("en-US");
        Path langFolder = dir.resolve("lang");

        LegacyLangMigrator.migrate(backupLang, langFolder, () -> { }, SILENT);
        XConfig lang = read(langFolder.resolve("en-US.yml"));

        assertEquals(List.of(new Notification(Channel.ACTION_BAR, "<red>You must wait <remaining> before mentioning.", "")),
                lang.get(LangKeys.COOLDOWN_WARN));
        assertEquals(List.of(
                        new Notification(Channel.ACTION_BAR, "<prefix> <#F4E4C0>Player </#F4E4C0><#DDF7A6><sender></#DDF7A6><#F4E4C0> mentioned you in chat.", ""),
                        new Notification(Channel.TITLE, "<prefix>", "<#F4E4C0>You have been mentioned in chat.")),
                lang.get(LangKeys.PLAYER_TARGET_MESSAGE));
        assertEquals(List.of(new Notification(Channel.CHAT, "<yellow>You will now be mentioned.", "")),
                lang.get(LangKeys.COMMAND_TOGGLE_ENABLED));
        assertEquals(List.of(new Notification(Channel.CHAT,
                        "<green>You changed your mention display from </green><red><old_value></red><green> to </green><yellow><value></yellow><green>.", "")),
                lang.get(LangKeys.COMMAND_CUSTOMIZE_SUCCESS));
    }

    @Test
    void groupMessagesComeFromTheDefaultGroup() throws IOException {
        Path backupLang = shipped("en-US");
        Path langFolder = dir.resolve("lang");

        LegacyLangMigrator.migrate(backupLang, langFolder, () -> { }, SILENT);

        assertTrue(read(langFolder.resolve("en-US.yml")).get(LangKeys.GROUP_SENDER_MESSAGE).getFirst().text().contains("<group>"));
    }

    @Test
    void messagesWithoutA1Point8CounterpartGetThe2Point0Text() throws IOException {
        Path backupLang = shipped("en-US");
        Path langFolder = dir.resolve("lang");

        LegacyLangMigrator.migrate(backupLang, langFolder, () -> { }, SILENT);

        XConfig lang = read(langFolder.resolve("en-US.yml"));
        assertEquals(LangKeys.COMMAND_HELP_HEADER.defaultValue(), lang.get(LangKeys.COMMAND_HELP_HEADER));
        String file = Files.readString(langFolder.resolve("en-US.yml"));
        assertFalse(file.contains("vanish_notify"), "1.8 only messages are dropped");
        assertFalse(file.contains("lang_version"), file);
        assertFalse(file.contains("{PREFIX}"), file);
    }

    // like zh-CN or a server's own translation: nothing in the jar to start from
    @Test
    void aLanguageThatIsNotShippedKeepsItsMessagesAndGetsEnglishForTheRest() throws IOException {
        Path backupLang = Files.createDirectories(dir.resolve("backup/lang"));
        Files.writeString(backupLang.resolve("es-ES.yml"), """
                cooldown_warn: "ACTION_BAR=&cEspera {remained}."
                player:
                  target_message:
                    - "ACTION_BAR={PREFIX} &e{sender} te ha mencionado."
                lang_version: "1.8.4"
                """);
        Path langFolder = dir.resolve("lang");

        LegacyLangMigrator.migrate(backupLang, langFolder, () -> { }, SILENT);
        XConfig lang = read(langFolder.resolve("es-ES.yml"));

        assertEquals(List.of(new Notification(Channel.ACTION_BAR, "<red>Espera <remaining>.", "")), lang.get(LangKeys.COOLDOWN_WARN));
        assertEquals(List.of(new Notification(Channel.ACTION_BAR, "<prefix> <yellow><sender> te ha mencionado.", "")),
                lang.get(LangKeys.PLAYER_TARGET_MESSAGE));
        assertEquals(LangKeys.PLAYER_SENDER_MESSAGE.defaultValue(), lang.get(LangKeys.PLAYER_SENDER_MESSAGE));
        assertEquals(LangKeys.COMMAND_HELP_HEADER.defaultValue(), lang.get(LangKeys.COMMAND_HELP_HEADER));
    }

    @Test
    void theOld1Point8FileIsReplacedAndShippedOnesAreRestoredFirst() throws IOException {
        Path backupLang = shipped("tr-TR");
        Path langFolder = Files.createDirectories(dir.resolve("lang"));
        Files.writeString(langFolder.resolve("tr-TR.yml"), "afk_warn: \"old\"\n");
        List<String> restored = new ArrayList<>();

        LegacyLangMigrator.migrate(backupLang, langFolder, () -> {
            restored.add(Files.exists(langFolder.resolve("tr-TR.yml")) ? "still there" : "removed");
        }, SILENT);

        assertEquals(List.of("removed"), restored, "the bundled copy must be written into an empty spot");
        String file = Files.readString(langFolder.resolve("tr-TR.yml"));
        assertFalse(file.contains("afk_warn"), file);
    }

    @Test
    void oldPlaceholderNamesOfTheShippedTurkishFileAreUnderstood() throws IOException {
        Path backupLang = shipped("tr-TR");
        Path langFolder = dir.resolve("lang");

        LegacyLangMigrator.migrate(backupLang, langFolder, () -> { }, SILENT);
        XConfig lang = read(langFolder.resolve("tr-TR.yml"));

        assertTrue(lang.get(LangKeys.COOLDOWN_WARN).getFirst().text().contains("<remaining>"), "{REMAINED}");
        // {p}: the mentioned player when telling the sender, the sender everywhere else
        assertTrue(lang.get(LangKeys.PLAYER_SENDER_MESSAGE).getFirst().text().contains("<target>"));
        assertTrue(lang.get(LangKeys.PLAYER_TARGET_MESSAGE).getFirst().text().contains("<sender>"));
        assertTrue(lang.get(LangKeys.EVERYONE_TARGET_MESSAGE).getFirst().text().contains("<sender>"));
        assertFalse(Files.readString(langFolder.resolve("tr-TR.yml")).contains("{p}"));
    }
}
