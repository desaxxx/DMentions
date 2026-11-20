package dev.dash.dmentions.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.dash.dmentions.text.ComponentInspector.flatten;
import static dev.dash.dmentions.text.ComponentInspector.plain;
import static dev.dash.dmentions.text.ComponentInspector.styleAt;
import static dev.dash.dmentions.text.ComponentInspector.textLeaves;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyledTextTest {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component MENTION = text("@Steve", NamedTextColor.GREEN);

    private static Component replaceOne(Component input, int start, int end, Component with) {
        return StyledText.of(input).replace(List.of(new Replacement(start, end, with)));
    }

    @Nested
    class Plain {

        @Test
        void simpleText() {
            assertEquals("hello world", StyledText.of(text("hello world")).plain());
        }

        @Test
        void emptyComponent() {
            assertEquals("", StyledText.of(Component.empty()).plain());
        }

        @Test
        void nestedTreeIsConcatenatedDepthFirst() {
            Component tree = text()
                    .append(text("ab", NamedTextColor.RED))
                    .append(text("cd").append(text("ef")))
                    .build();
            assertEquals("abcdef", StyledText.of(tree).plain());
        }

        @Test
        void gradientSplitIntoPerCharComponentsIsReadAsOneWord() {
            Component gradient = MM.deserialize("<gradient:red:blue>Steve</gradient> hi");
            assertEquals("Steve hi", StyledText.of(gradient).plain());
        }

        @Test
        void nonTextComponentIsSingleAtom() {
            Component tree = text("a ").append(translatable("item.minecraft.diamond")).append(text(" b"));
            assertEquals('￼', StyledText.ATOM);
            assertEquals("a ￼ b", StyledText.of(tree).plain());
        }

        @Test
        void childrenOfNonTextComponentAreStillSearchable() {
            Component tree = translatable("item.minecraft.diamond").append(text("Steve"));
            assertEquals("￼Steve", StyledText.of(tree).plain());
        }

        @Test
        void surrogatePairsAreKeptIntact() {
            assertEquals("😀 Steve", StyledText.of(text("😀 Steve")).plain());
        }
    }

    @Nested
    class ReplaceNothing {

        private void assertUnchanged(Component input) {
            Component result = StyledText.of(input).replace(List.of());
            assertEquals(flatten(input), flatten(result));
        }

        @Test
        void plainText() {
            assertUnchanged(text("hello world"));
        }

        @Test
        void gradient() {
            assertUnchanged(MM.deserialize("<gradient:#ff0000:#0000ff>hello Steve world</gradient>"));
        }

        @Test
        void nestedDecorations() {
            assertUnchanged(MM.deserialize("<bold>a <red>b <italic>c</italic></red> d</bold> e"));
        }

        @Test
        void eventsAndInsertion() {
            assertUnchanged(text("click me")
                    .clickEvent(ClickEvent.runCommand("/x"))
                    .hoverEvent(HoverEvent.showText(text("hover")))
                    .insertion("ins"));
        }

        @Test
        void translatableAtom() {
            assertUnchanged(text("I have ", NamedTextColor.GRAY)
                    .append(translatable("item.minecraft.diamond", NamedTextColor.AQUA))
                    .append(text(" now")));
        }

        @Test
        void emptyComponent() {
            assertUnchanged(Component.empty());
        }

        @Test
        void adjacentCharactersWithSameStyleAreMerged() {
            Component input = text()
                    .append(text("a", NamedTextColor.RED))
                    .append(text("b", NamedTextColor.RED))
                    .append(text("c", NamedTextColor.RED))
                    .build();
            assertEquals(List.of("abc"), textLeaves(StyledText.of(input).replace(List.of())));
        }
    }

    @Nested
    class ReplaceMention {

        @Test
        void plainTextIsReplaced() {
            Component result = replaceOne(text("hello Steve world"), 6, 11, MENTION);
            assertEquals("hello @Steve world", plain(result));
        }

        @Test
        void unchangedPartsAreMergedAroundMention() {
            Component result = replaceOne(text("hello Steve world", NamedTextColor.GRAY), 6, 11, MENTION);
            assertEquals(List.of("hello ", "@Steve", " world"), textLeaves(result));
        }

        @Test
        void gradientOutsideMentionKeepsEveryCharacterStyle() {
            Component input = MM.deserialize("<gradient:#ff0000:#0000ff>hello Steve world</gradient>");
            Component result = replaceOne(input, 6, 11, MENTION);

            assertEquals("hello @Steve world", plain(result));
            for (int i = 0; i < 6; i++) {
                assertEquals(styleAt(input, i), styleAt(result, i), "before mention, index " + i);
            }
            for (int i = 0; i < " world".length(); i++) {
                assertEquals(styleAt(input, 11 + i), styleAt(result, 12 + i), "after mention, offset " + i);
            }
            for (int i = 6; i < 12; i++) {
                assertEquals(NamedTextColor.GREEN, styleAt(result, i).color(), "mention, index " + i);
            }
        }

        @Test
        void mentionInsideGradientWordGetsOwnColor() {
            Component input = MM.deserialize("<gradient:red:blue>Steve</gradient>");
            Component result = replaceOne(input, 0, 5, MENTION);
            assertEquals("@Steve", plain(result));
            flatten(result).forEach(u -> assertEquals(NamedTextColor.GREEN, u.style().color()));
        }

        @Test
        void mentionDoesNotInheritSurroundingStyle() {
            Component input = MM.deserialize("<bold><red>hi Steve bye");
            Component result = replaceOne(input, 3, 8, MENTION);

            for (int i = 3; i < 9; i++) {
                assertNotEquals(TextDecoration.State.TRUE, styleAt(result, i).decoration(TextDecoration.BOLD));
                assertEquals(NamedTextColor.GREEN, styleAt(result, i).color());
            }
            for (int i = 9; i < 13; i++) {
                assertEquals(TextDecoration.State.TRUE, styleAt(result, i).decoration(TextDecoration.BOLD));
                assertEquals(NamedTextColor.RED, styleAt(result, i).color());
            }
        }

        @Test
        void rootOfResultHasNoStyle() {
            Component input = text("hi Steve", NamedTextColor.RED, TextDecoration.BOLD);
            assertTrue(replaceOne(input, 3, 8, MENTION).style().isEmpty());
        }

        @Test
        void clickHoverAndInsertionArePreservedOutsideMention() {
            ClickEvent click = ClickEvent.runCommand("/x");
            HoverEvent<Component> hover = HoverEvent.showText(text("hover"));
            Component input = text("click Steve").clickEvent(click).hoverEvent(hover).insertion("ins");
            Component result = replaceOne(input, 6, 11, MENTION);

            for (int i = 0; i < 6; i++) {
                assertEquals(click, styleAt(result, i).clickEvent());
                assertEquals(hover, styleAt(result, i).hoverEvent());
                assertEquals("ins", styleAt(result, i).insertion());
            }
            for (int i = 6; i < 12; i++) {
                assertNull(styleAt(result, i).clickEvent());
                assertNull(styleAt(result, i).hoverEvent());
                assertNull(styleAt(result, i).insertion());
            }
        }

        @Test
        void replacementComponentIsInsertedAsIs() {
            Component fancy = MM.deserialize("<hover:show_text:'Steve'><gradient:gold:yellow>@Steve</gradient></hover>");
            Component result = replaceOne(text("hi Steve!"), 3, 8, fancy);

            assertEquals("hi @Steve!", plain(result));
            List<ComponentInspector.Unit> expected = flatten(fancy);
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).style(), styleAt(result, 3 + i));
            }
        }

        @Test
        void wholeMessageReplaced() {
            assertEquals("@Steve", plain(replaceOne(text("Steve"), 0, 5, MENTION)));
        }

        @Test
        void multipleReplacements() {
            Component result = StyledText.of(text("Alex and Steve", NamedTextColor.GRAY)).replace(List.of(
                    new Replacement(0, 4, text("@Alex", NamedTextColor.GREEN)),
                    new Replacement(9, 14, MENTION)));

            assertEquals("@Alex and @Steve", plain(result));
            assertEquals(NamedTextColor.GRAY, styleAt(result, 6).color());
            assertEquals(NamedTextColor.GREEN, styleAt(result, 0).color());
            assertEquals(NamedTextColor.GREEN, styleAt(result, 10).color());
        }

        @Test
        void atomIsKeptWithItsStyle() {
            Component input = text("I have ", NamedTextColor.GRAY)
                    .append(translatable("item.minecraft.diamond", NamedTextColor.AQUA))
                    .append(text(" Steve"));
            Component result = replaceOne(input, 9, 14, MENTION);

            assertEquals("I have ￼ @Steve", plain(result));
            ComponentInspector.Unit atom = flatten(result).get(7);
            assertAll(
                    () -> assertTrue(atom.isAtom()),
                    () -> assertEquals("item.minecraft.diamond", ((TranslatableComponent) atom.atom()).key()),
                    () -> assertEquals(NamedTextColor.AQUA, atom.style().color()));
        }

        @Test
        void textInsideChildrenOfAtomCanBeReplaced() {
            Component input = translatable("item.minecraft.diamond", NamedTextColor.AQUA).append(text(" Steve"));
            Component result = replaceOne(input, 2, 7, MENTION);

            assertEquals("￼ @Steve", plain(result));
            assertEquals(NamedTextColor.AQUA, styleAt(result, 0).color());
            assertEquals(NamedTextColor.AQUA, styleAt(result, 1).color());
            assertEquals(NamedTextColor.GREEN, styleAt(result, 2).color());
        }

        @Test
        void surrogatePairsAroundMentionSurvive() {
            Component input = MM.deserialize("<gradient:red:blue>😀 Steve 😀</gradient>");
            Component result = replaceOne(input, 3, 8, MENTION);
            assertEquals("😀 @Steve 😀", plain(result));
        }
    }

    @Nested
    class InvalidReplacements {

        private final StyledText text = StyledText.of(text("hello Steve world"));

        @Test
        void overlapping() {
            assertThrows(IllegalArgumentException.class, () -> text.replace(List.of(
                    new Replacement(0, 5, MENTION),
                    new Replacement(4, 8, MENTION))));
        }

        @Test
        void unsorted() {
            assertThrows(IllegalArgumentException.class, () -> text.replace(List.of(
                    new Replacement(6, 11, MENTION),
                    new Replacement(0, 5, MENTION))));
        }

        @Test
        void outOfBounds() {
            assertThrows(IllegalArgumentException.class,
                    () -> text.replace(List.of(new Replacement(12, 40, MENTION))));
        }

        @Test
        void negativeStart() {
            assertThrows(IllegalArgumentException.class,
                    () -> text.replace(List.of(new Replacement(-1, 3, MENTION))));
        }

        @Test
        void emptyRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> text.replace(List.of(new Replacement(3, 3, MENTION))));
        }
    }
}
