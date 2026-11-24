package dev.dash.dmentions.mention;

import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Introduce each {@link MentionType} to {@link MentionRegistry} */
public interface MentionType {

    String id();

    boolean enabled();

    default boolean respectsToggle() {
        return true;
    }

    List<MentionCandidate> candidates();

    Component display(MentionCandidate candidate);

    String permission();

    default String permission(MentionCandidate candidate) {
        return permission();
    }

    PermissionDefault permissionDefault();

    Duration cooldown();

    default Duration cooldown(MentionCandidate candidate) {
        return cooldown();
    }

    Optional<Sound> sound();

    default Optional<Sound> sound(MentionCandidate candidate) {
        return sound();
    }

    Collection<Player> resolveTargets(Player sender, MentionCandidate candidate);

    default Optional<Player> singleTarget(MentionCandidate candidate) {
        return Optional.empty();
    }

    ConfigKey<List<Notification>> senderMessage();

    ConfigKey<List<Notification>> targetMessage();

    TagResolver senderPlaceholders(MentionCandidate candidate);

    TagResolver targetPlaceholders(MentionCandidate candidate);
}
