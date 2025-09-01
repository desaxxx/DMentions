package org.nandayo.dmentions.event;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.model.SoundProperty;
import org.nandayo.dmentions.provider.VanishProvider;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.message.Message;
import org.nandayo.dmentions.util.DUtil;

import java.util.Arrays;
import java.util.List;

public class PluginEvents implements Listener {
    static public final PluginEvents INSTANCE = new PluginEvents();

    private PluginEvents() {}

    @EventHandler
    public void onPlayerMention(MentionPlayerEvent e) {
        Player sender = e.getSender();
        Player target = e.getTarget();
        Config config = DMentions.inst().getConfiguration();

        SoundProperty soundProperty = SoundProperty.deserialize(config.getConfig().getString("player.sound", ""));

        Message targetMessage = Message.PLAYER_TARGET_MESSAGE
                .replaceValue("{sender}", sender.getName())
                .replaceValue("{target}", target.getName());
        Message senderMessage = Message.PLAYER_SENDER_MESSAGE
                .replaceValue("{target}", target.getName())
                .replaceValue("{sender}", sender.getName());

        mention(sender, new Player[]{target}, soundProperty, targetMessage, senderMessage);
    }

    @EventHandler
    public void onNearbyMention(MentionNearbyEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        Config config = DMentions.inst().getConfiguration();

        SoundProperty soundProperty = SoundProperty.deserialize(config.getConfig().getString("nearby.sound", ""));

        Message targetMessage = Message.NEARBY_TARGET_MESSAGE.replaceValue("{sender}", sender.getName());
        Message senderMessage = Message.NEARBY_SENDER_MESSAGE.replaceValue("{sender}", sender.getName());

        mention(sender, targets, soundProperty, targetMessage, senderMessage);
    }

    @EventHandler
    public void onEveryoneMention(MentionEveryoneEvent e) {
        Player sender = e.getSender();
        Player[] targets = e.getTargets();
        Config config = DMentions.inst().getConfiguration();

        SoundProperty soundProperty = SoundProperty.deserialize(config.getConfig().getString("everyone.sound", ""));

        Message targetMessage = Message.EVERYONE_TARGET_MESSAGE.replaceValue("{sender}", sender.getName());
        Message senderMessage = Message.EVERYONE_SENDER_MESSAGE.replaceValue("{sender}", sender.getName());

        mention(sender, targets, soundProperty, targetMessage, senderMessage);
    }

    @EventHandler
    public void onGroupMention(MentionGroupEvent e) {
        Player sender = e.getSender();
        String group = e.getGroup();
        Player[] targets = e.getTargets();

        //GROUP CONFIG SECTION
        String groupLanguageKey = DUtil.getGroupLanguageKey(group);
        ConfigurationSection configSection = DUtil.getGroupConfigSection(group);
        if(configSection == null) return;

        SoundProperty soundProperty = SoundProperty.deserialize(configSection.getString("sound", ""));

        Message targetMessage = Message.GROUP_X_TARGET_MESSAGE
                .replaceKey("{x}", groupLanguageKey)
                .replaceValue("{sender}", sender.getName())
                .replaceValue("{group}", group);
        Message senderMessage = Message.GROUP_X_SENDER_MESSAGE
                .replaceKey("{x}", groupLanguageKey)
                .replaceValue("{sender}", sender.getName())
                .replaceValue("{group}", group);

        mention(sender, targets, soundProperty, targetMessage, senderMessage);
    }

    /*
     * General mention method
     */
    private void mention(@NotNull Player sender, @NotNull Player[] targets, @NotNull SoundProperty soundProperty,
                         @NotNull Message targetMessage, @NotNull Message senderMessage) {
        DMentions plugin = DMentions.inst();
        Sound sound = plugin.getWrapper().getSound(soundProperty.getKey());

        //SENDER
        senderMessage.sendMessage(sender);
        if(sound != null) plugin.getWrapper().playSound(sender, sound, soundProperty.getVolume(), soundProperty.getPitch());

        //TARGET
        new BukkitRunnable() {
            int counter = 0;
            final List<Player> targetList = Arrays.asList(targets);

            final boolean ignoreRespect = plugin.getConfiguration().getConfig().getBoolean("ignore_respect", true);
            final boolean afkRespect = plugin.getConfiguration().getConfig().getBoolean("afk_respect", false);
            final boolean vanishRespect = plugin.getConfiguration().getConfig().getBoolean("vanish_respect", true);

            @Override
            public void run() {
                for(int i = 0; i < 30; i++) {
                    if(counter >= targetList.size()) {
                        cancel();
                        break;
                    }
                    Player target = targetList.get(counter++);

                    boolean isIgnored = ignoreRespect && plugin.getEssentialsHook().isIgnored(sender, target);
                    boolean isAFK = afkRespect && plugin.getEssentialsHook().isAFK(target);
                    boolean isVanished = vanishRespect && VanishProvider.get().isVanished(target);
                    if(target == null || target.equals(sender) || DUtil.isRestricted(sender, target) || isIgnored || isAFK || isVanished) continue;

                    if(sound != null) plugin.getWrapper().playSound(target, sound, soundProperty.getVolume(), soundProperty.getPitch());
                    targetMessage.sendMessage(target);
                }
            }
        }.runTaskTimer(plugin, 0, 10L);
    }
}
