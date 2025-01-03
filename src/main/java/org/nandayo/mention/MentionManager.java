package org.nandayo.mention;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.nandayo.ConfigManager;
import org.nandayo.Main;
import org.nandayo.integration.LP;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MentionManager {

    private final Set<String> validKeywords = new HashSet<>();
    private final Map<String, MentionHolder> mentionHolders = new HashMap<>();

    private String keywordPattern = "";
    private final ConfigManager configManager;
    private final Main plugin;

    public MentionManager(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        reload();
    }

    public Set<String> getValidKeywords() {
        return validKeywords;
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
        String perm = plugin.getPermission(configManager.getString("player.permission", "dmentions.mention.player"));
        mentionHolders.put(player.getName(), new MentionHolder(MentionType.PLAYER, perm, player.getName()));
        updateKeywordPattern();
    }

    public void reload() {
        mentionHolders.clear();

        // LOAD PLAYER KEYWORDS
        if (configManager.getBoolean("player.enabled", false)) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                String keyword = player.getName();
                String perm = plugin.getPermission(configManager.getString("player.permission", "dmentions.mention.player"));
                validKeywords.add(keyword);
                mentionHolders.put(keyword, new MentionHolder(MentionType.PLAYER, perm, keyword));
            });
        }

        // LOAD NEARBY KEYWORD
        if(configManager.getBoolean("nearby.enabled", false)) {
            String keyword = configManager.getString("nearby.keyword", "@nearby");
            String perm = plugin.getPermission(configManager.getString("nearby.permission", "dmentions.mention.nearby"));
            validKeywords.add(keyword);
            mentionHolders.put(keyword, new MentionHolder(MentionType.NEARBY, perm,null));
        }

        // LOAD EVERYONE KEYWORD
        if (configManager.getBoolean("everyone.enabled", false)) {
            String keyword = configManager.getString("everyone.keyword", "@everyone");
            String perm = plugin.getPermission(configManager.getString("everyone.permission", "dmentions.mention.everyone"));
            validKeywords.add(keyword);
            mentionHolders.put(keyword, new MentionHolder(MentionType.EVERYONE, perm,null));
        }

        //LOAD GROUP KEYWORDS
        if (configManager.getBoolean("group.enabled", false) && LP.isConnected()) {
            List<String> disabledGroups = configManager.getStringList("group.disabled_groups");
            String keywordTemplate = configManager.getString("group.keyword", "@{group}");
            LP.getGroups().stream()
                    .filter(group -> !disabledGroups.contains(group))
                    .forEach(group -> {
                        String keyword = keywordTemplate.replace("{group}", group);
                        String perm = plugin.getPermission(configManager.getString("group.permission", "dmentions.mention.group.{group}")).replace("{group}", group);
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

    public MentionHolder getMentionHolder(String keyword) {
        return mentionHolders.get(keyword);
    }

    public Pattern getMentionPattern() {
        return Pattern.compile("(?<!\\S)(" + keywordPattern + ")(?!\\S)");
    }
}
