package dev.dash.dmentions.mention;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeywordFinderTest {

    private static final List<String> STEVE = List.of("Steve");

    private static List<KeywordMatch> find(String text, Collection<String> keywords) {
        return KeywordFinder.find(text, keywords, true, 10);
    }

    @Nested
    class Boundaries {

        @Test
        void keywordInMiddle() {
            assertEquals(List.of(new KeywordMatch(3, 8, "Steve")), find("hi Steve bye", STEVE));
        }

        @Test
        void wholeText() {
            assertEquals(List.of(new KeywordMatch(0, 5, "Steve")), find("Steve", STEVE));
        }

        @ParameterizedTest
        @ValueSource(strings = {"hi\tSteve", "hi\nSteve", "hi  Steve"})
        void anyWhitespaceBefore(String text) {
            assertEquals(1, find(text, STEVE).size());
        }

        @ParameterizedTest
        @ValueSource(strings = {"xSteve", "@Steve", "1Steve", "-Steve", "xxSteve", "hi xxSteve", "a.bSteve"})
        void nonWhitespaceBeforeIsRejected(String text) {
            assertTrue(find(text, STEVE).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Steve,", "Steve!", "Steve?", "Steve.", "Steve'", "Steve:", "Steve)"})
        void punctuationAfterIsAccepted(String text) {
            assertEquals(List.of(new KeywordMatch(0, 5, "Steve")), find(text, STEVE));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Steves", "Steve2", "SteveX"})
        void letterOrDigitAfterIsRejected(String text) {
            assertTrue(find(text, STEVE).isEmpty());
        }

        @Test
        void underscoreAfterIsRejected() {
            // 1.8.4 bug: '_' is \p{P}, so "Steve_2" mentioned "Steve".
            assertTrue(find("Steve_2", STEVE).isEmpty());
        }

        @Test
        void prefixedKeyword() {
            assertEquals(List.of(new KeywordMatch(3, 9, "@Steve")), find("hi @Steve", List.of("@Steve")));
        }

        @Test
        void prefixedKeywordNeedsPrefix() {
            assertTrue(find("hi Steve", List.of("@Steve")).isEmpty());
        }
    }

    @Nested
    class Selection {

        @Test
        void longerNameWithUnderscoreWins() {
            assertEquals(List.of(new KeywordMatch(0, 7, "Steve_2")), find("Steve_2", List.of("Steve", "Steve_2")));
        }

        @Test
        void longestKeywordAtSamePositionWins() {
            assertEquals(List.of(new KeywordMatch(0, 7, "Steve!!")),
                    find("Steve!! hey", List.of("Steve", "Steve!!")));
        }

        @Test
        void matchesAreSortedLeftToRight() {
            assertEquals(List.of(
                            new KeywordMatch(0, 4, "Alex"),
                            new KeywordMatch(9, 14, "Steve")),
                    find("Alex and Steve", List.of("Steve", "Alex")));
        }

        @Test
        void searchResumesAfterMatchEnd() {
            // "eve" lies inside "Steve" and must not be matched again from within it.
            assertEquals(List.of(new KeywordMatch(0, 5, "Steve")),
                    find("Steve", List.of("Steve", "eve", "teve")));
        }

        @Test
        void sameKeywordMatchedRepeatedly() {
            assertEquals(3, find("Steve Steve Steve", STEVE).size());
        }

        @Test
        void keywordIsMatchedLiterallyNotAsRegex() {
            assertTrue(find("@everyXone", List.of("@every.one")).isEmpty());
            assertEquals(1, find("@every.one", List.of("@every.one")).size());
        }

        @Test
        void emptyKeywordIsIgnored() {
            assertEquals(List.of(new KeywordMatch(3, 8, "Steve")), find("hi Steve", List.of("", "Steve")));
        }

        @Test
        void emptyText() {
            assertTrue(find("", STEVE).isEmpty());
        }

        @Test
        void noKeywords() {
            assertTrue(find("hi Steve", List.of()).isEmpty());
        }
    }

    @Nested
    class Limit {

        @Test
        void stopsAtLimit() {
            assertEquals(List.of(
                            new KeywordMatch(0, 5, "Steve"),
                            new KeywordMatch(6, 11, "Steve")),
                    KeywordFinder.find("Steve Steve Steve", STEVE, true, 2));
        }

        @Test
        void zeroLimitFindsNothing() {
            assertTrue(KeywordFinder.find("Steve", STEVE, true, 0).isEmpty());
        }
    }

    @Nested
    class CaseSensitivity {

        @Test
        void sensitiveRejectsDifferentCase() {
            assertTrue(KeywordFinder.find("hi steve", STEVE, true, 10).isEmpty());
        }

        @Test
        void insensitiveReturnsCanonicalKeyword() {
            assertEquals(List.of(new KeywordMatch(3, 8, "Steve")),
                    KeywordFinder.find("hi sTEVE", STEVE, false, 10));
        }

        @Test
        void insensitivePrefixedKeyword() {
            assertEquals(List.of(new KeywordMatch(0, 9, "@everyone")),
                    KeywordFinder.find("@EveryOne!", List.of("@everyone"), false, 10));
        }
    }
}
