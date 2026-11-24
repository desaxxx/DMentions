package dev.dash.dmentions.mention.type;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.mention.MentionCandidate;
import dev.dash.dmentions.mention.MentionType;
import dev.dash.dmentions.mention.SoundSpec;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.user.UserData;
import dev.dash.dmentions.user.UserService;
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
    private final UserService userService;

    public PlayerMentionType(XConfig config, UserService userService) {
        this.config = config;
        this.userService = userService;
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
        try {
            UUID userId = UUID.fromString(candidate.targetId());
            Optional<String> customName = userService.customName(userId);
            if (customName.isPresent()) {
                return MINI_MESSAGE.deserialize(config.get(ConfigKeys.PLAYER_CUSTOM_DISPLAY), Placeholder.unparsed("custom_name", customName.get()));
            }
        } catch (IllegalArgumentException ignored) {
        }
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
    public Optional<Player> singleTarget(MentionCandidate candidate) {
        try {
            UUID playerId = UUID.fromString(candidate.targetId());
            return Optional.ofNullable(Bukkit.getPlayer(playerId));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
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
