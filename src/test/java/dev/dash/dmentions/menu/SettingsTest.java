package dev.dash.dmentions.menu;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Durations typed into the cooldown prompt and shown in the menus. */
class SettingsTest {

    @Test
    void typedDurationsAreRead() {
        assertEquals(Duration.ofSeconds(605), Settings.parseDuration("10m 5s"));
        assertEquals(Duration.ofSeconds(605), Settings.parseDuration("10m5s"));
        assertEquals(Duration.ofMinutes(90), Settings.parseDuration(" 1H 30M "));
        assertEquals(Duration.ofSeconds(30), Settings.parseDuration("30"), "a bare number is seconds");
        assertEquals(Duration.ZERO, Settings.parseDuration("0s"));
    }

    @Test
    void invalidOrNegativeDurationsAreRefused() {
        assertNull(Settings.parseDuration(""));
        assertNull(Settings.parseDuration("ten minutes"));
        assertNull(Settings.parseDuration("10x"));
        assertNull(Settings.parseDuration("-5s"));
    }

    @Test
    void durationsAreShownWithSpaces() {
        assertEquals("1h 30m 5s", Settings.formatDuration(Duration.ofSeconds(5405)));
        assertEquals("5s", Settings.formatDuration(Duration.ofSeconds(5)));
        assertEquals("0s", Settings.formatDuration(Duration.ZERO));
    }
}
