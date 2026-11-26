package dev.dash.dmentions.migration;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyText {

    // 1.8 gradient format (from DAPI): <&#RRGGBB>text</&#RRGGBB>, unknown to Adventure
    private static final Pattern GRADIENT = Pattern.compile("<&#([0-9a-fA-F]{6})>(.*?)</&#([0-9a-fA-F]{6})>");

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private LegacyText() {
    }

    public static String convert(String text, Map<String, String> placeholders) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        Matcher matcher = GRADIENT.matcher(text);
        int last = 0;
        while (matcher.find()) {
            result.append(toMiniMessage(text.substring(last, matcher.start())));
            result.append("<gradient:#").append(matcher.group(1)).append(":#").append(matcher.group(3)).append('>')
                    .append(toMiniMessage(matcher.group(2)))
                    .append("</gradient>");
            last = matcher.end();
        }
        result.append(toMiniMessage(text.substring(last)));

        return renamePlaceholders(result.toString(), placeholders);
    }

    // placeholders are renamed only after serializing: a <tag> written before would be escaped as plain text
    private static String toMiniMessage(String legacy) {
        if (legacy.isEmpty()) return legacy;
        return MiniMessage.miniMessage().serialize(LEGACY.deserialize(legacy));
    }

    static String renamePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null) return null;
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", "<" + entry.getValue() + ">");
        }
        return result;
    }
}
