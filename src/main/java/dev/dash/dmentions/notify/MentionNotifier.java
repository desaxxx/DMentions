package dev.dash.dmentions.notify;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.mention.PendingMention;
import dev.dash.dmentions.mention.SoundSpec;
import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class MentionNotifier {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final XConfig config;

    public MentionNotifier(JavaPlugin plugin, XConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void notify(PendingMention mention) {
        if (mention.sender().equals(mention.target())) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = mention.target();
            if (!target.isOnline()) return;

            Optional<Sound> sound = SoundSpec.parse(config.get(ConfigKeys.PLAYER_SOUND));
            sound.ifPresent(target::playSound);

            Player sender = mention.sender();

            String targetMessage = config.get(ConfigKeys.PLAYER_TARGET_MESSAGE);
            if (targetMessage != null) {
                target.sendActionBar(MINI_MESSAGE.deserialize(targetMessage, Placeholder.unparsed("sender", sender.getName())));
            }

            String senderMessage = config.get(ConfigKeys.PLAYER_SENDER_MESSAGE);
            if (sender.isOnline() && senderMessage != null) {
                sender.sendActionBar(MINI_MESSAGE.deserialize(senderMessage, Placeholder.unparsed("target", target.getName())));
            }
        });
    }

    public void cooldownWarn(Player sender, long remainingMs) {
        String cooldownMessage = config.get(ConfigKeys.PLAYER_COOLDOWN_WARN);
        if (cooldownMessage != null) {
            sender.sendActionBar(MINI_MESSAGE.deserialize(cooldownMessage, Placeholder.unparsed("remaining", formatTime(remainingMs))));
        }
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
