package dev.dash.dmentions.config;

import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigKeysTest {

    @TempDir
    Path dir;

    private static final Logger SILENT = Logger.getLogger("ConfigKeysTest");

    static {
        SILENT.setLevel(Level.OFF);
    }

    private XConfig load() {
        return XConfig.builder(dir.resolve("config.yml"))
                .keys(ConfigKeys.class)
                .logger(SILENT)
                .build();
    }

    private void write(String yaml) throws IOException {
        Files.writeString(dir.resolve("config.yml"), yaml);
    }

    @Test
    void defaults() {
        XConfig config = load();
        assertEquals(2, config.get(ConfigKeys.MENTION_LIMIT));
        assertFalse(config.get(ConfigKeys.CASE_SENSITIVE));
        assertTrue(config.get(ConfigKeys.PLAYER_ENABLED));
        assertEquals("<player>", config.get(ConfigKeys.PLAYER_KEYWORD));
        assertEquals("<#a9e871>@<player>", config.get(ConfigKeys.PLAYER_DISPLAY));
        assertEquals(Duration.ofSeconds(5), config.get(ConfigKeys.PLAYER_COOLDOWN));
        assertEquals("en-US", config.get(ConfigKeys.LANG_FILE));
        assertEquals("<dark_gray>[<#ffd16d>DMentions<dark_gray>]<reset>", config.get(ConfigKeys.PREFIX));
    }

    @Test
    void customCooldownIsRead() throws IOException {
        write("""
                player:
                  cooldown: 30s
                """);
        assertEquals(Duration.ofSeconds(30), load().get(ConfigKeys.PLAYER_COOLDOWN));
    }

    @Test
    void generatedFileUsesExpectedPaths() {
        load();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dir.resolve("config.yml").toFile());
        for (String path : new String[]{"lang_file", "prefix", "mention_limit", "case_sensitive",
                "player.enabled", "player.keyword", "player.display", "player.sound", "player.cooldown"}) {
            assertTrue(yaml.contains(path), "missing path: " + path);
        }
        // Messages live in the language file now.
        for (String path : new String[]{"player.sender_message", "player.target_message", "player.cooldown_warn"}) {
            assertFalse(yaml.contains(path), "message key still in config.yml: " + path);
        }
    }

    @Test
    void userValuesAreRead() throws IOException {
        write("""
                mention_limit: 5
                case_sensitive: true
                player:
                  enabled: false
                  keyword: "@<player>"
                  display: "<red>@<player>"
                """);
        XConfig config = load();
        assertEquals(5, config.get(ConfigKeys.MENTION_LIMIT));
        assertTrue(config.get(ConfigKeys.CASE_SENSITIVE));
        assertFalse(config.get(ConfigKeys.PLAYER_ENABLED));
        assertEquals("@<player>", config.get(ConfigKeys.PLAYER_KEYWORD));
        assertEquals("<red>@<player>", config.get(ConfigKeys.PLAYER_DISPLAY));
    }

    @Test
    void negativeLimitFallsBackToDefault() throws IOException {
        write("mention_limit: -1\n");
        assertEquals(2, load().get(ConfigKeys.MENTION_LIMIT));
    }

    @Test
    void keywordWithoutPlaceholderFallsBackToDefault() throws IOException {
        write("""
                player:
                  keyword: "@Steve"
                """);
        assertEquals("<player>", load().get(ConfigKeys.PLAYER_KEYWORD));
    }

    @Test
    void defaultDisplayRendersWithPlayerName() {
        String template = load().get(ConfigKeys.PLAYER_DISPLAY);
        Component rendered = MiniMessage.miniMessage()
                .deserialize(template, Placeholder.unparsed("player", "Steve"));

        assertEquals("@Steve", PlainTextComponentSerializer.plainText().serialize(rendered));
        assertEquals(TextColor.fromHexString("#a9e871"), rendered.color());
    }
}
