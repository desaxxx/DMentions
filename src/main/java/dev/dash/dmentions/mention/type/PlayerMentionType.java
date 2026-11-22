package dev.dash.dmentions.mention.type;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.mention.MentionCandidate;
import dev.dash.dmentions.mention.MentionType;
import dev.dash.dmentions.mention.SoundSpec;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.XConfig;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.time.Duration;
import java.util.*;

public final class PlayerMentionType implements MentionType {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final String ID = "player";

    private final XConfig config;

    public PlayerMentionType(XConfig config) {
        this.config = config;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean enabled() {
        return config.find(ConfigKeys.PLAYER_ENABLED).orElse(false);
    }

    @Override
    public List<MentionCandidate> candidates() {
        List<MentionCandidate> result = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            String keyword = config.get(ConfigKeys.PLAYER_KEYWORD).replace("<player>", target.getName());
            MentionCandidate candidate = new MentionCandidate(keyword, this, target.getUniqueId().toString(), target.getName());
            result.add(candidate);
        }
        return List.copyOf(result);
    }

    @Override
    public Component display(MentionCandidate candidate) {
        return MINI_MESSAGE.deserialize(config.get(ConfigKeys.PLAYER_DISPLAY), Placeholder.unparsed("player", candidate.label()));
    }

    @Override
    public String permission() {
        return config.find(ConfigKeys.PLAYER_PERMISSION).orElse("");
    }

    @Override
    public PermissionDefault permissionDefault() {
        return PermissionDefault.TRUE;
    }

    @Override
    public Duration cooldown() {
        return config.find(ConfigKeys.PLAYER_COOLDOWN).orElse(Duration.ZERO);
    }

    @Override
    public Optional<Sound> sound() {
        return SoundSpec.parse(config.find(ConfigKeys.PLAYER_SOUND).orElse(null));
    }

    @Override
    public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) {
        try {
            Player target = Bukkit.getPlayer(UUID.fromString(candidate.targetId()));
            if (target != null && target.isOnline()) return List.of(target);
        } catch (IllegalArgumentException ignored) {
        }
        return List.of();
    }

    @Override
    public ConfigKey<List<Notification>> senderMessage() {
        return LangKeys.PLAYER_SENDER_MESSAGE;
    }

    @Override
    public ConfigKey<List<Notification>> targetMessage() {
        return LangKeys.PLAYER_TARGET_MESSAGE;
    }

    @Override
    public TagResolver senderPlaceholders(MentionCandidate candidate) {
        return Placeholder.unparsed("target", candidate.label());
    }

    @Override
    public TagResolver targetPlaceholders(MentionCandidate candidate) {
        return TagResolver.empty();
    }
}
