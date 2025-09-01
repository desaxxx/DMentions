package org.nandayo.dmentions.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

/**
 * @since 1.8.3
 */
public class DUtil {
    static private final String OTHER_KEY = "__OTHER__";

    /**
     * Get group key from {@link Config}.<br>
     * <b>NOTE:</b> This returns {@link #OTHER_KEY} if the configuration doesn't specifically include given group name.
     * @param groupName Group name
     * @return Configuration key
     * @since 1.8.3
     */
    @NotNull
    static public String getGroupConfigKey(String groupName) {
        if(groupName == null || groupName.isEmpty()) return OTHER_KEY;
        ConfigurationSection section = DMentions.inst().getConfig().getConfigurationSection("group.list");
        if(section == null || !section.contains(groupName)) return OTHER_KEY;
        return groupName;
    }

    /**
     * Get group key from {@link LanguageManager}.<br>
     * <b>NOTE:</b> This returns {@link #OTHER_KEY} if the configuration doesn't specifically include given group name.
     * @param groupName Group name
     * @return Configuration key
     * @since 1.8.3
     */
    @NotNull
    static public String getGroupLanguageKey(String groupName) {
        if(groupName == null || groupName.isEmpty()) return OTHER_KEY;
        ConfigurationSection section = DMentions.inst().getConfig().getConfigurationSection("group");
        if(section == null || !section.contains(groupName)) return OTHER_KEY;
        return groupName;
    }

    /**
     * Get ConfigurationSection of given group from {@link Config}.<br>
     * <b>NOTE:</b> This method uses {@link #getGroupConfigKey(String)} for config key.
     * @param groupName Group name
     * @return ConfigurationSection if found, else {@code null}.
     * @since 1.8.3
     */
    @Nullable
    static public ConfigurationSection getGroupConfigSection(String groupName) {
        if(groupName == null || groupName.isEmpty()) return null;
        Config config = DMentions.inst().getConfiguration();
        if(config.getConfig().getStringList("group.disabled_groups").contains(groupName)) return null;
        return config.getConfig().getConfigurationSection("group.list." + getGroupConfigKey(groupName));
    }

    /**
     * Get ConfigurationSection of given group from {@link LanguageManager}
     * <b>NOTE:</b> This method uses {@link #getGroupLanguageKey(String)} for config key.
     * @param groupName Group nam
     * @return ConfigurationSection if found, else {@code null}.
     * @since 1.8.3
     */
    @Nullable
    static public ConfigurationSection getGroupLanguageSection(String groupName) {
        if(groupName == null || groupName.isEmpty()) return null;
        LanguageManager languageManager = DMentions.inst().getLanguageManager();
        return languageManager.getSection("group." + getGroupLanguageKey(groupName));
    }

    /**
     * Replace prefix placeholders with the prefix.
     * @param str Message
     * @return Replaced Message
     * @since 1.8.3
     */
    static public String replacePrefixes(String str) {
        if(str == null || str.isEmpty()) return "";
        String prefix = DMentions.inst().getConfiguration().getConfig().getString("prefix", "");
        return str.replaceAll("\\{PREFIX}", prefix);
    }

    /**
     * Parse a String to Float.
     * @param str String to parse
     * @param def Default float value
     * @return parsed float or the default value
     * @since 1.8.3
     */
    static public float parseFloat(String str, float def) {
        if(str == null || str.isEmpty()) return def;
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Parse a String to Integer.
     * @param str String to parse
     * @param def Default integer value
     * @return parsed integer or the default value
     * @since 1.8.3
     */
    static public int parseInt(String str, int def) {
        if(str == null || str.isEmpty()) return def;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Get a formatted String from given time milliseconds. Format {@code "%d d, %d h"}, {@code "%d h, %d m"},
     * {@code "%d m, %d s"} or {@code "%d s"}
     * @param millisecond Milliseconds of time
     * @return String
     * @since 1.8.3
     */
    @NotNull
    static public String formattedTime(long millisecond) {
        long seconds = millisecond / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        if (days > 0) return String.format("%d d, %d h", days, hours);
        if (hours > 0) return String.format("%d h, %d m", hours, minutes);
        if (minutes > 0) return String.format("%d m, %d s", minutes, seconds);
        return String.format("%d s", seconds);
    }

    /**
     * Check if the target is restricted from being mentioned.
     * @param sender Mention sender
     * @param target Mention target
     * @return {@code false} if target is not restricted or sender has bypass permission, else {@code true}.
     * @since 1.8.3
     */
    static public boolean isRestricted(@NotNull Player sender, @NotNull Player target) {
        if(target.hasPermission("dmentions.mention.restricted")) {
            return !sender.hasPermission("dmentions.mention.restricted.bypass");
        }
        return false;
    }

    /**
     * Get {@link Enchantment} from {@link DEnchantment} for max version compatibility.
     * @param dEnchantment DEnchantment
     * @param def Default DEnchantment
     * @return Enchantment
     * @since 1.8.3
     */
    static public Enchantment getEnchantment(@NotNull DEnchantment dEnchantment, @NotNull DEnchantment def) {
        Enchantment enchantment = dEnchantment.get();
        if (enchantment != null) return enchantment;
        return def.get();
    }

    /**
     * Get {@link Material} from {@link DMaterial} for max version compatibility.
     * @param dMaterial DMaterial
     * @param def Default DMaterial
     * @return Material
     * @since 1.8.3
     */
    static public Material getMaterial(@NotNull DMaterial dMaterial, @NotNull DMaterial def) {
        Material mat = dMaterial.parseMaterial();
        if (mat != null) return mat;
        return def.parseMaterial();
    }
}
