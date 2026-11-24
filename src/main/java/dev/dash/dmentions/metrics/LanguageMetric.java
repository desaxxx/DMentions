package dev.dash.dmentions.metrics;

import java.util.Set;

public final class LanguageMetric {

    public static final String CUSTOM = "custom";

    private LanguageMetric() {}

    public static String valueFor(String loadedLanguage, Set<String> bundledLanguages) {
        return bundledLanguages.contains(loadedLanguage) ? loadedLanguage : CUSTOM;
    }
}
