package org.nandayo.dmentions.service.message;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.*;

/**
 * @since 1.8.3
 */
public class Message {
    static private final Map<String, Message> KEY_MAP = new HashMap<>();
    static private final String UNSET_VALUE = "?LM?";

    @Parameterized(params = {"{remained}"})
    public static final Message COOLDOWN_WARN = create("cooldown_warn");
    @Parameterized(params = "{target}")
    public static final Message MENTION_RESTRICTED_WARN = create("mention_restricted_warn");
    @Parameterized(params = "{world}")
    public static final Message DISABLED_WORLD_WARN = create("disabled_world_warn");
    @Parameterized(params = "{target}")
    public static final Message AFK_WARN = create("afk_warn");
    @Parameterized(params = "{target}")
    public static final Message IGNORE_WARN = create("ignore_warn");

    public static final Message VANISH_NOTIFY_X = create("vanish_notify.{x}");


    @Parameterized(params = {"{target}","{sender}"})
    public static final Message PLAYER_SENDER_MESSAGE = create("player.sender_message");
    @Parameterized(params = {"{target}","{sender}"})
    public static final Message PLAYER_TARGET_MESSAGE = create("player.target_message");
    @Parameterized(params = "{sender}")
    public static final Message NEARBY_SENDER_MESSAGE = create("nearby.sender_message");
    @Parameterized(params = "{sender}")
    public static final Message NEARBY_TARGET_MESSAGE = create("nearby.target_message");
    @Parameterized(params = "{sender}")
    public static final Message EVERYONE_SENDER_MESSAGE = create("everyone.sender_message");
    @Parameterized(params = "{sender}")
    public static final Message EVERYONE_TARGET_MESSAGE = create("everyone.target_message");
    @Parameterized(params = {"{sender}", "{group}"})
    public static final Message GROUP_X_SENDER_MESSAGE = create("group.{x}.sender_message");
    @Parameterized(params = {"{sender}","{group}"})
    public static final Message GROUP_X_TARGET_MESSAGE = create("group.{x}.target_message");


    public static final Message COMMAND_MUST_BE_PLAYER = create("command.must_be_player");
    public static final Message COMMAND_PLAYER_NOT_FOUND = create("command.player_not_found");
    public static final Message COMMAND_UNKNOWN = create("command.unknown");
    public static final Message COMMAND_TOGGLE_NO_LONGER_MENTIONED = create("command.toggle.no_longer_mentioned");
    public static final Message COMMAND_TOGGLE_WILL_NOW_MENTIONED = create("command.toggle.will_now_mentioned");
    public static final Message COMMAND_SEND_INVALID_KEYWORD = create("command.send.invalid_keyword");
    public static final Message COMMAND_RELOAD_SUCCESS = create("command.reload.success");
    public static final Message COMMAND_USER_UNKNOWN = create("command.user.unknown");
    @Parameterized(params = {"{target}","{value}"})
    public static final Message COMMAND_USER_MENTIONS_SUCCESS = create("command.user.mentions.success");
    public static final Message COMMAND_USER_DISPLAY_INVALID_DISPLAY = create("command.user.display.invalid_display");
    @Parameterized(params = {"{target}", "{value}"})
    public static final Message COMMAND_USER_DISPLAY_SUCCESS = create("command.user.display.success");
    public static final Message COMMAND_HELP_DESCRIPTION = create("command.help.description");
    @Parameterized(params = "{commands}")
    public static final Message COMMAND_HELP_LIST = create("command.help.list");
    public static final Message COMMAND_CONFIG_ALREADY_CONFIGURING = create("command.config.already_configuring");
    public static final Message COMMAND_CONFIG_RESET_CHANGES = create("command.config.reset_changes");
    public static final Message COMMAND_CONFIG_SAVE_CHANGES = create("command.config.save_changes");
    public static final Message COMMAND_CUSTOMIZE_INVALID_DISPLAY = create("command.customize.invalid_display");
    @Parameterized(params = {"{old_value}", "{value}"})
    public static final Message COMMAND_CUSTOMIZE_SUCCESS = create("command.customize.success");



    @Getter
    private final @NotNull String key;
    private @NotNull Object value = UNSET_VALUE;
    protected Message(@NotNull String key) {
        this.key = key;
    }
    protected Message(@NotNull String key, @NotNull Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get value of the Message.
     * @return Object
     * @since 1.8.3
     */
    public @NotNull Object getValue() {
        if(value.equals(UNSET_VALUE)) {
            value = languageManager.get(key);
        }
        return value;
    }

    /**
     * Create an official {@link Message} object and register it to {@link #KEY_MAP}.
     * @param key Key of the Message
     * @return Message object
     * @since 1.8.3
     */
    protected static Message create(String key) {
        Message message = new Message(key);
        // only the original Messages
        KEY_MAP.put(key, message);
        return message;
    }

    /**
     * Get a new {@link Message} with modified key.
     * @param regex Regex
     * @param replacement Replacement
     * @return new Message
     * @since 1.8.3
     */
    public Message replaceKey(@NotNull String regex, @NotNull String replacement) {
        return new Message(key.replace(regex, replacement));
    }

    /**
     * Get a new {@link Message} with modified value.
     * @param regex Regex
     * @param replacement Replacement
     * @return new Message
     * @since 1.8.3
     */
    public Message replaceValue(@NotNull String regex, @NotNull String replacement) {
        Object value = getValue();
        if(value instanceof String) {
            value = ((String) value).replace(regex, replacement);
        }
        else if(value instanceof List<?>) {
            List<String> list = new ArrayList<>();
            for(Object o: (List<?>) value) {
                if(!(o instanceof String)) continue;
                list.add(((String) o).replace(regex, replacement));
            }
            value = list;
        }
        return new Message(key, value);
    }

    /**
     * Get a copy of {@link #KEY_MAP}.
     * @return Map of Key - {@link Message}
     * @since 1.8.3
     */
    protected static Map<String, Message> getKeyMap() {
        return new HashMap<>(KEY_MAP);
    }



    private static LanguageManager languageManager;

    /**
     * Initializes the official {@link Message}s.
     * @param languageManager LanguageManager
     * @since 1.8.3
     */
    public static void init(@NotNull LanguageManager languageManager) {
        resetValues();
        Message.languageManager = languageManager;
    }

    /**
     * Resets current message values.
     * @since 1.8.3
     */
    private static void resetValues() {
        for(Message message : KEY_MAP.values()) {
            message.value = UNSET_VALUE;
        }
    }


    /**
     * Send this Message to given receiver.<br>
     * <b>NOTE:</b> It will silently fail if the type of value is not {@code String} or {@code List<String>}.
     * @param receiver Receiver
     * @since 1.8.3
     */
    public void sendMessage(@NotNull CommandSender receiver) {
        Object obj = getValue();
        if(obj instanceof String) {
            MessageRouter.sendResolved(receiver, (String) obj);
        }
        else if(obj instanceof List<?>) {
            for(Object o : (List<?>) obj) {
                if(!(o instanceof String)) continue;
                MessageRouter.sendResolved(receiver, (String) o);
            }
        }
    }
}
