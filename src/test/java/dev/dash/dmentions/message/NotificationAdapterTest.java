package dev.dash.dmentions.message;

import dev.yoshi.xconfig.ConfigException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationAdapterTest {

    private static final NotificationAdapter ADAPTER = NotificationAdapter.INSTANCE;

    private static Notification read(String raw) {
        return ADAPTER.deserialize(raw, "test.path");
    }

    @Nested
    class Channels {

        @Test
        void chat() {
            assertEquals(new Notification(Channel.CHAT, "<red>hi", ""), read("CHAT=<red>hi"));
        }

        @Test
        void actionBar() {
            assertEquals(new Notification(Channel.ACTION_BAR, "<red>hi", ""), read("ACTION_BAR=<red>hi"));
        }

        @Test
        void titleWithSubtitle() {
            assertEquals(new Notification(Channel.TITLE, "<prefix>", "<gray>You were mentioned"),
                    read("TITLE=<prefix>||<gray>You were mentioned"));
        }

        @Test
        void titleWithoutSubtitle() {
            assertEquals(new Notification(Channel.TITLE, "Big", ""), read("TITLE=Big"));
        }

        @Test
        void titleWithOnlySubtitle() {
            assertEquals(new Notification(Channel.TITLE, "", "small"), read("TITLE=||small"));
        }

        @Test
        void titleSplitsOnFirstSeparatorOnly() {
            assertEquals(new Notification(Channel.TITLE, "a", "b||c"), read("TITLE=a||b||c"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"action_bar=hi", "Action_Bar=hi", "ACTION_BAR=hi"})
        void channelNameIsCaseInsensitive(String raw) {
            assertEquals(Channel.ACTION_BAR, read(raw).channel());
        }
    }

    @Nested
    class FallbackToChat {

        @Test
        void textWithoutChannel() {
            assertEquals(new Notification(Channel.CHAT, "<green>plain message", ""), read("<green>plain message"));
        }

        @Test
        void unknownChannelKeepsWholeText() {
            String raw = "<click:open_url:'https://example.com?a=b'>Click";
            assertEquals(new Notification(Channel.CHAT, raw, ""), read(raw));
        }

        @Test
        void equalsSignInsideMessageIsKept() {
            assertEquals(new Notification(Channel.ACTION_BAR, "1+1=2", ""), read("ACTION_BAR=1+1=2"));
        }

        @Test
        void separatorIsNotSplitOutsideTitle() {
            assertEquals(new Notification(Channel.CHAT, "a || b", ""), read("CHAT=a || b"));
            assertEquals(new Notification(Channel.ACTION_BAR, "a||b", ""), read("ACTION_BAR=a||b"));
        }
    }

    @Nested
    class Blank {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "CHAT=", "ACTION_BAR=  ", "TITLE=", "TITLE=||", "TITLE= || "})
        void blankTextIsRejected(String raw) {
            assertThrows(ConfigException.class, () -> read(raw));
        }
    }

    @Nested
    class Serialize {

        @Test
        void writesChannelAndText() {
            assertEquals("ACTION_BAR=<red>hi", ADAPTER.serialize(new Notification(Channel.ACTION_BAR, "<red>hi", "")));
            assertEquals("CHAT=hello", ADAPTER.serialize(new Notification(Channel.CHAT, "hello", "")));
        }

        @Test
        void writesTitleWithSeparator() {
            assertEquals("TITLE=a||b", ADAPTER.serialize(new Notification(Channel.TITLE, "a", "b")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"CHAT=<red>hi", "ACTION_BAR=1+1=2", "TITLE=<prefix>||<gray>sub", "TITLE=only||",
                "TITLE=||sub", "CHAT=<click:open_url:'https://example.com?a=b'>Click"})
        void roundTrip(String raw) {
            Notification once = read(raw);
            assertEquals(once, read((String) ADAPTER.serialize(once)));
        }
    }
}
