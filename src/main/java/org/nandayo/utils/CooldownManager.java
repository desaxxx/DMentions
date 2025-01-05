package org.nandayo.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.nandayo.ConfigManager;
import org.nandayo.Main;

import java.util.HashMap;

public class CooldownManager {

    private final HashMap<String, Long> playerCooldown = new HashMap<>(); //PLAYER_NAME, LONG
    private final HashMap<String, Long> nearbyCooldown = new HashMap<>(); //PLAYER_NAME, LONG
    private Long everyoneCooldown = 0L;
    private final HashMap<String, Long> groupCooldown = new HashMap<>(); //GROUP_NAME, LONG

    private final ConfigManager configManager;
    private final Main plugin;
    public CooldownManager(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }


    //LAST MENTION
    public long getLastPlayerMention(String target) {
        return playerCooldown.getOrDefault(target, 0L);
    }
    public long getLastNearbyMention(String sender) {
        return nearbyCooldown.getOrDefault(sender, 0L);
    }
    public long getLastEveryoneMention() {
        return everyoneCooldown;
    }
    public long getLastGroupMention(String group) {
        return groupCooldown.getOrDefault(group, 0L);
    }

    //SET
    public void setLastPlayerMention(String target, long time) {
        playerCooldown.put(target, time);
    }
    public void setLastNearbyMention(String sender, long time) {
        nearbyCooldown.put(sender, time);
    }
    public void setLastEveryoneMention(long time) {
        everyoneCooldown = time;
    }
    public void setLastGroupMention(String group, long time) {
        groupCooldown.put(group, time);
    }

    //REMOVE PLAYER COOLDOWN
    public void removeLastPlayerMention(String target) {
        playerCooldown.remove(target);
    }

    //COOLDOWN CHECK
    public boolean playerIsOnCooldown(Player sender, String target) {
        long lastMention = getLastPlayerMention(target);
        long cooldown = configManager.getLong("player.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            playerCooldown.remove(target);
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }
    public boolean nearbyIsOnCooldown(Player sender) {
        long lastMention = getLastNearbyMention(sender.getName());
        long cooldown = configManager.getLong("nearby.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            nearbyCooldown.remove(sender.getName());
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }
    public boolean everyoneIsOnCooldown(Player sender) {
        long lastMention = getLastEveryoneMention();
        long cooldown = configManager.getLong("everyone.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            everyoneCooldown = 0L;
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }
    public boolean groupIsOnCooldown(Player sender, String group) {
        ConfigurationSection section = plugin.getConfigGroupSection(group);
        if(section == null) return true;

        long lastMention = getLastGroupMention(group);
        long cooldown = section.getLong("cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            groupCooldown.remove(group);
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }

    private void cooldownWarn(Player sender, long remained) {
        String type = configManager.getString("cooldown_warn.type", "");
        String message = configManager.getString("cooldown_warn.message", "");
        if(type.isEmpty() || message.isEmpty()) return;

        String formattedMessage = message.replace("{REMAINED}", plugin.formattedTime(remained));
        MessageManager messageManager = new MessageManager(configManager);
        switch (type) {
            case "CHAT" -> messageManager.sendMessage(sender, formattedMessage);
            case "ACTION_BAR" -> messageManager.sendActionBar(sender, formattedMessage);
            case "TITLE" -> messageManager.sendTitle(sender, formattedMessage);
        }
    }
}
