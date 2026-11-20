package dev.dash.dmentions;

import dev.dash.dmentions.mention.KeywordFinder;
import dev.dash.dmentions.mention.KeywordMatch;
import dev.dash.dmentions.text.Replacement;
import dev.dash.dmentions.text.StyledText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproduces how Paper hands a message coloured by a legacy-event plugin
 * (e.g. a per-character pastel rainbow) to {@code AsyncChatEvent}:
 * the legacy string is deserialized with Paper's section serializer
 * (hex colours, "§x§r§r§g§g§b§b" format).
 */
class LegacyChatPipelineTest {

    /** Same configuration as Paper's LegacyComponentSerializerProviderImpl#legacySection. */
    private static final LegacyComponentSerializer PAPER_SECTION = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static String rainbowLegacy(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            int hue = (i * 37) % 360;
            int rgb = java.awt.Color.HSBtoRGB(hue / 360f, 0.35f, 1f) & 0xFFFFFF;
            String hex = String.format("%06x", rgb);
            sb.append('§').append('x');
            for (char h : hex.toCharArray()) {
                sb.append('§').append(h);
            }
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }

    @Test
    void perCharacterLegacyRainbowIsMentionable() {
        String typed = "sdfasdf hallalal yoshii01! dsjklf";
        Component message = PAPER_SECTION.deserialize(rainbowLegacy(typed));

        StyledText styled = StyledText.of(message);
        assertEquals(typed, styled.plain());

        List<KeywordMatch> matches = KeywordFinder.find(styled.plain(), List.of("yoshii01"), false, 2);
        assertEquals(List.of(new KeywordMatch(17, 25, "yoshii01")), matches);

        Component mention = Component.text("@yoshii01", NamedTextColor.GREEN);
        Component result = styled.replace(List.of(new Replacement(17, 25, mention)));
        assertEquals("sdfasdf hallalal @yoshii01! dsjklf",
                PlainTextComponentSerializer.plainText().serialize(result));

        // Round trip through Paper's legacy renderer keeps the mention colour.
        String legacyOut = PAPER_SECTION.serialize(result);
        assertTrue(legacyOut.contains("§a@yoshii01"), "legacy output: " + legacyOut.replace('§', '&'));
    }
}
