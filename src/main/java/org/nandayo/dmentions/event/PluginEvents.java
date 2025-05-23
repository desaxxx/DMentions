package org.nandayo.dmentions.event;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.LanguageManager;
import org.nandayo.dmentions.service.MessageManager;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();
        Config config = DMentions.inst().getConfiguration();
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().getLanguageManager();

        String soundName = config.getConfig().getString("player.sound", "");

        String targetBar = LANGUAGE_MANAGER.getString("player.action_bar.target_message")
                .replace("{p}", sender.getName());
        String targetTitle = LANGUAGE_MANAGER.getString("player.title.target_message")
                .replace("{p}", sender.getName());

        String senderBar = LANGUAGE_MANAGER.getString("player.action_bar.sender_message")
                .replace("{p}", target.getName());
        String senderTitle = LANGUAGE_MANAGER.getString("player.title.sender_message")
                .replace("{p}", target.getName());

        mention(sender, new Player[]{target}, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onNearbyMention(MentionNearbyEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        Config config = DMentions.inst().getConfiguration();
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().getLanguageManager();

        String soundName = config.getConfig().getString("nearby.sound", "");

        String targetBar = LANGUAGE_MANAGER.getString("nearby.action_bar.target_message")
                .replace("{p}", sender.getName());
        String targetTitle = LANGUAGE_MANAGER.getString("nearby.title.target_message")
                .replace("{p}", sender.getName());

        String senderBar = LANGUAGE_MANAGER.getString("nearby.action_bar.sender_message");
        String senderTitle = LANGUAGE_MANAGER.getString("nearby.title.sender_message");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        Config config = DMentions.inst().getConfiguration();
        LanguageManager LANGUAGE_MANAGER = DMentions.inst().getLanguageManager();

        String soundName = config.getConfig().getString("everyone.sound", "");

        String targetBar = LANGUAGE_MANAGER.getString("everyone.action_bar.target_message")
                .replace("{p}", sender.getName());
        String targetTitle = LANGUAGE_MANAGER.getString("everyone.title.target_message")
                .replace("{p}", sender.getName());

        String senderBar = LANGUAGE_MANAGER.getString("everyone.action_bar.sender_message");
        String senderTitle = LANGUAGE_MANAGER.getString("everyone.title.sender_message");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onGroupMention(MentionGroupEvent e) {
        Player sender = e.getSender();
        String group = e.getGroup();
        Player[] targets = e.getTargets();

        LanguageManager LANGUAGE_MANAGER = DMentions.inst().getLanguageManager();

        //GROUP CONFIG SECTION
        ConfigurationSection configSection = DMentions.inst().getConfigGroupSection(group);
        ConfigurationSection languageSection = DMentions.inst().getLanguageGroupSection(group);
        if(configSection == null || languageSection == null) return;

        String soundName = configSection.getString("sound", "");

        String targetBar = LANGUAGE_MANAGER.getString(languageSection,"action_bar.target_message")
                .replace("{p}", sender.getName())
                .replace("{group}", group);
        String targetTitle = LANGUAGE_MANAGER.getString(languageSection,"title.target_message")
                .replace("{p}", sender.getName())
                .replace("{group}", group);

        String senderBar = LANGUAGE_MANAGER.getString(languageSection,"action_bar.sender_message")
                .replace("{group}", group);
        String senderTitle = LANGUAGE_MANAGER.getString(languageSection,"title.sender_message")
                .replace("{group}", group);

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    /*
     * General mention method
     */
    private void mention(Player sender, Player[] targets, String soundName, String targetBar, String targetTitle, String senderBar, String senderTitle) {
        Sound sound = null;
        if(soundName != null && !soundName.isEmpty()) {
            sound = DMentions.inst().getWrapper().getSound(soundName);
        }

        MessageManager messageManager = new MessageManager(DMentions.inst());
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
