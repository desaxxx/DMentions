package org.nandayo.dmentions.service;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.HexUtil;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.event.*;
import org.nandayo.dmentions.integration.EssentialsHook;
import org.nandayo.dmentions.model.MentionHolder;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LP;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MentionManager {

    @Getter
    private final Set<String> validKeywords = new HashSet<>();
    private final Map<String, MentionHolder> mentionHolders = new HashMap<>();

    private String keywordPattern = "";
    private final @NotNull DMentions plugin;
    private final @NotNull Config config;

    public MentionManager(@NotNull DMentions plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        load();
    }

    public void removePlayer(Player player) {
        if(!validKeywords.contains(player.getName())) return;

        validKeywords.remove(player.getName());
        mentionHolders.remove(player.getName());
        updateKeywordPattern();
    }
    public void addPlayer(Player player) {
        if(validKeywords.contains(player.getName())) return;

        validKeywords.add(player.getName());
        String perm = plugin.getPermission(config.getConfig().getString("player.permission", "dmentions.mention.player"));
        mentionHolders.put(player.getName(), new MentionHolder(MentionType.PLAYER, perm, player.getName()));
        updateKeywordPattern();
    }

    public void load() {
        mentionHolders.clear();
        validKeywords.clear();

        // LOAD PLAYER KEYWORDS
        if (config.getConfig().getBoolean("player.enabled", false)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                String keyword = player.getName();
                String perm = plugin.getPermission(config.getConfig().getString("player.permission", "dmentions.mention.player"));
                validKeywords.add(keyword);
                mentionHolders.put(keyword, new MentionHolder(MentionType.PLAYER, perm, keyword));
            });
        }

        // LOAD NEARBY KEYWORD
        if(config.getConfig().getBoolean("nearby.enabled", false)) {
            String keyword = config.getConfig().getString("nearby.keyword", "@nearby");
            String perm = plugin.getPermission(config.getConfig().getString("nearby.permission", "dmentions.mention.nearby"));
            validKeywords.add(keyword);
            mentionHolders.put(keyword, new MentionHolder(MentionType.NEARBY, perm));
        }

        // LOAD EVERYONE KEYWORD
        if (config.getConfig().getBoolean("everyone.enabled", false)) {
            String keyword = config.getConfig().getString("everyone.keyword", "@everyone");
            String perm = plugin.getPermission(config.getConfig().getString("everyone.permission", "dmentions.mention.everyone"));
            validKeywords.add(keyword);
            mentionHolders.put(keyword, new MentionHolder(MentionType.EVERYONE, perm));
        }

        //LOAD GROUP KEYWORDS
        if (config.getConfig().getBoolean("group.enabled", false) && LP.isConnected()) {
            List<String> disabledGroups = config.getConfig().getStringList("group.disabled_groups");
            String keywordTemplate = config.getConfig().getString("group.keyword", "@{group}");
            LP.getGroups().stream()
                    .filter(group -> !disabledGroups.contains(group))
                    .forEach(group -> {
                        String keyword = keywordTemplate.replace("{group}", group);
                        String perm = plugin.getPermission(config.getConfig().getString("group.permission", "dmentions.mention.group.{group}")).replace("{group}", group);
                        validKeywords.add(keyword);
                        mentionHolders.put(keyword, new MentionHolder(MentionType.GROUP, perm, group));
                    });
        }

        //UPDATE PATTERN
        updateKeywordPattern();
    }

    private void updateKeywordPattern() {
        keywordPattern = validKeywords.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
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

        Pattern mentionPattern = Pattern.compile(String.format("(?<!\\S)(%s)(?=[\\s\\p{P}]|$)", keywordPattern));
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
            MentionHolder mentionHolder = mentionHolders.get(keyword);

            // MentionHolder and permission check
            if (mentionHolder == null || !sender.hasPermission(mentionHolder.getPerm())) continue;

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
                    if(plugin.isRestricted(sender, target)) {
                        MessageManager.sendSortedMessage(sender, plugin.getLanguageManager().getString("mention_restricted_warn"));
                        continue;
                    }
                    if(plugin.getConfiguration().getConfig().getBoolean("ignore_respect", true) && EssentialsHook.isIgnored(sender, target)) {
                        MessageManager.sendSortedMessage(sender, plugin.getLanguageManager().getString("ignore_warn"));
                        continue;
                    }
                    if(plugin.getConfiguration().getConfig().getBoolean("afk_respect", false) && EssentialsHook.isAFK(target)) {
                        MessageManager.sendSortedMessage(sender, plugin.getLanguageManager().getString("afk_warn"));
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
                    ConfigurationSection section = plugin.getConfigGroupSection(group);
                    if(section == null) continue;
                    if(remainedCooldown > 0) {
                        cooldownManager.cooldownWarn(sender, remainedCooldown);
                        continue;
                    }

                    //Getting from group section
                    displayText = section.getString("display", "{group}").replace("{group}", group) + suffixColor;
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new MentionGroupEvent(sender, group, LP.getOnlinePlayersInGroup(group))));
                    break;
            }

            mentionCounter++;
            cooldownManager.updateLastUse(mentionHolder.getType(), mentionHolder.getTarget());

            // Add the message from last end to new start.
            updatedMessage.append(message, lastAppendPosition, matcher.start());
            lastAppendPosition = matcher.end();
            // Add matched key
            updatedMessage.append(HexUtil.color(displayText));
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
        if(LP.isConnected()) {
            String group = LP.getGroup(sender);
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
