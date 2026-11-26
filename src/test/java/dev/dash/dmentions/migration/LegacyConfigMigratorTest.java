package dev.dash.dmentions.migration;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.type.GroupSettings;
import dev.yoshi.xconfig.WritableXConfig;
import dev.yoshi.xconfig.XConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyConfigMigratorTest {

    @TempDir
    Path dir;

    private final List<String> warnings = new ArrayList<>();
    private Logger logger;
    private WritableXConfig config;

    @BeforeEach
    void setUp() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        config = XConfig.builder(dir.resolve("config.yml")).keys(ConfigKeys.class).logger(logger).buildWritable();
    }

    private static YamlConfiguration shipped() throws IOException, InvalidConfigurationException {
        try (InputStream in = LegacyConfigMigratorTest.class.getResourceAsStream("/legacy-1.8.4/config.yml")) {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            return yaml;
        }
    }

    private static YamlConfiguration yaml(String text) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(text);
        return yaml;
    }

    @Test
    void shippedDefaultsBecomeTheSame2Point0Values() throws Exception {
        LegacyConfigMigrator.migrate(shipped(), config, logger);

        assertEquals("<dark_gray>[</dark_gray><#FFD16D>DMentions</#FFD16D><dark_gray>]", config.get(ConfigKeys.PREFIX));
        assertEquals("<#A9E871>@<player>", config.get(ConfigKeys.PLAYER_DISPLAY));
        assertEquals("<#EAC773>@<custom_name>", config.get(ConfigKeys.PLAYER_CUSTOM_DISPLAY));
        assertEquals("<#8FB56C>@everyone", config.get(ConfigKeys.EVERYONE_DISPLAY));
        assertEquals("@nearby", config.get(ConfigKeys.NEARBY_KEYWORD));
        assertEquals(20, config.get(ConfigKeys.NEARBY_RADIUS));
        assertEquals(2, config.get(ConfigKeys.MENTION_LIMIT));
        assertEquals(List.of("no_mention_world_1", "no_mention_world_2"), config.get(ConfigKeys.DISABLED_WORLDS));
        assertEquals("block.note_block.pling", config.get(ConfigKeys.PLAYER_SOUND));
        assertEquals(Duration.ofSeconds(10), config.get(ConfigKeys.EVERYONE_COOLDOWN));
        assertTrue(warnings.isEmpty(), "nothing in the shipped file should fail: " + warnings);
    }

    @Test
    void groupPlaceholdersAreRenamedInKeywordAndPermission() throws Exception {
        LegacyConfigMigrator.migrate(shipped(), config, logger);

        assertEquals("@<group>", config.get(ConfigKeys.GROUP_KEYWORD));
        assertEquals("dmentions.mention.group.<group>", config.get(ConfigKeys.GROUP_PERMISSION));
        assertEquals(List.of("admin", "owner"), config.get(ConfigKeys.GROUP_DISABLED_GROUPS));
    }

    @Test
    void otherGroupsBecomeTheGroupWideSettingsAndTheRestStayOverrides() throws Exception {
        LegacyConfigMigrator.migrate(shipped(), config, logger);

        assertEquals("<#73C7DC>@<group>", config.get(ConfigKeys.GROUP_DISPLAY));
        assertEquals("item.totem.use", config.get(ConfigKeys.GROUP_SOUND));
        assertEquals(Duration.ofSeconds(5), config.get(ConfigKeys.GROUP_COOLDOWN));
        assertEquals(Map.of("default", new GroupSettings("<#C0DEE6>@<group>", "item.totem.use", Duration.ofSeconds(5))),
                config.get(ConfigKeys.GROUP_LIST));
    }

    @Test
    void cooldownsAreWrittenAsDurations() throws Exception {
        LegacyConfigMigrator.migrate(yaml("player:\n  cooldown: 90\n"), config, logger);

        assertTrue(Files.readString(dir.resolve("config.yml")).contains("cooldown: 1m30s"));
    }

    @Test
    void anUnusableValueKeepsTheDefaultAndIsReported() throws Exception {
        LegacyConfigMigrator.migrate(yaml("mention_limit: -3\nnearby:\n  radius: far\n"), config, logger);

        assertEquals(ConfigKeys.MENTION_LIMIT.defaultValue(), config.get(ConfigKeys.MENTION_LIMIT));
        assertEquals(ConfigKeys.NEARBY_RADIUS.defaultValue(), config.get(ConfigKeys.NEARBY_RADIUS));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("'mention_limit'")), warnings.toString());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("'nearby.radius'")), warnings.toString());
    }

    // 1.8 told admins to leave a value empty (or 0) to turn it off
    @Test
    void emptyValuesStayEmpty() throws Exception {
        LegacyConfigMigrator.migrate(yaml("""
                player:
                  sound: ""
                  cooldown: 0
                group:
                  list:
                    vip:
                      sound: ""
                """), config, logger);

        assertEquals("", config.get(ConfigKeys.PLAYER_SOUND));
        assertTrue(dev.dash.dmentions.mention.SoundSpec.parse(config.get(ConfigKeys.PLAYER_SOUND)).isEmpty(), "no sound in 2.0 either");
        assertEquals(Duration.ZERO, config.get(ConfigKeys.PLAYER_COOLDOWN));
        assertEquals(Map.of("vip", new GroupSettings(null, "", null)), config.get(ConfigKeys.GROUP_LIST));
        assertTrue(warnings.isEmpty(), warnings.toString());
    }

    @Test
    void anEmptyKeyword2Point0RejectsKeepsTheDefault() throws Exception {
        LegacyConfigMigrator.migrate(yaml("everyone:\n  keyword: \"\"\n"), config, logger);

        assertEquals(ConfigKeys.EVERYONE_KEYWORD.defaultValue(), config.get(ConfigKeys.EVERYONE_KEYWORD));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("'everyone.keyword'")), warnings.toString());
    }

    @Test
    void keysMissingFromTheOldFileKeepTheirDefaults() throws Exception {
        LegacyConfigMigrator.migrate(yaml("mention_limit: 4\n"), config, logger);

        assertEquals(4, config.get(ConfigKeys.MENTION_LIMIT));
        assertEquals(ConfigKeys.PLAYER_DISPLAY.defaultValue(), config.get(ConfigKeys.PLAYER_DISPLAY));
        assertEquals(ConfigKeys.GROUP_LIST.defaultValue(), config.get(ConfigKeys.GROUP_LIST));
    }

    @Test
    void droppedKeysAreNotCarriedOver() throws Exception {
        LegacyConfigMigrator.migrate(shipped(), config, logger);

        String file = Files.readString(dir.resolve("config.yml"));
        assertFalse(file.contains("suffix_color"), file);
        assertFalse(file.contains("vanish_provider"), file);
        assertFalse(file.contains("config_version"), file);
        assertFalse(file.contains("customized_display"), file);
    }
}
