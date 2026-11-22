package dev.dash.dmentions.mention.type;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.group.GroupProvider;
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
import java.util.stream.Collectors;

public final class GroupMentionType implements MentionType {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final String ID = "group";

    private final XConfig config;
    private final GroupProvider groupProvider;

    public GroupMentionType(XConfig config, GroupProvider groupProvider) {
        this.config = config;
        this.groupProvider = groupProvider;
    }


    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean enabled() {
        return config.find(ConfigKeys.GROUP_ENABLED).orElse(false);
    }

    @Override
    public List<MentionCandidate> candidates() {
        List<MentionCandidate> result = new ArrayList<>();
        List<String> disabled = config.find(ConfigKeys.GROUP_DISABLED_GROUPS).orElse(List.of()).stream().map(n -> n.toLowerCase(Locale.ROOT)).toList();
        String keywordTemplate = config.get(ConfigKeys.GROUP_KEYWORD);

        for (String group : groupProvider.groups()) {
            if (disabled.contains(group.toLowerCase(Locale.ROOT))) continue;

            result.add(new MentionCandidate(keywordTemplate.replace("<group>", group), this, group, group));
        }
        return List.copyOf(result);
    }

    @Override
    public Component display(MentionCandidate candidate) {
        return MINI_MESSAGE.deserialize(config.get(ConfigKeys.GROUP_DISPLAY), Placeholder.unparsed("group", candidate.label()));
    }

    @Override
    public String permission() {
        return "";
    }

    @Override
    public String permission(MentionCandidate candidate) {
        return config.find(ConfigKeys.GROUP_PERMISSION).orElse("").replace("<group>", candidate.label());
    }

    @Override
    public PermissionDefault permissionDefault() {
        return PermissionDefault.OP;
    }

    @Override
    public Duration cooldown() {
        return config.find(ConfigKeys.GROUP_COOLDOWN).orElse(Duration.ZERO);
    }

    @Override
    public Optional<Sound> sound() {
        return SoundSpec.parse(config.find(ConfigKeys.GROUP_SOUND).orElse(null));
    }

    @Override
    public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> groupProvider.isMember(p, candidate.label()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public ConfigKey<List<Notification>> senderMessage() {
        return LangKeys.GROUP_SENDER_MESSAGE;
    }

    @Override
    public ConfigKey<List<Notification>> targetMessage() {
        return LangKeys.GROUP_TARGET_MESSAGE;
    }

    @Override
    public TagResolver senderPlaceholders(MentionCandidate candidate) {
        return Placeholder.unparsed("group", candidate.label());
    }

    @Override
    public TagResolver targetPlaceholders(MentionCandidate candidate) {
        return Placeholder.unparsed("group", candidate.label());
    }
}
