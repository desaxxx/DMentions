package org.nandayo;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nandayo.HexUtil.color;

public final class Main extends JavaPlugin implements Listener {

    public static Main plugin;
    public static Main inst() {
        return plugin;
    }

    public static Config config;

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);

        getCommand("oyuncuetiket").setExecutor(new MainCommand());

        config = new Config();
        updatePlayerNamesPattern();
    }

    @Override
    public void onDisable() {
    }

    final HashMap<String, Long> cooldown = new HashMap<>();

    private Pattern playerNamesPattern = Pattern.compile("");

    private void updatePlayerNamesPattern() {
        String pattern = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .reduce((a, b) -> a + "|" + b)
                .orElse(""); // If no players are online, the pattern is empty

        if (!pattern.isEmpty()) {
            playerNamesPattern = Pattern.compile("\\b(" + pattern + ")\\b");
        } else {
            playerNamesPattern = null; // No players online
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updatePlayerNamesPattern();
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        updatePlayerNamesPattern();
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        String msg = e.getMessage();
        Player sender = e.getPlayer();

        if(!config.get().getBoolean("notify.enabled",true)) {
            return;
        }
        if(playerNamesPattern == null) {
            return;
        }

        e.setMessage(getUpdatedMessage(sender, msg, playerNamesPattern.toString()));
    }

    private String getUpdatedMessage(Player sender, String msg, String namesPattern) {
        Pattern pattern = Pattern.compile("\\b(" + namesPattern + ")\\b");
        Matcher matcher = pattern.matcher(msg);

        Sound sound;
        try {
            sound = Sound.valueOf(config.get().getString("notify.sound", "ENTITY_PLAYER_LEVELUP"));
        }catch (IllegalArgumentException e) {
            sound = null;
        }
        StringBuilder updatedMessage = new StringBuilder();
        int lastMatchEnd = 0;

        while (matcher.find()) {
            String mentionedName = matcher.group(1);
            if(isOnCooldown(mentionedName)) continue;

            updatedMessage.append(msg, lastMatchEnd, matcher.start());

            String formattedMention = config.get().getString("notify.pattern", "@{p}").replaceFirst("\\{p}",mentionedName);
            updatedMessage.append(color(formattedMention));

            Player mentionedPlayer = Bukkit.getPlayerExact(mentionedName);
            if (mentionedPlayer != null) {
                if(sound == null) {
                    getLogger().warning("Invalid sound name.");
                }else {
                    cooldown.put(mentionedName, System.currentTimeMillis());
                    mentionedPlayer.playSound(mentionedPlayer, sound, 1.0f, 1.0f);

                    //Action bars
                    String receiverBar = config.get().getString("notify.action_bar.receiver_message");
                    if(receiverBar != null) {
                        receiverBar = receiverBar.replaceFirst("\\{p}",sender.getName());
                    }
                    sendActionBar(mentionedPlayer, receiverBar);

                    String senderBar = config.get().getString("notify.action_bar.sender_message");
                    if(senderBar != null) {
                        senderBar = senderBar.replaceFirst("\\{p}",mentionedPlayer.getName());
                    }
                    sendActionBar(sender, senderBar);
                }
            }

            lastMatchEnd = matcher.end();
        }

        updatedMessage.append(msg.substring(lastMatchEnd));
        return updatedMessage.toString();
    }

    private boolean isOnCooldown(String mentionedName) {
        if(!cooldown.containsKey(mentionedName)) return false;
        long millis = cooldown.get(mentionedName);
        long cooldown = config.get().getLong("notify.cooldown", 0) * 1000;
        return System.currentTimeMillis() - millis < cooldown;
    }

    private void sendActionBar(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(color(msg)));
    }
}