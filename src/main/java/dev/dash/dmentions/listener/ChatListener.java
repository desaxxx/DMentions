package dev.dash.dmentions.listener;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.*;
import dev.dash.dmentions.notify.MentionNotifier;
import dev.dash.dmentions.text.Replacement;
import dev.dash.dmentions.text.StyledText;
import dev.yoshi.xconfig.XConfig;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.*;

public final class ChatListener implements Listener {

    private final XConfig config;
    private final CooldownTracker cooldownTracker;
    private final PendingMentions pendingMentions;
    private final MentionNotifier mentionNotifier;
    private final MentionRegistry mentionRegistry;

    public ChatListener(XConfig config, CooldownTracker cooldownTracker, PendingMentions pendingMentions, MentionNotifier mentionNotifier, MentionRegistry mentionRegistry) {
        this.config = config;
        this.cooldownTracker = cooldownTracker;
        this.pendingMentions = pendingMentions;
        this.mentionNotifier = mentionNotifier;
        this.mentionRegistry = mentionRegistry;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Candidates candidates = mentionRegistry.candidatesFor(sender);
        if (candidates.allowed().isEmpty() && candidates.blocked().isEmpty()) return;

        int limit = config.get(ConfigKeys.MENTION_LIMIT);
        boolean caseSensitive = config.get(ConfigKeys.CASE_SENSITIVE);

        StyledText styled = StyledText.of(event.message());
        List<KeywordMatch> matches = KeywordFinder.find(styled.plain(), candidates.allowed().keySet(), caseSensitive, limit);
        List<KeywordMatch> blockedMatches = KeywordFinder.find(styled.plain(), candidates.blocked().keySet(), caseSensitive, Integer.MAX_VALUE);
        if (matches.isEmpty() && blockedMatches.isEmpty()) return;

        if (config.get(ConfigKeys.DISABLED_WORLDS).contains(sender.getWorld().getName())) {
            mentionNotifier.disabledWorldWarn(sender, sender.getWorld().getName());
            return;
        }

        Set<BlockReason> warned = new HashSet<>();
        for (KeywordMatch bm : blockedMatches) {
            BlockReason blockReason = candidates.blocked().get(bm.keyword());
            if (!warned.add(blockReason)) continue;

            switch (blockReason) {
                case AFK -> mentionNotifier.afkWarn(sender);
                case RESTRICTED -> mentionNotifier.restrictedWarn(sender);
            }
        }

        List<PendingMention> pending = new ArrayList<>();
        List<Replacement> replacements = new ArrayList<>();
        for (KeywordMatch m : matches) {
            MentionCandidate candidate = candidates.allowed().get(m.keyword());

            long remainingCooldown = cooldownTracker.remaining(sender.getUniqueId(), candidate.type().id(), candidate.targetId(), System.currentTimeMillis());
            if (!sender.hasPermission("dmentions.cooldown.bypass") && remainingCooldown > 0) {
                mentionNotifier.cooldownWarn(sender, remainingCooldown);
                continue;
            }

            Component display = candidate.type().display(candidate);
            pending.add(new PendingMention(sender, candidate));
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

        long now = System.currentTimeMillis();
        for (PendingMention mention : pending) {
            Duration cooldownDuration = mention.candidate().type().cooldown(mention.candidate());
            cooldownTracker.start(mention.sender().getUniqueId(), mention.candidate().type().id(), mention.candidate().targetId(), cooldownDuration, now);
        }
        mentionNotifier.notify(event.getPlayer(), pending);
    }
}
