package org.nandayo;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.nandayo.data.UserManager;
import org.nandayo.integration.LP;
import org.nandayo.mention.Events.MentionEveryoneEvent;
import org.nandayo.mention.Events.MentionGroupEvent;
import org.nandayo.mention.Events.MentionNearbyEvent;
import org.nandayo.mention.Events.MentionPlayerEvent;
import org.nandayo.mention.MentionHolder;
import org.nandayo.mention.MentionManager;
import org.nandayo.mention.MentionType;
import org.nandayo.mention.PluginEvents;
import org.nandayo.utils.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nandayo.utils.HexUtil.color;

public final class Main extends JavaPlugin implements Listener {

    private static Main plugin;
    public static Main inst() {
        return plugin;
    }

    //SPIGOT RESOURCE ID
    private final int resourceId = 121452;

    //AFTER LOAD PERMISSIONS
    public final List<String> afterLoadPermissions = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new PluginEvents(), this);
        getCommand("dmentions").setExecutor(new MainCommand());

        //APIS
        if(pm.getPlugin("LuckPerms") != null) {
            new LP(this);
            getLogger().info("LuckPerms has been enabled");
        }else {
            getLogger().info("LuckPerms not found. Skipping group mentions.");
        }

        //UPDATE VALUES
        updateVariables();

        //UPDATE CHECK
        if(configManager.getBoolean("check_for_updates", true)) {
            new UpdateChecker(this, resourceId).getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    getLogger().info("Plugin is up-to-date.");
                } else {
                    getLogger().info("There is a new version update. (" + version + ")");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        userManager.saveChanges();
    }

    //MANAGERS
    public ConfigManager configManager;
    public MentionManager mentionManager;
    public UserManager userManager;
    public CooldownManager cooldownManager;
    public LangManager langManager;

    public void updateVariables() {
        //MANAGERS
        Config config = new Config(this);
        configManager = new ConfigManager(config.get());
        mentionManager = new MentionManager(this, configManager);
        if(userManager != null) {
            userManager.saveChanges();
        }
        userManager = new UserManager(this);
        cooldownManager = new CooldownManager(this, configManager);
        langManager = new LangManager(this, configManager.getString("lang_file", "en-US"));

        //PERMISSION
        PermissionManager permissionManager = new PermissionManager(this, configManager);
        permissionManager.clearAfterLoadPermissions();
        permissionManager.setupPermissions();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        mentionManager.addPlayer(event.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        mentionManager.removePlayer(event.getPlayer());
        cooldownManager.removeLastPlayerMention(event.getPlayer().getName());
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String message = e.getMessage();
        if(e.isCancelled()) return;

        Pattern mentionPattern = mentionManager.getMentionPattern();
        Matcher matcher = mentionPattern.matcher(message);

        StringBuilder updatedMessage = new StringBuilder();
        int lastAppendPosition = 0;

        int mentionLimit = configManager.getInt("mention_limit", 2);
        int mentionCounter = 0;

        while (matcher.find()) {
            if(mentionCounter >= mentionLimit) {
                break;
            }
            String keyword = matcher.group(1);
            MentionHolder mh = mentionManager.getMentionHolder(keyword);

            if (mh != null) {
                int matchStart = matcher.start();
                int matchEnd = matcher.end();

                updatedMessage.append(message, lastAppendPosition, matchStart);
                String displayText = keyword;
                String suffix = getSuffixColor(LP.getGroup(sender));

                if (mh.getType() == MentionType.PLAYER) {
                    Player target = Bukkit.getPlayerExact(mh.getTarget());
                    if (target != null && userManager.getMentionMode(target) && !cooldownManager.playerIsOnCooldown(sender,keyword) && sender.hasPermission(mh.getPerm())) {
                        if(getRestrictConditions(sender, target)) {
                            cooldownManager.setLastPlayerMention(target.getName(), System.currentTimeMillis());
                            displayText = configManager.getString("player.display", "<#a9e871>{p}&f").replace("{p}", target.getName()) + suffix;
                            mentionCounter++;

                            Bukkit.getScheduler().runTask(this, () -> {
                                MentionPlayerEvent event = new MentionPlayerEvent(sender, target);
                                Bukkit.getPluginManager().callEvent(event);
                            });
                        }else {
                            String msg = langManager.getMsg("mention_restricted_warn");
                            new MessageManager(configManager).sendSortedMessage(sender, msg);
                        }
                    }
                } else if (mh.getType() == MentionType.NEARBY) {
                    if (!cooldownManager.nearbyIsOnCooldown(sender) && sender.hasPermission(mh.getPerm())) {
                        cooldownManager.setLastNearbyMention(sender.getName(), System.currentTimeMillis());
                        displayText = configManager.getString("nearby.display", "<#ea79b8>@nearby&f") + suffix;
                        mentionCounter++;

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionNearbyEvent event = new MentionNearbyEvent(sender, getNearbyPlayers(sender));
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.EVERYONE) {
                    if (!cooldownManager.everyoneIsOnCooldown(sender) && sender.hasPermission(mh.getPerm())) {
                        cooldownManager.setLastEveryoneMention(System.currentTimeMillis());
                        displayText = configManager.getString("everyone.display", "<#8fb56c>@everyone&f") + suffix;
                        mentionCounter++;

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionEveryoneEvent event = new MentionEveryoneEvent(sender, Bukkit.getOnlinePlayers().toArray(new Player[0]));
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.GROUP && LP.isConnected()) {
                    String group = mh.getTarget();
                    ConfigurationSection section = getConfigGroupSection(group);
                    if (!cooldownManager.groupIsOnCooldown(sender, mh.getTarget()) && section != null && sender.hasPermission(mh.getPerm())) {
                        cooldownManager.setLastGroupMention(group, System.currentTimeMillis());
                        //Getting from group section
                        displayText = section.getString("display", "<#73c7dc>{group}&f").replace("{group}", group) + suffix;
                        mentionCounter++;

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionGroupEvent event = new MentionGroupEvent(sender, group, LP.getOnlinePlayersInGroup(group));
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                }

                updatedMessage.append(color(displayText));
                lastAppendPosition = matchEnd;
            }
        }

        updatedMessage.append(message.substring(lastAppendPosition));
        e.setMessage(updatedMessage.toString());
    }

    //SUFFIX COLOR
    public String getSuffixColor(String groupName) {
        ConfigurationSection section = configManager.getConfigurationSection("suffix_color.group");
        if(section.contains(groupName)) {
            return section.getString(groupName, "");
        }
        return configManager.getString("suffix_color.group.__OTHER__", "&f");
    }

    //CHECK RESTRICTED PERMISSIONS
    public boolean getRestrictConditions(Player sender, Player target) {
        if(target.hasPermission("dmentions.mention.restricted")) {
            return sender.hasPermission("dmentions.mention.restricted.bypass");
        }
        return true;
    }

    //CONFIG GROUP SECTION
    public ConfigurationSection getConfigGroupSection(String groupName) {
        if(groupName == null || groupName.isEmpty()) return null;
        if(configManager.getStringList("group.disabled_groups").contains(groupName)) return null;

        ConfigurationSection section = configManager.getConfigurationSection("group.list." + groupName);
        if(section != null) {
            return section;
        }else {
            return configManager.getConfigurationSection("group.list.__OTHER__");
        }
    }

    //LANG GROUP SECTION
    public ConfigurationSection getLangGroupSection(String groupName) {
        if(groupName == null || groupName.isEmpty()) return null;

        ConfigurationSection section = langManager.getConfig().getConfigurationSection("group." + groupName);
        if(section != null) {
            return section;
        }else {
            return langManager.getConfig().getConfigurationSection("group.__OTHER__");
        }
    }

    //GET NEARBY PLAYERS
    private Player[] getNearbyPlayers(Player player) {
        int radius = configManager.getInt("nearby.radius", 20);
        if(radius <= 0) return new Player[0];

        Set<Player> nearbyPlayers = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player nearbyPlayer) {
                nearbyPlayers.add(nearbyPlayer);
            }
        }
        return nearbyPlayers.toArray(new Player[0]);
    }

    //GET PERMISSION
    public String getPermission(String str) {
        return (str == null || str.isEmpty()) ? "" : str;
    }

    //FORMATTED TIME
    public String formattedTime(long millisecond) {
        long seconds = millisecond / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        if (days > 0) return String.format("%d d, %d h", days, hours);
        if (hours > 0) return String.format("%d h, %d m", hours, minutes);
        if (minutes > 0) return String.format("%d m, %d s", minutes, seconds);
        return String.format("%d s", seconds);
    }
}
