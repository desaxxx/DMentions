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
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class EveryoneMentionType implements MentionType {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final String ID = "everyone";

    private final XConfig config;

    public EveryoneMentionType(XConfig config) {
        this.config = config;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean enabled() {
        return config.find(ConfigKeys.EVERYONE_ENABLED).orElse(false);
    }

    @Override
    public List<MentionCandidate> candidates() {
        return List.of(new MentionCandidate(config.get(ConfigKeys.EVERYONE_KEYWORD), this, ID, ID));
    }

    @Override
    public Component display(MentionCandidate candidate) {
        return MINI_MESSAGE.deserialize(config.get(ConfigKeys.EVERYONE_DISPLAY));
    }

    @Override
    public String permission() {
        return config.find(ConfigKeys.EVERYONE_PERMISSION).orElse("");
    }

    @Override
    public PermissionDefault permissionDefault() {
        return PermissionDefault.OP;
    }

    @Override
    public Duration cooldown() {
        return config.find(ConfigKeys.EVERYONE_COOLDOWN).orElse(Duration.ZERO);
    }

    @Override
    public Optional<Sound> sound() {
        return SoundSpec.parse(config.find(ConfigKeys.EVERYONE_SOUND).orElse(null));
    }

    @Override
    public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) {
        return List.copyOf(Bukkit.getOnlinePlayers());
    }

    @Override
    public ConfigKey<List<Notification>> senderMessage() {
        return LangKeys.EVERYONE_SENDER_MESSAGE;
    }

    @Override
    public ConfigKey<List<Notification>> targetMessage() {
        return LangKeys.EVERYONE_TARGET_MESSAGE;
    }

    @Override
    public TagResolver senderPlaceholders(MentionCandidate candidate) {
        return TagResolver.empty();
    }

    @Override
    public TagResolver targetPlaceholders(MentionCandidate candidate) {
        return TagResolver.empty();
    }
}
