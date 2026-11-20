package dev.dash.dmentions.mention;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class KeywordFinder {

    private KeywordFinder() {
    }

    public static List<KeywordMatch> find(String text, Collection<String> keywords, boolean caseSensitive, int limit) {
        if (limit <= 0) return List.of();

        List<String> sorted = keywords.stream().filter(s -> !s.isEmpty()).sorted(Comparator.comparingInt(String::length).reversed()).toList();
        List<KeywordMatch> result = new ArrayList<>();
        int i = 0;
        while (i < text.length() && result.size() < limit) {
            if (i != 0 && !Character.isWhitespace(text.charAt(i-1))) {
                i++;
                continue;
            }
            String found = null;
            for (String keyword : sorted) {
                if (text.regionMatches(!caseSensitive, i, keyword, 0, keyword.length())
                        && isEndBoundary(text, i + keyword.length())) {
                    found = keyword;
                    break;
                }
            }
            if (found != null) {
                result.add(new KeywordMatch(i, i + found.length(), found));
                i += found.length();
            } else {
                i++;
            }
        }
        return result;
    }

    private static boolean isEndBoundary(String text, int end) {
        if (end == text.length()) return true;
        char c = text.charAt(end);
        if (c == '_') return false;
        if (Character.isWhitespace(c)) return true;
        int type = Character.getType(c);
        return switch (type) {
            case Character.CONNECTOR_PUNCTUATION, Character.DASH_PUNCTUATION, Character.START_PUNCTUATION,
                 Character.END_PUNCTUATION, Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
                 Character.OTHER_PUNCTUATION -> true;
            default -> false;
        };
    }
}
