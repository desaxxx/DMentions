package dev.dash.dmentions.update;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionsTest {

    @ParameterizedTest(name = "{0} newer than {1}: {2}")
    @CsvSource({
            "2.0.0, 1.8.4, true",
            "1.8.4, 2.0.0, false",
            "2.0.0, 2.0.0, false",
            "2.0.10, 2.0.9, true",
            "2.0.1, 2.0, true",
            "2.0, 2.0.0, false",
            "2.0.0, 2.0.0-SNAPSHOT, true",
            "2.0.0-SNAPSHOT, 2.0.0, false",
            "2.0.0, 2.1.0-SNAPSHOT, false",
            "v2.0.1, 2.0.0, true",
            "1.8, 1.8.4, false",
    })
    void isNewer(String candidate, String current, boolean expected) {
        assertEquals(expected, Versions.isNewer(candidate, current));
    }
}
