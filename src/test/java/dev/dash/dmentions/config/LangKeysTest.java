package dev.dash.dmentions.config;

import dev.dash.dmentions.message.Channel;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.XConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangKeysTest {

    @TempDir
    Path dir;

    private static final Logger SILENT = Logger.getLogger("LangKeysTest");

    static {
        SILENT.setLevel(Level.OFF);
    }

    private static final List<ConfigKey<List<Notification>>> ALL_KEYS = List.of(
            LangKeys.PLAYER_SENDER_MESSAGE, LangKeys.PLAYER_TARGET_MESSAGE, LangKeys.COOLDOWN_WARN,
            LangKeys.DISABLED_WORLD_WARN, LangKeys.EVERYONE_SENDER_MESSAGE, LangKeys.EVERYONE_TARGET_MESSAGE,
            LangKeys.NEARBY_SENDER_MESSAGE, LangKeys.NEARBY_TARGET_MESSAGE,
            LangKeys.GROUP_SENDER_MESSAGE, LangKeys.GROUP_TARGET_MESSAGE,
            LangKeys.COMMAND_HELP_HEADER, LangKeys.COMMAND_HELP_VERSION, LangKeys.COMMAND_VERSION,
            LangKeys.COMMAND_HELP_SEND, LangKeys.COMMAND_HELP_RELOAD, LangKeys.COMMAND_MUST_BE_PLAYER,
            LangKeys.COMMAND_SEND_INVALID_KEYWORD, LangKeys.COMMAND_RELOAD_SUCCESS, LangKeys.COMMAND_RELOAD_FAILED,
            LangKeys.COMMAND_HELP_TOGGLE, LangKeys.COMMAND_TOGGLE_ENABLED, LangKeys.COMMAND_TOGGLE_DISABLED,
            LangKeys.COMMAND_HELP_USER, LangKeys.COMMAND_USER_MENTIONABLE_ENABLED, LangKeys.COMMAND_USER_MENTIONABLE_DISABLED,
            LangKeys.COMMAND_PLAYER_NOT_FOUND,
            LangKeys.COMMAND_HELP_CUSTOMIZE, LangKeys.COMMAND_CUSTOMIZE_INVALID_CUSTOM_NAME, LangKeys.COMMAND_CUSTOMIZE_SUCCESS,
            LangKeys.COMMAND_USER_CUSTOM_NAME_SUCCESS,
            LangKeys.UPDATE_AVAILABLE);

    private Path file() {
        return dir.resolve("lang.yml");
    }

    private XConfig load() {
        return XConfig.builder(file()).keys(LangKeys.class).logger(SILENT).build();
    }

    private void write(String yaml) throws IOException {
        Files.writeString(file(), yaml);
    }

    @Test
    void defaults() {
        XConfig lang = load();
        assertEquals(List.of(new Notification(Channel.ACTION_BAR,
                        "<prefix> <#f4e4c0>You mentioned <#ddf7a6><target><#f4e4c0>.", "")),
                lang.get(LangKeys.PLAYER_SENDER_MESSAGE));
        assertEquals(List.of(
                        new Notification(Channel.ACTION_BAR, "<prefix> <#ddf7a6><sender> <#f4e4c0>mentioned you.", ""),
                        new Notification(Channel.TITLE, "<prefix>", "<#f4e4c0>You have been mentioned in chat.")),
                lang.get(LangKeys.PLAYER_TARGET_MESSAGE));
        assertEquals(List.of(new Notification(Channel.ACTION_BAR,
                        "<red>You must wait <remaining> before mentioning.", "")),
                lang.get(LangKeys.COOLDOWN_WARN));
        assertEquals(List.of(new Notification(Channel.ACTION_BAR,
                        "<prefix> <red>You can't mention a player in this world.", "")),
                lang.get(LangKeys.DISABLED_WORLD_WARN));
        assertEquals(List.of(new Notification(Channel.ACTION_BAR,
                        "<prefix> <#f4e4c0>You mentioned everyone.", "")),
                lang.get(LangKeys.EVERYONE_SENDER_MESSAGE));
        assertEquals(List.of(
                        new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>Player <#ddf7a6><sender><#f4e4c0> mentioned everyone.", ""),
                        new Notification(Channel.TITLE, "<prefix>", "<#f4e4c0>You have been mentioned in chat.")),
                lang.get(LangKeys.EVERYONE_TARGET_MESSAGE));
        assertEquals(List.of(new Notification(Channel.ACTION_BAR,
                        "<prefix> <#f4e4c0>You mentioned nearby players.", "")),
                lang.get(LangKeys.NEARBY_SENDER_MESSAGE));
        assertEquals(List.of(
                        new Notification(Channel.ACTION_BAR, "<prefix> <#f4e4c0>Player <#ddf7a6><sender><#f4e4c0> mentioned nearby players.", ""),
                        new Notification(Channel.TITLE, "<prefix>", "<#f4e4c0>You have been mentioned in chat.")),
                lang.get(LangKeys.NEARBY_TARGET_MESSAGE));
    }

    @Test
    void generatedFileUsesExpectedPathsAndFormat() {
        load();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file().toFile());
        assertEquals(List.of("ACTION_BAR=<prefix> <#f4e4c0>You mentioned <#ddf7a6><target><#f4e4c0>."),
                yaml.getStringList("player.sender_message"));
        assertEquals(List.of("ACTION_BAR=<prefix> <#ddf7a6><sender> <#f4e4c0>mentioned you.",
                        "TITLE=<prefix>||<#f4e4c0>You have been mentioned in chat."),
                yaml.getStringList("player.target_message"));
        assertEquals(List.of("ACTION_BAR=<red>You must wait <remaining> before mentioning."),
                yaml.getStringList("cooldown_warn"));
        assertFalse(yaml.contains("player.cooldown_warn"));
        assertEquals(List.of("ACTION_BAR=<prefix> <red>You can't mention a player in this world."),
                yaml.getStringList("disabled_world_warn"));
        assertEquals(List.of("CHAT=<prefix> <gray>Commands:"), yaml.getStringList("command.help.header"));
        assertEquals(List.of("CHAT=<#ffd16d>/dms version <gray>- Show the plugin version"),
                yaml.getStringList("command.help.version"));
        assertEquals(List.of("CHAT=<prefix> <gray>Running DMentions <white><version>"),
                yaml.getStringList("command.version"));
    }

    @Test
    void oldCooldownWarnPathIsStillRead() throws IOException {
        write("""
                player:
                  cooldown_warn:
                    - "CHAT=<gray>wait <remaining>"
                """);
        assertEquals(List.of(new Notification(Channel.CHAT, "<gray>wait <remaining>", "")),
                load().get(LangKeys.COOLDOWN_WARN));
    }

    @Test
    void generatedFileReloadsToTheSameValues() {
        XConfig first = load();
        XConfig second = load();
        for (ConfigKey<List<Notification>> key : ALL_KEYS) {
            assertEquals(first.get(key), second.get(key));
        }
    }

    @Test
    void userMessagesAreRead() throws IOException {
        write("""
                player:
                  sender_message:
                    - "CHAT=<green>Pinged <target>"
                    - "TITLE=Hey||<gray>sub"
                """);
        assertEquals(List.of(
                        new Notification(Channel.CHAT, "<green>Pinged <target>", ""),
                        new Notification(Channel.TITLE, "Hey", "<gray>sub")),
                load().get(LangKeys.PLAYER_SENDER_MESSAGE));
    }

    @Test
    void emptyListDisablesTheMessage() throws IOException {
        write("""
                player:
                  sender_message: []
                """);
        assertEquals(List.of(), load().get(LangKeys.PLAYER_SENDER_MESSAGE));
    }

    @Test
    void blankLineDoesNotProduceAnEmptyMessage() throws IOException {
        write("""
                player:
                  sender_message:
                    - "ACTION_BAR="
                """);
        List<Notification> messages = load().get(LangKeys.PLAYER_SENDER_MESSAGE);
        assertTrue(messages.stream().noneMatch(n -> n.text().isBlank() && n.subtitle().isBlank()),
                "blank notification slipped through: " + messages);
    }

    @Test
    void bundledTurkishFileIsCompleteAndValid() throws IOException {
        try (InputStream in = LangKeysTest.class.getResourceAsStream("/lang/tr-TR.yml")) {
            assertNotNull(in, "lang/tr-TR.yml is not bundled");
            Files.copy(in, file());
        }
        String before = Files.readString(file());

        XConfig lang = load();

        // xconfig appends missing keys; an unchanged file means every key is present.
        assertEquals(before, Files.readString(file()), "tr-TR.yml is missing keys");
        XConfig english = XConfig.builder(dir.resolve("en.yml")).keys(LangKeys.class).logger(SILENT).build();
        for (ConfigKey<List<Notification>> key : ALL_KEYS) {
            List<Notification> turkish = lang.get(key);
            assertFalse(turkish.isEmpty(), key.path() + " is empty");
            assertNotEquals(english.get(key), turkish, key.path() + " is not translated");
            assertEquals(english.get(key).stream().map(Notification::channel).toList(),
                    turkish.stream().map(Notification::channel).toList(), key.path() + " uses different channels");
        }
    }
}
