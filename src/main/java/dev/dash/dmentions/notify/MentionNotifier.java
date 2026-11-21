package dev.dash.dmentions.notify;

import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.mention.MentionType;
import dev.dash.dmentions.mention.PendingMention;
import dev.dash.dmentions.message.MessageService;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class MentionNotifier {

    private final JavaPlugin plugin;
    private final MessageService messageService;

    public MentionNotifier(JavaPlugin plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
    }

    public void notify(PendingMention mention) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player sender = mention.sender();
            MentionType type = mention.candidate().type();
            Optional<Sound> sound = type.sound();
            var targetMessage = type.targetMessage();
            TagResolver senderTag = Placeholder.unparsed("sender", sender.getName());

            boolean reachedAny = false;
            for (Player target : type.resolveTargets(sender, mention.candidate())) {
                if (target.equals(sender) || !target.isOnline()) continue;

                reachedAny = true;
                sound.ifPresent(target::playSound);
                messageService.send(target, targetMessage, senderTag);
            }

            if (sender.isOnline() && reachedAny) {
                messageService.send(sender, type.senderMessage(), type.senderPlaceholders(mention.candidate()));
            }
        });
    }

    public void cooldownWarn(Player sender, long remainingMs) {
        messageService.send(sender, LangKeys.COOLDOWN_WARN, Placeholder.unparsed("remaining", formatTime(remainingMs)));
    }

    public void disabledWorldWarn(Player sender, String world) {
        messageService.send(sender, LangKeys.DISABLED_WORLD_WARN, Placeholder.unparsed("world", world));
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
