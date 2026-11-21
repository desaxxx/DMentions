package dev.dash.dmentions.mention;

import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MentionType {

    String id();

    boolean enabled();

    List<MentionCandidate> candidates();

    Component display(MentionCandidate candidate);

    Duration cooldown();

    Optional<Sound> sound();

    Collection<Player> resolveTargets(Player sender, MentionCandidate candidate);

    ConfigKey<List<Notification>> senderMessage();

    ConfigKey<List<Notification>> targetMessage();

    TagResolver senderPlaceholders(MentionCandidate candidate);
}
