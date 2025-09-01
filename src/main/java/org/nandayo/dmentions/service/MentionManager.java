package org.nandayo.dmentions.service;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.HexUtil;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.event.*;
import org.nandayo.dmentions.integration.EssentialsHook;
import org.nandayo.dmentions.model.MentionHolder;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LuckPermsHook;
import org.nandayo.dmentions.service.message.Message;
import org.nandayo.dmentions.util.DUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MentionManager {
    /**
     * Keyword - MentionHolder
     */
    static private final Map<String, MentionHolder> MENTION_HOLDERS = new HashMap<>();
    static private String KEYWORDS_PATTERN = "";


    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    public MentionManager(@NotNull DMentions plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        load();
    }


    public void removePlayer(Player player) {
        if(!MENTION_HOLDERS.containsKey(player.getName())) return;
        MENTION_HOLDERS.remove(player.getName());
        updateKeywordsPattern();
    }
    public void addPlayer(Player player) {
        if(MENTION_HOLDERS.containsKey(player.getName())) return;
        String perm = config.getConfig().getString("player.permission", "dmentions.mention.player");
        MENTION_HOLDERS.put(player.getName(), new MentionHolder(MentionType.PLAYER, perm, player.getName()));
        updateKeywordsPattern();
    }

    public void load() {
        MENTION_HOLDERS.clear();

        // LOAD PLAYER KEYWORDS
        if (config.getConfig().getBoolean("player.enabled", false)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                String keyword = player.getName();
                String perm = config.getConfig().getString("player.permission", "dmentions.mention.player");
                MENTION_HOLDERS.put(keyword, new MentionHolder(MentionType.PLAYER, perm, keyword));
            });
        }

        // LOAD NEARBY KEYWORD
        if(config.getConfig().getBoolean("nearby.enabled", false)) {
            String keyword = config.getConfig().getString("nearby.keyword", "@nearby");
            String perm = config.getConfig().getString("nearby.permission", "dmentions.mention.nearby");
            MENTION_HOLDERS.put(keyword, new MentionHolder(MentionType.NEARBY, perm));
        }

        // LOAD EVERYONE KEYWORD
        if (config.getConfig().getBoolean("everyone.enabled", false)) {
            String keyword = config.getConfig().getString("everyone.keyword", "@everyone");
            String perm = config.getConfig().getString("everyone.permission", "dmentions.mention.everyone");
            MENTION_HOLDERS.put(keyword, new MentionHolder(MentionType.EVERYONE, perm));
        }

        //LOAD GROUP KEYWORDS
        LuckPermsHook luckPermsHook = plugin.getLuckPermsHook();
        if (config.getConfig().getBoolean("group.enabled", false)) {
            List<String> disabledGroups = config.getConfig().getStringList("group.disabled_groups");
            String keywordTemplate = config.getConfig().getString("group.keyword", "@{group}");
            luckPermsHook.getGroups().stream()
                    .filter(group -> !disabledGroups.contains(group))
                    .forEach(group -> {
                        String keyword = keywordTemplate.replace("{group}", group);
                        String perm = config.getConfig().getString("group.permission", "dmentions.mention.group.{group}").replace("{group}", group);
                        MENTION_HOLDERS.put(keyword, new MentionHolder(MentionType.GROUP, perm, group));
                    });
        }

        //UPDATE PATTERN
        updateKeywordsPattern();
    }

    private void updateKeywordsPattern() {
        KEYWORDS_PATTERN = MENTION_HOLDERS.keySet().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
    }

    /**
     * Get immutable map of copy of {@link #MENTION_HOLDERS}.
     * @return Map of Keyword - MentionHolder
     * @since 1.8.3
     */
    public Map<String, MentionHolder> getMentionHolders() {
        return ImmutableMap.copyOf(MENTION_HOLDERS);
    }

    /**
     * Get instant keyword patterns.
     * @return Keywords
     * @since 1.8.3
     */
    public String getKeywordsPattern() {
        return KEYWORDS_PATTERN;
    }

    /**
     * Get Mentioned replaced string.
     * @param plugin DMentions
     * @param sender Player
     * @param message Message
     * @return Mentioned Message
     */
    public String getMentionedString(@NotNull DMentions plugin, @NotNull Player sender, @NotNull String message) {
        final CooldownManager cooldownManager = plugin.getCooldownManager();
        final EssentialsHook essHook = plugin.getEssentialsHook();

        Pattern mentionPattern = Pattern.compile(String.format("(?<!\\S)(%s)(?=[\\s\\p{P}]|$)", KEYWORDS_PATTERN));
        Matcher matcher = mentionPattern.matcher(message);

        StringBuilder updatedMessage = new StringBuilder();
        int lastAppendPosition = 0;

        int mentionLimit = config.getConfig().getInt("mention_limit", 2);
        int mentionCounter = 0;

        while (matcher.find()) {
            if(mentionCounter >= mentionLimit) {
                break;
            }
            String keyword = matcher.group(1);
            MentionHolder mentionHolder = MENTION_HOLDERS.get(keyword);

            // MentionHolder and permission check
            if (mentionHolder == null || !sender.hasPermission(mentionHolder.getPermission())) continue;

            // Cooldown check
            long remainedCooldown = cooldownManager.getRemaining(mentionHolder.getType(), mentionHolder.getTarget());

            String displayText = keyword;
            String suffixColor = getSuffixColor(sender);

            switch (mentionHolder.getType()) {
                case PLAYER:
                    if(mentionHolder.getTarget() == null) continue;
                    Player target = Bukkit.getPlayerExact(mentionHolder.getTarget());
                    if(target == null || !plugin.getUserManager().getMentionMode(target)) continue;
                    if(remainedCooldown > 0) {
                        cooldownManager.cooldownWarn(sender, remainedCooldown);
                        continue;
                    }
                    if(DUtil.isRestricted(sender, target)) {
                        Message.MENTION_RESTRICTED_WARN
                                .replaceValue("{target}", target.getName())
                                .sendMessage(sender);
                        continue;
                    }
                    if(plugin.getConfiguration().getConfig().getBoolean("ignore_respect", true) && essHook.isIgnored(sender, target)) {
                        Message.IGNORE_WARN
                                .replaceValue("{target}", target.getName())
                                .sendMessage(sender);
                        continue;
                    }
                    if(plugin.getConfiguration().getConfig().getBoolean("afk_respect", false) && essHook.isAFK(target)) {
                        Message.AFK_WARN
                                .replaceValue("{target}", target.getName())
                                .sendMessage(sender);
                        continue;
                    }

                    displayText = getPlayerDisplay(target) + suffixColor;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new MentionPlayerEvent(sender, target)));
                    break;

                case NEARBY:
                    if(remainedCooldown > 0) {
                        cooldownManager.cooldownWarn(sender, remainedCooldown);
                        continue;
                    }

                    displayText = config.getConfig().getString("nearby.display", "@nearby") + suffixColor;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new MentionNearbyEvent(sender, getNearbyPlayers(sender))));
                    break;

                case EVERYONE:
                    if(remainedCooldown > 0) {
                        cooldownManager.cooldownWarn(sender, remainedCooldown);
                        continue;
                    }

                    displayText = config.getConfig().getString("everyone.display", "@everyone") + suffixColor;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new MentionEveryoneEvent(sender, Bukkit.getOnlinePlayers().toArray(new Player[0]))));
                    break;

                case GROUP:
                    String group = mentionHolder.getTarget();
                    if(group == null) continue;
                    ConfigurationSection section = DUtil.getGroupConfigSection(group);
                    if(section == null) continue;
                    if(remainedCooldown > 0) {
                        cooldownManager.cooldownWarn(sender, remainedCooldown);
                        continue;
                    }

                    //Getting from group section
                    displayText = section.getString("display", "{group}").replace("{group}", group) + suffixColor;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new MentionGroupEvent(sender, group, plugin.getLuckPermsHook().getOnlinePlayersInGroup(group))));
                    break;
            }

            mentionCounter++;
            cooldownManager.updateLastUse(mentionHolder.getType(), mentionHolder.getTarget());

            // Add the message from last end to new start.
            updatedMessage.append(message, lastAppendPosition, matcher.start());
            lastAppendPosition = matcher.end();
            // Add matched key
            updatedMessage.append(HexUtil.colorize(displayText));
        }

        // Add the rest of the message to builder
        updatedMessage.append(message.substring(lastAppendPosition));
        return updatedMessage.toString();
    }

    /**
     * Get player display word depending on customized or not.
     * @param target Player
     * @return Display string
     */
    private String getPlayerDisplay(@NotNull Player target) {
        String mentionDisplay = plugin.getUserManager().getMentionDisplay(target);
        if(mentionDisplay == null || mentionDisplay.isEmpty() || mentionDisplay.equalsIgnoreCase(target.getName())) {
            return config.getConfig().getString("player.display", "{p}").replace("{p}", plugin.getUserManager().getMentionDisplay(target));
        }
        return config.getConfig().getString("player.customized_display", "{display}").replace("{display}", mentionDisplay);
    }

    /**
     * Get suffix color of the player depending on their group suffix on config.
     * @param sender Player
     * @return Suffix string
     */
    private String getSuffixColor(@NotNull Player sender) {
        LuckPermsHook luckPermsHook = plugin.getLuckPermsHook();
        if(!luckPermsHook.isMaskNull()) {
            String group = luckPermsHook.getGroup(sender);
            ConfigurationSection section = config.getConfig().getConfigurationSection("suffix_color.group");
            if(section != null && section.contains(group)) {
                return section.getString(group,"");
            }
        }
        return config.getConfig().getString("suffix_color.group.__OTHER__", "");
    }

    /**
     * Get nearby players around the given player.
     * @param player Main Player
     * @return Players
     */
    private Player[] getNearbyPlayers(@NotNull Player player) {
        int radius = config.getConfig().getInt("nearby.radius", 20);
        if(radius <= 0) return new Player[0];

        Set<Player> nearbyPlayers = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                Player nearbyPlayer = (Player) entity;
                nearbyPlayers.add(nearbyPlayer);
            }
        }
        return nearbyPlayers.toArray(new Player[0]);
    }
}
