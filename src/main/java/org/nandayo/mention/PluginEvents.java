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
import org.nandayo.utils.LangManager;
import org.nandayo.utils.MessageManager;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();
        ConfigManager configManager = Main.inst().configManager;
        LangManager langManager = Main.inst().langManager;

        String soundName = configManager.getString("player.sound", "");

        String targetBar = langManager.getMsg("player.action_bar.target_message").replace("{p}", sender.getName());
        String targetTitle = langManager.getMsg("player.title.target_message").replace("{p}", sender.getName());

        String senderBar = langManager.getMsg("player.action_bar.sender_message").replace("{p}", target.getName());
        String senderTitle = langManager.getMsg("player.title.sender_message").replace("{p}", target.getName());

        mention(sender, new Player[]{target}, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onNearbyMention(MentionNearbyEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        ConfigManager configManager = Main.inst().configManager;
        LangManager langManager = Main.inst().langManager;

        String soundName = configManager.getString("nearby.sound", "");

        String targetBar = langManager.getMsg("nearby.action_bar.target_message").replace("{p}", sender.getName());
        String targetTitle = langManager.getMsg("nearby.title.target_message").replace("{p}", sender.getName());

        String senderBar = langManager.getMsg("nearby.action_bar.sender_message");
        String senderTitle = langManager.getMsg("nearby.title.sender_message");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        ConfigManager configManager = Main.inst().configManager;
        LangManager langManager = Main.inst().langManager;

        String soundName = configManager.getString("everyone.sound", "");

        String targetBar = langManager.getMsg("everyone.action_bar.target_message").replace("{p}", sender.getName());
        String targetTitle = langManager.getMsg("everyone.title.target_message").replace("{p}", sender.getName());

        String senderBar = langManager.getMsg("everyone.action_bar.sender_message");
        String senderTitle = langManager.getMsg("everyone.title.sender_message");

        mention(sender, targets, soundName, targetBar, targetTitle, senderBar, senderTitle);
    }

    @EventHandler
    public void onGroupMention(MentionGroupEvent e) {
        Player sender = e.getSender();
        String group = e.getGroup();
        Player[] targets = e.getTargets();

        LangManager langManager = Main.inst().langManager;

        //GROUP CONFIG SECTION
        ConfigurationSection configSection = Main.inst().getConfigGroupSection(group);
        ConfigurationSection langSection = Main.inst().getLangGroupSection(group);
        if(configSection == null || langSection == null) return;

        String soundName = configSection.getString("sound", "");

        String targetBar = langManager.getMsg(langSection,"action_bar.target_message").replace("{p}", sender.getName()).replace("{group}", group);
        String targetTitle = langManager.getMsg(langSection,"title.target_message").replace("{p}", sender.getName()).replace("{group}", group);

        String senderBar = langManager.getMsg(langSection,"action_bar.sender_message").replace("{group}", group);
        String senderTitle = langManager.getMsg(langSection,"title.sender_message").replace("{group}", group);

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
            if(target == sender || !Main.inst().getRestrictConditions(sender,target)) continue;
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
