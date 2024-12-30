package org.nandayo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {

    private static final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    /**
     * Compiles or retrieves a cached pattern for the given regex.
     *
     * @param regex the regular expression
     * @return a compiled Pattern instance
     */
    public static Pattern getPattern(String regex) {
        return patternCache.computeIfAbsent(regex, Pattern::compile);
    }

    /**
     * Checks if a given input matches the regex.
     *
     * @param input the string to match
     * @param regex the regex pattern
     * @return true if the input matches, false otherwise
     */
    public static boolean matches(String input, String regex) {
        return getPattern(regex).matcher(input).matches();
    }

    /**
     * Finds all matches of the given regex in the input string.
     *
     * @param input the string to search
     * @param regex the regex pattern
     * @return a list of all matched strings
     */
    public static List<String> findAllMatches(String input, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = getPattern(regex).matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    /**
     * Replaces all matches of the regex in the input with the replacement string.
     *
     * @param input       the string to process
     * @param regex       the regex pattern
     * @param replacement the replacement string
     * @return the resulting string
     */
    public static String replaceAll(String input, String regex, String replacement) {
        return getPattern(regex).matcher(input).replaceAll(replacement);
    }

    /**
     * Clears the pattern cache (useful during runtime changes like config reloads).
     */
    public static void clearCache() {
        patternCache.clear();
    }

}
