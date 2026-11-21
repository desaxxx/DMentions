package dev.dash.dmentions.notify;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.mention.PendingMention;
import dev.dash.dmentions.mention.SoundSpec;
import dev.dash.dmentions.message.MessageService;
import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class MentionNotifier {

    private final JavaPlugin plugin;
    private final XConfig config;
    private final MessageService messageService;

    public MentionNotifier(JavaPlugin plugin, XConfig config, MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
    }

    public void notify(PendingMention mention) {
        if (mention.sender().equals(mention.target())) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = mention.target();
            if (!target.isOnline()) return;

            Optional<Sound> sound = SoundSpec.parse(config.get(ConfigKeys.PLAYER_SOUND));
            sound.ifPresent(target::playSound);

            Player sender = mention.sender();

            messageService.send(target, LangKeys.PLAYER_TARGET_MESSAGE, Placeholder.unparsed("sender", sender.getName()));

            if (sender.isOnline()) {
                messageService.send(sender, LangKeys.PLAYER_SENDER_MESSAGE, Placeholder.unparsed("target", target.getName()));
            }
        });
    }

    public void cooldownWarn(Player sender, long remainingMs) {
        messageService.send(sender, LangKeys.COOLDOWN_WARN, Placeholder.unparsed("remaining", formatTime(remainingMs)));
    }

    /**
     * Formats a remaining duration as hours, minutes and seconds, e.g. {@code "45s"},
     * {@code "10m"}, {@code "1h 5m 30s"}. Zero units are omitted and partial seconds
     * are rounded up, so a cooldown that is still running never shows as {@code "0s"}.
     */
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
