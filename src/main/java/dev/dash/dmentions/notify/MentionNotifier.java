package dev.dash.dmentions.notify;

import com.tcoded.folialib.impl.PlatformScheduler;
import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.mention.MentionType;
import dev.dash.dmentions.mention.PendingMention;
import dev.dash.dmentions.mention.TargetFilter;
import dev.dash.dmentions.message.MessageService;
import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.*;

public final class MentionNotifier {

    private final PlatformScheduler scheduler;
    private final MessageService messageService;
    private final XConfig config;
    private final TargetFilter targetFilter;

    public MentionNotifier(PlatformScheduler scheduler, MessageService messageService, XConfig config, TargetFilter targetFilter) {
        this.scheduler = scheduler;
        this.messageService = messageService;
        this.config = config;
        this.targetFilter = targetFilter;
    }

    public void notify(Player sender, List<PendingMention> mentions) {
        scheduler.runAtEntity(sender, t1 -> {
            boolean allowMultipleNotifications = config.get(ConfigKeys.ALLOW_MULTIPLE_NOTIFICATIONS);
            Set<UUID> notified = new HashSet<>();

            for (PendingMention mention : mentions) {
                MentionType type = mention.candidate().type();
                Optional<Sound> sound = type.sound(mention.candidate());
                var targetMessage = type.targetMessage();
                TagResolver senderTag = Placeholder.unparsed("sender", sender.getName());

                boolean reachedAny = false;
                for (Player target : type.resolveTargets(sender, mention.candidate())) {
                    if (target.equals(sender)) continue;
                    if (!targetFilter.allows(sender, target, type)) continue;
                    if (!allowMultipleNotifications && !notified.add(target.getUniqueId())) continue;

                    reachedAny = true;
                    scheduler.runAtEntity(target, t2 -> {
                        sound.ifPresent(target::playSound);
                        messageService.send(target, targetMessage, TagResolver.resolver(senderTag, type.targetPlaceholders(mention.candidate())));
                    });
                }

                if (sender.isOnline() && reachedAny) {
                    messageService.send(sender, type.senderMessage(), type.senderPlaceholders(mention.candidate()));
                }
            }
        });
    }

    public void cooldownWarn(Player sender, long remainingMs) {
        messageService.send(sender, LangKeys.COOLDOWN_WARN, Placeholder.unparsed("remaining", formatTime(remainingMs)));
    }

    public void disabledWorldWarn(Player sender, String world) {
        messageService.send(sender, LangKeys.DISABLED_WORLD_WARN, Placeholder.unparsed("world", world));
    }

    public void afkWarn(Player sender) {
        messageService.send(sender, LangKeys.AFK_WARN);
    }

    public void restrictedWarn(Player sender) {
        messageService.send(sender, LangKeys.MENTION_RESTRICTED_WARN);
    }

    static String formatTime(long ms) {
        long totalSeconds = Math.max(1, (ms + 999) / 1000);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s ");
        return sb.toString().trim();
    }
}
