package org.nandayo.mention;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nandayo.ConfigManager;
import org.nandayo.Main;
import org.nandayo.mention.Events.MentionEveryoneEvent;
import org.nandayo.mention.Events.MentionGroupEvent;
import org.nandayo.mention.Events.MentionNearbyEvent;
import org.nandayo.mention.Events.MentionPlayerEvent;
import org.nandayo.utils.MessageManager;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();
        ConfigManager configManager = Main.inst().configManager;

        String soundName = configManager.getString("player.sound", "");

        String targetBar = configManager.getString("player.action_bar.target_message", "").replace("{p}", sender.getName());
        String targetTitle = configManager.getString("player.title.target_message", "").replace("{p}", sender.getName());

        String senderBar = configManager.getString("player.action_bar.sender_message", "").replace("{p}", target.getName());
        String senderTitle = configManager.getString("player.title.sender_message", "").replace("{p}", target.getName());

        mention(sender, new Player[]{target}, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onNearbyMention(MentionNearbyEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        ConfigManager configManager = Main.inst().configManager;

        String soundName = configManager.getString("nearby.sound", "");

        String targetBar = configManager.getString("nearby.action_bar.target_message", "").replace("{p}", sender.getName());
        String targetTitle = configManager.getString("nearby.title.target_message", "").replace("{p}", sender.getName());

        String senderBar = configManager.getString("nearby.action_bar.sender_message", "");
        String senderTitle = configManager.getString("nearby.title.sender_message", "");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        ConfigManager configManager = Main.inst().configManager;

        String soundName = configManager.getString("everyone.sound", "");

        String targetBar = configManager.getString("everyone.action_bar.target_message", "").replace("{p}", sender.getName());
        String targetTitle = configManager.getString("everyone.title.target_message", "").replace("{p}", sender.getName());

        String senderBar = configManager.getString("everyone.action_bar.sender_message", "");
        String senderTitle = configManager.getString("everyone.title.sender_message", "");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onGroupMention(MentionGroupEvent e) {
        Player sender = e.getSender();
        String group = e.getGroup();
        Player[] targets = e.getTargets();

        //GROUP CONFIG SECTION
        ConfigurationSection section = Main.inst().getGroupSection(group);
        if(section == null) return;

        String soundName = section.getString("sound", "");

        String targetBar = section.getString("action_bar.target_message", "").replace("{p}", sender.getName()).replace("{group}", group);
        String targetTitle = section.getString("title.target_message", "").replace("{p}", sender.getName()).replace("{group}", group);

        String senderBar = section.getString("action_bar.sender_message","").replace("{group}", group);
        String senderTitle = section.getString("title.sender_message", "").replace("{group}", group);

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    //GENERAL MENTION METHOD
    private void mention(Player sender, Player[] targets, String soundName, String targetBar, String targetTitle, String senderBar, String senderTitle) {
        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException exc) {
            sound = null;
        }

        MessageManager messageManager = new MessageManager(Main.inst().configManager);
        //SENDER
        messageManager.sendActionBar(sender, senderBar);
        messageManager.sendTitle(sender, senderTitle);
        if(sound != null) sender.playSound(sender, sound, 0.6f, 1.0f);

        //TARGET
        int counter = 0;
        for(Player target : targets) {
            if(target == sender) continue;
            if(sound != null) {
                target.playSound(target, sound, 0.6f, 1.0f);
            }
            messageManager.sendActionBar(target, targetBar);
            messageManager.sendTitle(target, targetTitle);

            if(++counter % 15 == 0) {
                Bukkit.getScheduler().runTaskLater(Main.inst(), () -> {}, 10L);
            }
        }
    }
}
