package dev.dash.dmentions.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangConfigFactoryTest {

    private static final Logger SILENT = Logger.getLogger("LangConfigFactoryTest");

    static {
        SILENT.setLevel(Level.OFF);
    }

    @TempDir
    Path dir;

    private static List<Path> bundledLanguages() throws IOException, URISyntaxException {
        URL folder = LangConfigFactoryTest.class.getClassLoader().getResource("lang");
        assertNotNull(folder, "the jar ships a lang folder");
        try (Stream<Path> files = Files.list(Path.of(folder.toURI()))) {
            return files.filter(path -> path.getFileName().toString().endsWith(".yml")).sorted().toList();
        }
    }

    private List<Path> filesIn(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.toList();
        }
    }

    // a shipped file with an older (or no) config-version would be "migrated" and backed up on every fresh install
    @Test
    void shippedLanguagesOpenWithoutAMigration() throws Exception {
        List<Path> bundled = bundledLanguages();
        assertFalse(bundled.isEmpty());

        for (Path source : bundled) {
            Path folder = Files.createDirectories(dir.resolve(source.getFileName().toString() + "-dir"));
            Path copy = Files.copy(source, folder.resolve(source.getFileName()));

            LangConfigFactory.create(copy).logger(SILENT).build();

            assertTrue(Files.readString(copy).contains("config-version: " + LangConfigFactory.VERSION),
                    source.getFileName() + " must carry config-version " + LangConfigFactory.VERSION);
            assertTrue(filesIn(folder).equals(List.of(copy)),
                    source.getFileName() + " left a migration backup: " + filesIn(folder));
        }
    }

    @Test
    void aGeneratedLanguageOpensWithoutAMigrationAfterwards() throws IOException {
        Path file = dir.resolve("es-ES.yml");

        LangConfigFactory.create(file).logger(SILENT).build();
        LangConfigFactory.create(file).logger(SILENT).build();

        assertTrue(Files.readString(file).contains("config-version: " + LangConfigFactory.VERSION));
        assertTrue(filesIn(dir).equals(List.of(file)), "no backup expected: " + filesIn(dir));
    }
}
