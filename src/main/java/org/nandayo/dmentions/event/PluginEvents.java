package org.nandayo.dmentions.event;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.nandayo.dmentions.integration.EssentialsHook;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.LanguageManager;
import org.nandayo.dmentions.service.MessageManager;

import java.util.Arrays;
import java.util.List;

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
        DMentions plugin = DMentions.inst();
        Sound sound;
        if(soundName != null && !soundName.isEmpty()) {
            sound = plugin.getWrapper().getSound(soundName);
        } else {
            sound = null;
        }

        //SENDER
        MessageManager.sendActionBar(sender, senderBar);
        MessageManager.sendTitle(sender, senderTitle);
        if(sound != null) plugin.getWrapper().playSound(sender, sound);

        //TARGET
        new BukkitRunnable() {
            int counter = 0;
            final List<Player> targetList = Arrays.asList(targets);

            @Override
            public void run() {
                for(int i = 0; i < 30; i++) {
                    if(counter >= targetList.size()) {
                        cancel();
                        break;
                    }
                    Player target = targetList.get(counter);
                    counter++;

                    boolean isIgnored = plugin.getConfiguration().getConfig().getBoolean("ignore_respect", true) && EssentialsHook.isIgnored(sender, target);
                    boolean isAFK = plugin.getConfiguration().getConfig().getBoolean("afk_respect", false) && EssentialsHook.isAFK(target);
                    boolean isVanished = plugin.getConfiguration().getConfig().getBoolean("vanish_respect", true) && EssentialsHook.isVanished(target);
                    if(target == null || target.equals(sender) || plugin.isRestricted(sender, target) || isIgnored || isAFK || isVanished) continue;

                    if(sound != null) plugin.getWrapper().playSound(target, sound);
                    MessageManager.sendActionBar(target, targetBar);
                    MessageManager.sendTitle(target, targetTitle);
                }
            }
        }.runTaskTimer(plugin, 0, 10L);
    }
}
