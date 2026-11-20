package dev.dash.dmentions.listener;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.KeywordFinder;
import dev.dash.dmentions.mention.KeywordMatch;
import dev.dash.dmentions.text.Replacement;
import dev.dash.dmentions.text.StyledText;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.XConfig;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChatListener implements Listener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final XConfig config;

    public ChatListener(XConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!config.get(ConfigKeys.PLAYER_ENABLED)) return;

        int limit = config.get(ConfigKeys.MENTION_LIMIT);
        boolean caseSensitive = config.get(ConfigKeys.CASE_SENSITIVE);
        String keywordTemplate = config.get(ConfigKeys.PLAYER_KEYWORD);
        String displayTemplate = config.get(ConfigKeys.PLAYER_DISPLAY);

        Map<String, Player> playersByKeyword = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            playersByKeyword.put(keywordTemplate.replace("<player>", p.getName()), p);
        }

        StyledText styled = StyledText.of(event.message());
        List<KeywordMatch> matches = KeywordFinder.find(styled.plain(), playersByKeyword.keySet(), caseSensitive, limit);
        if (matches.isEmpty()) return;

        List<Replacement> replacements = new ArrayList<>();
        for (KeywordMatch m : matches) {
            Player target = playersByKeyword.get(m.keyword());
            Component display = MINI_MESSAGE.deserialize(displayTemplate, Placeholder.unparsed("player", target.getName()));
            replacements.add(new Replacement(m.start(), m.end(), display));
        }

        event.message(styled.replace(replacements));
    }
}
