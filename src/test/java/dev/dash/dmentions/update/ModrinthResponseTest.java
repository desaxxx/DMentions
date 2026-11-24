package dev.dash.dmentions.update;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModrinthResponseTest {

    /** Trimmed from a real answer of /v2/project/dmentions/version (newest first). */
    private static final String REAL_ANSWER = """
            [
              {"id":"a1","project_id":"QBInLkhr","name":"DMentions 1.8.4","version_number":"1.8.4",
               "version_type":"release","loaders":["bukkit","paper","purpur","spigot"],
               "date_published":"2025-10-18T11:54:44.534658Z","files":[]},
              {"id":"a2","project_id":"QBInLkhr","name":"DMentions 1.8.3","version_number":"1.8.3",
               "version_type":"release","loaders":["bukkit","paper","purpur","spigot"],
               "date_published":"2025-09-01T15:49:01.467863Z","files":[]}
            ]
            """;

    @Test
    void newestReleaseIsTheFirstOne() {
        assertEquals(Optional.of("1.8.4"), ModrinthResponse.latestRelease(REAL_ANSWER));
    }

    @Test
    void betaAndAlphaVersionsAreSkipped() {
        String json = """
                [
                  {"version_number":"2.1.0-beta","version_type":"beta"},
                  {"version_number":"2.1.0-alpha","version_type":"alpha"},
                  {"version_number":"2.0.0","version_type":"release"}
                ]
                """;

        assertEquals(Optional.of("2.0.0"), ModrinthResponse.latestRelease(json));
    }

    @Test
    void noReleaseGivesNothing() {
        assertEquals(Optional.empty(), ModrinthResponse.latestRelease("[]"));
        assertEquals(Optional.empty(), ModrinthResponse.latestRelease("[{\"version_number\":\"2.0.0-beta\",\"version_type\":\"beta\"}]"));
    }

    @Test
    void unexpectedAnswersGiveNothing() {
        assertEquals(Optional.empty(), ModrinthResponse.latestRelease("not json"));
        assertEquals(Optional.empty(), ModrinthResponse.latestRelease("{\"error\":\"not_found\"}"));
        assertEquals(Optional.empty(), ModrinthResponse.latestRelease("[{\"version_type\":\"release\"}]"));
    }
}
