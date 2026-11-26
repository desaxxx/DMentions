package dev.dash.dmentions.migration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTextTest {

    private static final Map<String, String> NONE = Map.of();

    private static Component parse(String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static TextColor colorOf(Component root, String text) {
        return colorOf(root, text, null);
    }

    private static TextColor colorOf(Component component, String text, TextColor inherited) {
        TextColor color = component.color() != null ? component.color() : inherited;
        if (component instanceof net.kyori.adventure.text.TextComponent textComponent && textComponent.content().contains(text)) {
            return color;
        }
        for (Component child : component.children()) {
            TextColor found = colorOf(child, text, color);
            if (found != null) return found;
        }
        return null;
    }

    private static boolean isBold(Component component, String text, boolean inherited) {
        TextDecoration.State state = component.decoration(TextDecoration.BOLD);
        boolean bold = state == TextDecoration.State.NOT_SET ? inherited : state == TextDecoration.State.TRUE;
        if (component instanceof net.kyori.adventure.text.TextComponent textComponent && textComponent.content().contains(text)) {
            return bold;
        }
        for (Component child : component.children()) {
            if (isBold(child, text, bold)) return true;
        }
        return false;
    }

    @Test
    void nullAndEmptyStayAsTheyAre() {
        assertNull(LegacyText.convert(null, NONE));
        assertEquals("", LegacyText.convert("", NONE));
    }

    @Test
    void namedAndHexColoursBecomeTags() {
        Component result = parse(LegacyText.convert("&8[&#ffd16dDMentions&8]&r", NONE));

        assertEquals("[DMentions]", plain(result));
        assertEquals(TextColor.fromHexString("#ffd16d"), colorOf(result, "DMentions"));
        assertEquals(NamedTextColor.DARK_GRAY, colorOf(result, "["));
    }

    @Test
    void decorationsAreKept() {
        Component result = parse(LegacyText.convert("&c&lHi", NONE));

        assertTrue(isBold(result, "Hi", false));
        assertEquals(NamedTextColor.RED, colorOf(result, "Hi"));
    }

    @Test
    void repeatedXHexFormatIsRead() {
        Component result = parse(LegacyText.convert("&x&f&f&d&1&6&dGold", NONE));

        assertEquals(TextColor.fromHexString("#ffd16d"), colorOf(result, "Gold"));
    }

    @Test
    void gradientsBecomeGradientTags() {
        String result = LegacyText.convert("&7Hi <&#ff0000>rainbow</&#0000ff> end", NONE);

        assertTrue(result.contains("<gradient:#ff0000:#0000ff>rainbow</gradient>"), result);
        assertEquals("Hi rainbow end", plain(parse(result)));
    }

    @Test
    void placeholdersAreRenamedAndStayUsable() {
        String result = LegacyText.convert("&#a9e871@{p} {unknown}", Map.of("p", "player"));

        assertTrue(result.contains("@<player>"), result);
        assertTrue(result.contains("{unknown}"), "names not in the map are left alone: " + result);
        Component filled = MiniMessage.miniMessage().deserialize(result,
                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("player", "Steve"));
        assertEquals("@Steve {unknown}", plain(filled));
    }

    @Test
    void plainAngleBracketsAreEscapedNotTurnedIntoTags() {
        String result = LegacyText.convert("&ca<b> <3", NONE);

        assertEquals("a<b> <3", plain(parse(result)));
    }

    @Test
    void renamePlaceholdersLeavesColourCodesAlone() {
        assertEquals("dmentions.mention.group.<group>",
                LegacyText.renamePlaceholders("dmentions.mention.group.{group}", Map.of("group", "group")));
        assertEquals("&a@<group>", LegacyText.renamePlaceholders("&a@{group}", Map.of("group", "group")));
    }
}
