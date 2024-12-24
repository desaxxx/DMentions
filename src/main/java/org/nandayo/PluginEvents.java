package org.nandayo;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nandayo.Events.MentionPlayerEvent;

public class PluginEvents implements Listener {

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();

        try {
            Sound sound = Sound.valueOf(Main.config.get().getString("notify.sound", "ENTITY_PLAYER_LEVELUP"));
            target.playSound(target, sound, 1.0f, 1.0f);
        }catch (IllegalArgumentException exc) {
            Main.inst().getLogger().warning("Invalid sound name.");
        }

        //Receiver action bar
        String receiverBar = Main.config.get().getString("notify.action_bar.receiver_message");
        if(receiverBar != null) receiverBar = receiverBar.replaceFirst("\\{p}", sender.getName());
        Main.sendActionBar(target, receiverBar);

        //Sender action bar
        String senderBar = Main.config.get().getString("notify.action_bar.sender_message");
        if(senderBar != null) senderBar = senderBar.replaceFirst("\\{p}",target.getName());
        Main.sendActionBar(sender, senderBar);
    }
}
