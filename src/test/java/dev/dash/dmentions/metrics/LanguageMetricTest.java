package dev.dash.dmentions.metrics;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageMetricTest {

    private static final Set<String> BUNDLED = Set.of("en-US", "tr-TR");

    @Test
    void bundledLanguageIsSentByName() {
        assertEquals("tr-TR", LanguageMetric.valueFor("tr-TR", BUNDLED));
        assertEquals("en-US", LanguageMetric.valueFor("en-US", BUNDLED));
    }

    @Test
    void anyOtherLanguageIsSentAsCustom() {
        assertEquals("custom", LanguageMetric.valueFor("my-own-translation", BUNDLED));
        assertEquals("custom", LanguageMetric.valueFor("TR-tr", BUNDLED), "names are compared exactly");
    }
}
