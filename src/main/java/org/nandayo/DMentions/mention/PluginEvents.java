package org.nandayo.DMentions.mention;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nandayo.DMentions.mention.Events.*;
import org.nandayo.DMentions.service.ConfigManager;
import org.nandayo.DMentions.DMentions;
import org.nandayo.DMentions.service.LanguageManager;
import org.nandayo.DMentions.service.MessageManager;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();
        ConfigManager configManager = DMentions.inst().CONFIG_MANAGER;
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().LANGUAGE_MANAGER;

        String soundName = configManager.getString("player.sound", "");

        String targetBar = LANGUAGE_MANAGER.getMessageReplaceable("player.action_bar.target_message")
                .replace("{p}", sender.getName()).get()[0];
        String targetTitle = LANGUAGE_MANAGER.getMessageReplaceable("player.title.target_message")
                .replace("{p}", sender.getName()).get()[0];

        String senderBar = LANGUAGE_MANAGER.getMessageReplaceable("player.action_bar.sender_message")
                .replace("{p}", target.getName()).get()[0];
        String senderTitle = LANGUAGE_MANAGER.getMessageReplaceable("player.title.sender_message")
                .replace("{p}", target.getName()).get()[0];

        mention(sender, new Player[]{target}, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onNearbyMention(MentionNearbyEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        ConfigManager configManager = DMentions.inst().CONFIG_MANAGER;
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().LANGUAGE_MANAGER;

        String soundName = configManager.getString("nearby.sound", "");

        String targetBar = LANGUAGE_MANAGER.getMessageReplaceable("nearby.action_bar.target_message")
                .replace("{p}", sender.getName()).get()[0];
        String targetTitle = LANGUAGE_MANAGER.getMessageReplaceable("nearby.title.target_message")
                .replace("{p}", sender.getName()).get()[0];

        String senderBar = (String) LANGUAGE_MANAGER.getMessage("nearby.action_bar.sender_message");
        String senderTitle = (String) LANGUAGE_MANAGER.getMessage("nearby.title.sender_message");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        ConfigManager configManager = DMentions.inst().CONFIG_MANAGER;
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().LANGUAGE_MANAGER;

        String soundName = configManager.getString("everyone.sound", "");

        String targetBar = LANGUAGE_MANAGER.getMessageReplaceable("everyone.action_bar.target_message")
                .replace("{p}", sender.getName()).get()[0];
        String targetTitle = LANGUAGE_MANAGER.getMessageReplaceable("everyone.title.target_message")
                .replace("{p}", sender.getName()).get()[0];

        String senderBar = (String) LANGUAGE_MANAGER.getMessage("everyone.action_bar.sender_message");
        String senderTitle = (String) LANGUAGE_MANAGER.getMessage("everyone.title.sender_message");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onGroupMention(MentionGroupEvent e) {
        Player sender = e.getSender();
        String group = e.getGroup();
        Player[] targets = e.getTargets();

        LanguageManager LANGUAGE_MANAGER = DMentions.inst().LANGUAGE_MANAGER;

        //GROUP CONFIG SECTION
        ConfigurationSection configSection = DMentions.inst().getConfigGroupSection(group);
        ConfigurationSection languageSection = DMentions.inst().getLanguageGroupSection(group);
        if(configSection == null || languageSection == null) return;

        String soundName = configSection.getString("sound", "");

        String targetBar = LANGUAGE_MANAGER.getMessageReplaceable(languageSection,"action_bar.target_message")
                .replace("{p}", sender.getName())
                .replace("{group}", group)
                .get()[0];
        String targetTitle = LANGUAGE_MANAGER.getMessageReplaceable(languageSection,"title.target_message")
                .replace("{p}", sender.getName())
                .replace("{group}", group)
                .get()[0];

        String senderBar = LANGUAGE_MANAGER.getMessageReplaceable(languageSection,"action_bar.sender_message")
                .replace("{group}", group).get()[0];
        String senderTitle = LANGUAGE_MANAGER.getMessageReplaceable(languageSection,"title.sender_message")
                .replace("{group}", group).get()[0];

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    /*
     * General mention method
     */
    private void mention(Player sender, Player[] targets, String soundName, String targetBar, String targetTitle, String senderBar, String senderTitle) {
        Sound sound = null;
        if(soundName != null && !soundName.isEmpty()) {
            sound = DMentions.inst().WRAPPER.getSound(soundName);
        }

        MessageManager messageManager = new MessageManager(DMentions.inst().CONFIG_MANAGER);
        //SENDER
        messageManager.sendActionBar(sender, senderBar);
        messageManager.sendTitle(sender, senderTitle);
        if(sound != null) sender.playSound(sender.getLocation(), sound, 0.6f, 1.0f);

        //TARGET
        int counter = 0;
        for(Player target : targets) {
            if(target == sender || DMentions.inst().isRestricted(sender,target)) continue;
            if(sound != null) {
                target.playSound(target.getLocation(), sound, 0.6f, 1.0f);
            }
            messageManager.sendActionBar(target, targetBar);
            messageManager.sendTitle(target, targetTitle);

            if(++counter % 15 == 0) {
                Bukkit.getScheduler().runTaskLater(DMentions.inst(), () -> {}, 10L);
            }
        }
    }
}
