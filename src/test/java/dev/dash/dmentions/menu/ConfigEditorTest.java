package dev.dash.dmentions.menu;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.type.GroupSettings;
import dev.dash.dmentions.storage.StorageType;
import dev.yoshi.xconfig.WritableXConfig;
import dev.yoshi.xconfig.XConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigEditorTest {

    private static final Logger SILENT = Logger.getLogger("ConfigEditorTest");

    static {
        SILENT.setLevel(Level.OFF);
    }

    @TempDir
    Path dir;

    private WritableXConfig config;
    private int applied;
    private ConfigEditor editor;

    @BeforeEach
    void setUp() {
        config = XConfig.builder(dir.resolve("config.yml")).keys(ConfigKeys.class).logger(SILENT).buildWritable();
        editor = new ConfigEditor(config, () -> {
            config.update(); // what /dms reload does with the config
            applied++;
            return true;
        }, SILENT);
    }

    private String file() throws IOException {
        return Files.readString(dir.resolve("config.yml"));
    }

    @Test
    void valueIsSavedAppliedAndCommentsAreKept() throws IOException {
        assertTrue(editor.set(ConfigKeys.CASE_SENSITIVE, true));

        assertTrue(config.get(ConfigKeys.CASE_SENSITIVE));
        assertEquals(1, applied);
        assertTrue(file().contains("case_sensitive: true"));
        assertTrue(file().contains("# Whether mentions are case sensitive."), "comments survive the write");
        assertFalse(Files.exists(dir.resolve("config.yml.tmp")), "temp file left behind");
    }

    @Test
    void durationsAndEnumsAreWrittenTheWayXConfigReadsThem() throws IOException {
        editor.set(ConfigKeys.PLAYER_COOLDOWN, Duration.ofSeconds(90));
        editor.set(ConfigKeys.STORAGE_TYPE, StorageType.SQLITE);

        assertTrue(file().contains("cooldown: 1m30s"), file());
        assertTrue(file().contains("type: SQLITE"), file());
        assertEquals(Duration.ofSeconds(90), config.get(ConfigKeys.PLAYER_COOLDOWN));
        assertEquals(StorageType.SQLITE, config.get(ConfigKeys.STORAGE_TYPE));
    }

    @Test
    void listsAndElementMapsRoundTrip() {
        editor.set(ConfigKeys.DISABLED_WORLDS, List.of("arena", "lobby"));
        Map<String, GroupSettings> overrides = new LinkedHashMap<>();
        overrides.put("vip", new GroupSettings("<gold>@vip", null, Duration.ofSeconds(3)));
        editor.set(ConfigKeys.GROUP_LIST, overrides);

        assertEquals(List.of("arena", "lobby"), config.get(ConfigKeys.DISABLED_WORLDS));
        assertEquals(Map.of("vip", new GroupSettings("<gold>@vip", null, Duration.ofSeconds(3))),
                config.get(ConfigKeys.GROUP_LIST));
    }

    @Test
    void emptyMapRemovesEveryEntry() {
        editor.set(ConfigKeys.GROUP_LIST, Map.of());

        assertEquals(Map.of(), config.get(ConfigKeys.GROUP_LIST));
    }

    @Test
    void invalidValueIsRefusedWithoutTouchingTheFile() throws IOException {
        String before = file();

        assertFalse(editor.set(ConfigKeys.MENTION_LIMIT, -1));

        assertEquals(before, file());
        assertEquals(0, applied, "nothing to apply");
        assertEquals(2, config.get(ConfigKeys.MENTION_LIMIT));
    }

    @Test
    void failedApplyIsReported() {
        ConfigEditor failing = new ConfigEditor(config, () -> false, SILENT);

        assertFalse(failing.set(ConfigKeys.MENTION_LIMIT, 4));
    }
}
