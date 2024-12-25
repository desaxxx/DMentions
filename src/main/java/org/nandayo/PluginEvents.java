package org.nandayo;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nandayo.Events.MentionEveryoneEvent;
import org.nandayo.Events.MentionPlayerEvent;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();

        //SOUND
        try {
            Sound sound = Sound.valueOf(Main.config.get().getString("player.sound", ""));
            target.playSound(target, sound, 1.0f, 1.0f);
        }catch (IllegalArgumentException exc) {
        }

        //TARGET BAR
        String targetBar = Main.config.get().getString("player.action_bar.target_message");
        if(targetBar != null) targetBar = targetBar.replaceFirst("\\{p}", sender.getName());
        Main.sendActionBar(target, targetBar);

        //SENDER BAR
        String senderBar = Main.config.get().getString("player.action_bar.sender_message");
        if(senderBar != null) senderBar = senderBar.replaceFirst("\\{p}",target.getName());
        Main.sendActionBar(sender, senderBar);
    }
    
    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();

        //SOUND
        Sound sound;
        try {
            sound = Sound.valueOf(Main.config.get().getString("everyone.sound", ""));
        }catch (IllegalArgumentException exc) {
            sound = null;
        }

        //TARGET BAR
        String targetBar = Main.config.get().getString("everyone.action_bar.target_message");
        if(targetBar != null) targetBar = targetBar.replaceFirst("\\{p}", sender.getName());

        //SENDER BAR
        String senderBar = Main.config.get().getString("everyone.action_bar.sender_message");
        Main.sendActionBar(sender, senderBar);
        if(sound != null) sender.playSound(sender, sound, 1.0f, 1.0f);

        for(Player target : targets) {
            if(target == sender) continue;
            if(sound != null) {
                target.playSound(target, sound, 1.0f, 1.0f);
            }
            Main.sendActionBar(target, targetBar);
        }
    }
}
