package org.nandayo.mention;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nandayo.Main;
import org.nandayo.mention.Events.MentionEveryoneEvent;
import org.nandayo.mention.Events.MentionGroupEvent;
import org.nandayo.mention.Events.MentionPlayerEvent;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();

        //SOUND
        Sound sound;
        try {
            sound = Sound.valueOf(Main.configManager.getString("everyone.sound", ""));
        }catch (IllegalArgumentException exc) {
            sound = null;
        }

        //TARGET BAR
        String targetBar = Main.configManager.getString("player.action_bar.target_message", "");
        if(targetBar != null) targetBar = targetBar.replace("{p}", sender.getName());
        Main.sendActionBar(target, targetBar);
        if(sound != null) target.playSound(target, sound, 0.6f, 1f);

        //SENDER BAR
        String senderBar = Main.configManager.getString("player.action_bar.sender_message", "");
        if(senderBar != null) senderBar = senderBar.replace("{p}",target.getName());
        Main.sendActionBar(sender, senderBar);
        if(sound != null) sender.playSound(sender, sound, 0.6f, 1f);
    }

    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();

        //SOUND
        Sound sound;
        try {
            sound = Sound.valueOf(Main.configManager.getString("everyone.sound", ""));
        }catch (IllegalArgumentException exc) {
            sound = null;
        }

        //TARGET BAR
        String targetBar = Main.configManager.getString("everyone.action_bar.target_message", "");
        if(targetBar != null) targetBar = targetBar.replaceFirst("\\{p}", sender.getName());

        //SENDER BAR
        String senderBar = Main.configManager.getString("everyone.action_bar.sender_message", "");
        Main.sendActionBar(sender, senderBar);
        if(sound != null) sender.playSound(sender, sound, 0.6f, 1.0f);

        for(Player target : targets) {
            if(target == sender) continue;
            if(sound != null) {
                target.playSound(target, sound, 0.6f, 1.0f);
            }
            Main.sendActionBar(target, targetBar);
        }
    }

    @EventHandler
    public void onGroupMention(MentionGroupEvent e) {
        Player sender = e.getSender();
        String group = e.getGroup();
        Player[] targets = e.getTargets();

        //GROUP CONFIG SECTION
        ConfigurationSection section = Main.getGroupSection(group);
        if(section == null) return;

        //SOUND
        Sound sound;
        try {
            sound = Sound.valueOf(section.getString("sound", ""));
        }catch (IllegalArgumentException exc) {
            sound = null;
        }

        //TARGET BAR
        String targetBar = section.getString("action_bar.target_message");
        if(targetBar != null) targetBar = targetBar.replace("{p}", sender.getName())
                .replace("{group}", group);

        //SENDER BAR
        String senderBar = section.getString("action_bar.sender_message");
        if(senderBar != null) senderBar = senderBar.replace("{group}",group);
        Main.sendActionBar(sender, senderBar);
        if(sound != null) sender.playSound(sender, sound, 0.6f, 1.0f);

        for(Player target : targets) {
            if(target == sender) continue;
            if(sound != null) {
                target.playSound(target, sound, 0.6f, 1.0f);
            }
            Main.sendActionBar(target, targetBar);
        }
    }
}
