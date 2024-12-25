package org.nandayo;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.nandayo.Events.MentionEveryoneEvent;
import org.nandayo.Events.MentionPlayerEvent;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nandayo.HexUtil.color;

public final class Main extends JavaPlugin implements Listener {

    private static Main plugin;
    public static Main inst() {
        return plugin;
    }

    public static Config config;

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new PluginEvents(), this);

        getCommand("dmentions").setExecutor(new MainCommand());

        config = new Config();
        config.updateFileKeys();
        updatePlayerNamesPattern();
    }

    @Override
    public void onDisable() {
    }

    //COOLDOWNS
    private static final HashMap<String, Long> cooldown = new HashMap<>();
    private static Long everyoneCooldown = 0L;

    //PATTERNS
    private Pattern playerNamesPattern = Pattern.compile("");
    private void updatePlayerNamesPattern() {
        String pattern = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .reduce((a, b) -> a + "|" + b)
                .orElse("");

        //EVERYONE KEYWORD
        String everyoneKw = config.get().getString("everyone.keyword", "@everyone");
        if (!pattern.contains(everyoneKw)) {
            pattern = everyoneKw + "|" + pattern;
        }
        playerNamesPattern = Pattern.compile("(?<!\\S)(" + pattern + ")(?!\\S)");
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

        if(playerNamesPattern == null) {
            return;
        }
        if(config.get().getBoolean("player.enabled",true) ||
                config.get().getBoolean("everyone.enabled", true)) {
            msg = getUpdatedMessage(sender, msg, playerNamesPattern.toString());
            e.setMessage(msg);
        }
    }

    //UPDATED CHAT MESSAGE
    private String getUpdatedMessage(Player sender, String msg, String namesPattern) {
        //OTHER KEYWORDS
        String everyoneKw = config.get().getString("everyone.keyword", "@everyone");

        //DISPLAY
        String everyoneDisplay = config.get().getString("everyone.display", "&2@everyone&f");
        String playerDisplay = config.get().getString("player.display", "&a@{p}&f");

        //PERMISSIONS
        String playerPerm = getPermission(config.get().getString("player.permission"));
        String everyonePerm = getPermission(config.get().getString("everyone.permission"));

        //PATTERN
        Pattern pattern = Pattern.compile("(?<!\\S)(" + namesPattern + ")(?!\\S)");
        Matcher matcher = pattern.matcher(msg);
        StringBuilder updatedMessage = new StringBuilder();
        int lastMatchEnd = 0;

        //WORD CHECK
        while (matcher.find()) {
            String word = matcher.group(1);

            //EVERYONE MENTION
            if(word.equalsIgnoreCase(everyoneKw) && !everyoneIsOnCooldown() && sender.hasPermission(everyonePerm)) {
                everyoneCooldown = System.currentTimeMillis();

                updatedMessage.append(msg, lastMatchEnd, matcher.start());
                updatedMessage.append(color(everyoneDisplay));

                Bukkit.getScheduler().runTask(this, () -> {
                    Player[] targets = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                    MentionEveryoneEvent mentionEvent = new MentionEveryoneEvent(sender, targets);
                    Bukkit.getPluginManager().callEvent(mentionEvent);
                });
                lastMatchEnd = matcher.end();
                continue;
            }

            //PLAYER MENTION
            Player target = Bukkit.getPlayerExact(word);
            if(target == null || playerIsOnCooldown(word) || !sender.hasPermission(playerPerm)) continue;

            cooldown.put(word, System.currentTimeMillis());
            updatedMessage.append(msg, lastMatchEnd, matcher.start());

            String formattedMention = playerDisplay.replaceFirst("\\{p}", word);
            updatedMessage.append(color(formattedMention));

            Bukkit.getScheduler().runTask(this, () -> {
                MentionPlayerEvent mentionEvent = new MentionPlayerEvent(sender, target);
                Bukkit.getPluginManager().callEvent(mentionEvent);
            });
            lastMatchEnd = matcher.end();
        }

        updatedMessage.append(msg.substring(lastMatchEnd));
        return updatedMessage.toString();
    }


    //LAST MENTION
    private long getLastPlayerMention(String target) {
        return cooldown.getOrDefault(target, 0L);
    }
    private long getLastEveryoneMention() {
        return everyoneCooldown;
    }

    //COOLDOWN CHECK
    private boolean playerIsOnCooldown(String target) {
        long lastMention = getLastPlayerMention(target);
        long cooldown = config.get().getLong("player.cooldown", 0) * 1000;
        return System.currentTimeMillis() - lastMention < cooldown;
    }
    private boolean everyoneIsOnCooldown() {
        long lastMention = getLastEveryoneMention();
        long cooldown = config.get().getLong("everyone.cooldown", 0) * 1000;
        return System.currentTimeMillis() - lastMention < cooldown;
    }
    
    //GET PERMISSION
    private String getPermission(String str) {
        return (str == null || str.isEmpty()) ? "" : str;
    }

    //ACTION BAR
    public static void sendActionBar(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = color(prefixedString(msg));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedText));
    }

    //PREFIX REPLACE
    private static String prefixedString(String str) {
        if(str == null || str.isEmpty()) return "";
        String prefix = config.get().getString("prefix", "");
        return str.replaceAll("\\{PREFIX}", prefix);
    }
    
}
