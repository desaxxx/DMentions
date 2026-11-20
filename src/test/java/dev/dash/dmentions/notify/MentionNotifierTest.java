package dev.dash.dmentions.notify;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MentionNotifierTest {

    @ParameterizedTest
    @CsvSource({
            "1,         1s",
            "999,       1s",
            "1000,      1s",
            "1001,      2s",
            "45000,     45s",
            "60000,     1m",
            "61000,     1m 1s",
            "600000,    10m",
            "3600000,   1h",
            "3661000,   1h 1m 1s",
            "3630000,   1h 30s",
            "90000000,  25h",
    })
    void formatTime(long ms, String expected) {
        assertEquals(expected, MentionNotifier.formatTime(ms));
    }
}
