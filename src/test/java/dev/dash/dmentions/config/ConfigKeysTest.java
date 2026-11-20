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
    }

    @Test
    void generatedFileUsesExpectedPaths() {
        load();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dir.resolve("config.yml").toFile());
        for (String path : new String[]{"mention_limit", "case_sensitive",
                "player.enabled", "player.keyword", "player.display"}) {
            assertTrue(yaml.contains(path), "missing path: " + path);
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
