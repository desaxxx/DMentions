package org.nandayo.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.nandayo.Main;

import java.util.HashMap;

public class CooldownManager {

    public static final HashMap<String, Long> playerCooldown = new HashMap<>(); //PLAYER_NAME, LONG
    public static final HashMap<String, Long> nearbyCooldown = new HashMap<>(); //PLAYER_NAME, LONG
    public static Long everyoneCooldown = 0L;
    public static final HashMap<String, Long> groupCooldown = new HashMap<>(); //GROUP_NAME, LONG

    //LAST MENTION
    public static long getLastPlayerMention(String target) {
        return playerCooldown.getOrDefault(target, 0L);
    }
    public static long getLastNearbyMention(String sender) {
        return nearbyCooldown.getOrDefault(sender, 0L);
    }
    public static long getLastEveryoneMention() {
        return everyoneCooldown;
    }
    public static long getLastGroupMention(String group) {
        return groupCooldown.getOrDefault(group, 0L);
    }

    //COOLDOWN CHECK
    public static boolean playerIsOnCooldown(Player sender, String target) {
        long lastMention = getLastPlayerMention(target);
        long cooldown = Main.configManager.getLong("player.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            playerCooldown.remove(target);
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }
    public static boolean nearbyIsOnCooldown(Player sender) {
        long lastMention = getLastNearbyMention(sender.getName());
        long cooldown = Main.configManager.getLong("nearby.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            nearbyCooldown.remove(sender.getName());
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }
    public static boolean everyoneIsOnCooldown(Player sender) {
        long lastMention = getLastEveryoneMention();
        long cooldown = Main.configManager.getLong("everyone.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            everyoneCooldown = 0L;
            return false;
        }
        cooldownWarn(sender, cooldown-elapsed);
        return true;
    }
    public static boolean groupIsOnCooldown(Player sender, String group) {
        ConfigurationSection section = Main.getGroupSection(group);
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

    public static void cooldownWarn(Player sender, long remained) {
        String type = Main.configManager.getString("cooldown_warn.type", "");
        String message = Main.configManager.getString("cooldown_warn.message", "");
        if(type.isEmpty() || message.isEmpty()) return;

        String formattedMessage = message.replace("{REMAINED}", Main.formattedTime(remained));
        switch (type) {
            case "CHAT" -> Main.sendMessage(sender, formattedMessage);
            case "ACTION_BAR" -> Main.sendActionBar(sender, formattedMessage);
            case "TITLE" -> Main.sendTitle(sender, formattedMessage);
        }
    }
}
