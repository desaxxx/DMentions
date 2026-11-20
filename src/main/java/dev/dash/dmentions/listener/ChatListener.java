package dev.dash.dmentions.listener;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.*;
import dev.dash.dmentions.notify.MentionNotifier;
import dev.dash.dmentions.text.Replacement;
import dev.dash.dmentions.text.StyledText;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChatListener implements Listener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final XConfig config;
    private final CooldownTracker cooldownTracker;
    private final PendingMentions pendingMentions;
    private final MentionNotifier mentionNotifier;

    public ChatListener(XConfig config, CooldownTracker cooldownTracker, PendingMentions pendingMentions, MentionNotifier mentionNotifier) {
        this.config = config;
        this.cooldownTracker = cooldownTracker;
        this.pendingMentions = pendingMentions;
        this.mentionNotifier = mentionNotifier;
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

        Player sender = event.getPlayer();
        List<PendingMention> pending = new ArrayList<>();
        List<Replacement> replacements = new ArrayList<>();
        for (KeywordMatch m : matches) {
            Player target = playersByKeyword.get(m.keyword());

            long remainingCooldown = cooldownTracker.remaining(sender.getUniqueId(), "player", target.getUniqueId().toString(), System.currentTimeMillis());
            if (!sender.hasPermission("dmentions.cooldown.bypass") && remainingCooldown > 0) {
                mentionNotifier.cooldownWarn(sender, remainingCooldown);
                continue;
            }

            Component display = MINI_MESSAGE.deserialize(displayTemplate, Placeholder.unparsed("player", target.getName()));
            if (!target.equals(sender)) {
                pending.add(new PendingMention(sender, target));
            }
            replacements.add(new Replacement(m.start(), m.end(), display));
        }

        pendingMentions.put(event, pending);
        if (replacements.isEmpty()) return;
        event.message(styled.replace(replacements));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatMonitor(AsyncChatEvent event) {
        List<PendingMention> pending = pendingMentions.take(event); // must run regardless of event cancelation
        if (event.isCancelled() || pending.isEmpty()) return;

        Duration cooldownDuration = config.find(ConfigKeys.PLAYER_COOLDOWN).orElse(Duration.ZERO);
        long now = System.currentTimeMillis();
        for (PendingMention mention : pending) {
            cooldownTracker.start(mention.sender().getUniqueId(), "player", mention.target().getUniqueId().toString(), cooldownDuration, now);
            mentionNotifier.notify(mention);
        }
    }
}
