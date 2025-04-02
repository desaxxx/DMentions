package org.nandayo.DMentions.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.nandayo.DMentions.DMentions;
import org.nandayo.DMentions.mention.MentionType;

import java.util.HashMap;
import java.util.Locale;

public class CooldownManager {

    private final DMentions plugin;
    public CooldownManager(DMentions plugin) {
        this.plugin = plugin;
    }

    /**
     * First Key: MentionType<br>
     * Second Key: Cooldown(PLAYER NAME | GROUP NAME | null)
     */
    private final HashMap<MentionType, Cooldown<String>> COOLDOWN_MAP_MS = new HashMap<>();

    /**
     * Set last mention as present unix time.
     * @param mentionType MentionType
     * @param name Player name or Group name
     */
    public void updateLastUse(@NotNull MentionType mentionType, @Nullable String name) {
        COOLDOWN_MAP_MS.computeIfAbsent(mentionType, k -> new Cooldown<>())
                .update(name);
    }

    /**
     * Check whether given name is on cooldown.
     * @param mentionType MentionType
     * @param name Player name or Group name
     * @return remained time
     */
    public long getRemaining(@NotNull MentionType mentionType, @Nullable String name) {
        return COOLDOWN_MAP_MS.computeIfAbsent(mentionType, k -> new Cooldown<>())
                .getRemaining(name, getConfigCooldown(mentionType, name));
    }

    /**
     * Remove a player name of group name from map.
     * @param mentionType MentionType
     * @param name Player name or Group name
     */
    public void removeCooldown(@NotNull MentionType mentionType, @Nullable String name) {
        Cooldown<String> cooldown = COOLDOWN_MAP_MS.get(mentionType);
        if (cooldown != null) {
            cooldown.remove(name);
        }
    }


    /**
     * Config cooldown map.
     * player, nearby, everyone, group_%group%
     */
    private final HashMap<String, Long> CONFIG_COOLDOWN_MS = new HashMap<>();

    /**
     * Update config cooldown times.
     */
    public void updateConfigCooldowns() {
        CONFIG_COOLDOWN_MS.clear();
        CONFIG_COOLDOWN_MS.put("player", plugin.CONFIG_MANAGER.getLong("player.cooldown", 0) * 1000);
        CONFIG_COOLDOWN_MS.put("nearby", plugin.CONFIG_MANAGER.getLong("nearby.cooldown", 0) * 1000);
        CONFIG_COOLDOWN_MS.put("everyone", plugin.CONFIG_MANAGER.getLong("everyone.cooldown", 0) * 1000);

        ConfigurationSection groupSection = plugin.CONFIG_MANAGER.getConfigurationSection("group.list");
        if(groupSection == null) return;
        for(String group : groupSection.getKeys(false)) {
            if(plugin.CONFIG_MANAGER.getStringList("group.disabled_groups").contains(group)) continue;
            CONFIG_COOLDOWN_MS.put("group_" + group, groupSection.getLong(group + ".cooldown", 0) * 1000);
        }
    }

    /**
     * Get config cooldown from MentionType.
     * @param mentionType MentionType
     * @return cooldown time by seconds
     */
    private long getConfigCooldown(@NotNull MentionType mentionType, @Nullable String name) {
        switch (mentionType) {
            case PLAYER:
            case NEARBY:
            case EVERYONE:
                return CONFIG_COOLDOWN_MS.getOrDefault(mentionType.name().toLowerCase(Locale.ENGLISH), 0L);
            case GROUP:
                return CONFIG_COOLDOWN_MS.get("group_" + plugin.getGroupConfigTitle(name));
        }
        return 0L;
    }

    /**
     * Warn the sender if they are on cooldown.
     * @param sender Mention sender
     * @param remaining Remaining time
     */
    public void cooldownWarn(@NotNull Player sender, long remaining) {
        if(remaining <= 0) return;
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        String msg = LANGUAGE_MANAGER.getMessageReplaceable("cooldown_warn")
                .replace("{REMAINED}", plugin.formattedTime(remaining))
                .get()[0];
        new MessageManager(plugin.CONFIG_MANAGER).sendSortedMessage(sender, msg);
    }
}
