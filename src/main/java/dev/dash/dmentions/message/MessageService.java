package dev.dash.dmentions.message;

import dev.dash.dmentions.config.ConfigKeys;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;

import java.util.List;

public final class MessageService {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final XConfig config;
    private volatile XConfig lang;

    public MessageService(XConfig config, XConfig lang) {
        this.config = config;
        this.lang = lang;
    }

    /** Swaps the language file in place (reload); messages sent afterwards use the new one. */
    public void setLang(XConfig lang) {
        this.lang = lang;
    }

    public void send(Audience audience, ConfigKey<List<Notification>> key, TagResolver... placeholders) {
        List<Notification> notifications = lang.get(key);
        if (notifications == null) return;

        for (Notification n : notifications) {
            switch (n.channel()) {
                case CHAT -> audience.sendMessage(
                        render(n.text(), TagResolver.resolver(placeholders), prefixResolver())
                );
                case ACTION_BAR -> audience.sendActionBar(
                        render(n.text(), TagResolver.resolver(placeholders), prefixResolver())
                );
                case TITLE -> audience.showTitle(
                        Title.title(render(n.text(), TagResolver.resolver(placeholders), prefixResolver()), render(n.subtitle(), TagResolver.resolver(placeholders), prefixResolver()))
                );
            }
        }
    }

    private TagResolver prefixResolver() {
        return Placeholder.component("prefix", MINI_MESSAGE.deserialize(config.get(ConfigKeys.PREFIX)));
    }

    private Component render(String template, TagResolver... placeholders) {
        return MINI_MESSAGE.deserialize(template, placeholders);
    }
}
