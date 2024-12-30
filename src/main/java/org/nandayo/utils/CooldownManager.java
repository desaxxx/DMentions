package org.nandayo.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.nandayo.Main;

import java.util.HashMap;

public class CooldownManager {

    public static final HashMap<String, Long> playerCooldown = new HashMap<>(); //PLAYER_NAME, LONG
    public static Long everyoneCooldown = 0L;
    public static final HashMap<String, Long> groupCooldown = new HashMap<>(); //GROUP_NAME, LONG

    //LAST MENTION
    public static long getLastPlayerMention(String target) {
        return playerCooldown.getOrDefault(target, 0L);
    }
    public static long getLastEveryoneMention() {
        return everyoneCooldown;
    }
    public static long getLastGroupMention(String group) {
        return groupCooldown.getOrDefault(group, 0L);
    }

    //COOLDOWN CHECK
    public static boolean playerIsOnCooldown(String target) {
        long lastMention = getLastPlayerMention(target);
        long cooldown = Main.configManager.getLong("player.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            playerCooldown.remove(target);
            return false;
        }
        return true;
    }
    public static boolean everyoneIsOnCooldown() {
        long lastMention = getLastEveryoneMention();
        long cooldown = Main.configManager.getLong("everyone.cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            everyoneCooldown = 0L;
            return false;
        }
        return true;
    }
    public static boolean groupIsOnCooldown(String group) {
        ConfigurationSection section = Main.getGroupSection(group);
        if(section == null) return true;

        long lastMention = getLastGroupMention(group);
        long cooldown = section.getLong("cooldown", 0) * 1000;
        long elapsed = System.currentTimeMillis() - lastMention;

        if(elapsed >= cooldown) {
            groupCooldown.remove(group);
            return false;
        }
        return true;
    }
}
