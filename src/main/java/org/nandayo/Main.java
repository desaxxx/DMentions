package org.nandayo;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.nandayo.utils.CooldownManager;
import org.nandayo.utils.UpdateChecker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nandayo.utils.HexUtil.color;

public final class Main extends JavaPlugin implements Listener {

    private static Main plugin;
    public static Main inst() {
        return plugin;
    }

    //MANAGERS
    public static ConfigManager configManager;
    public static MentionManager mentionManager;
    public static UserManager userManager;

    //SPIGOT RESOURCE ID
    private final int resourceId = 121452;
    private final UpdateChecker updateChecker = new UpdateChecker(this, resourceId);

    //AFTER LOAD PERMISSIONS
    private static final List<String> afterLoadPermissions = new ArrayList<>();

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
        new UpdateChecker(this, resourceId).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("Plugin is up-to-date.");
            } else {
                getLogger().info("There is a new version update. (" + version + ")");
            }
        });
    }

    @Override
    public void onDisable() {
        userManager.saveChanges();
    }

    public static void updateVariables() {
        //MANAGERS
        Config config = new Config();
        configManager = new ConfigManager(config.get());
        mentionManager = new MentionManager(configManager);
        if(userManager != null) {
            userManager.saveChanges();
        }
        userManager = new UserManager();
        //PERMISSION
        clearAfterLoadPermissions();
        setupPermissions();
    }

    //PERMISSION SETUP
    public static void setupPermissions() {
        String playerPermission = getPermission(configManager.getString("player.permission", "dmentions.mention.player"));
        String everyonePermission = getPermission(configManager.getString("everyone.permission", "dmentions.mention.everyone"));
        String nearbyPermission = getPermission(configManager.getString("nearby.permission", "dmentions.mention.nearby"));

        Bukkit.getPluginManager().addPermission(new Permission(playerPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(nearbyPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(everyonePermission, PermissionDefault.OP));
        afterLoadPermissions.add(playerPermission);
        afterLoadPermissions.add(nearbyPermission);
        afterLoadPermissions.add(everyonePermission);

        if (LP.isConnected()) {
            Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
            if(adminPermission == null) return;

            Map<String, Boolean> children = new HashMap<>();
            children.put(playerPermission, true);
            children.put(everyonePermission, true);

            for (String group : LP.getGroups()) {
                String groupPermission = getPermission(configManager.getString("group.permission", null)).replace("{group}", group);
                // REGISTERING GROUP PERMISSIONS
                Bukkit.getPluginManager().addPermission(new Permission(groupPermission, PermissionDefault.OP));
                afterLoadPermissions.add(groupPermission);
                // ADDING GROUP PERMISSIONS AS CHILDREN TO 'dmentions.admin'
                children.put(groupPermission, true);
            }
            adminPermission.getChildren().putAll(children);
            adminPermission.recalculatePermissibles();
        }
    }
    public static void clearAfterLoadPermissions() {
        if(afterLoadPermissions.isEmpty()) return;
        for(String perm : afterLoadPermissions) {
            Permission permission = Bukkit.getPluginManager().getPermission(perm);
            if(permission != null) {
                Bukkit.getPluginManager().removePermission(permission);
            }
            Permission adminPermission = Bukkit.getPluginManager().getPermission("dmentions.admin");
            if(adminPermission != null) {
                adminPermission.getChildren().remove(perm);
                adminPermission.recalculatePermissibles();
            }
        }
        afterLoadPermissions.clear();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        mentionManager.addPlayer(event.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        mentionManager.removePlayer(event.getPlayer());
        CooldownManager.playerCooldown.remove(event.getPlayer().getName());
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

                if (mh.getType() == MentionType.PLAYER) {
                    Player target = Bukkit.getPlayerExact(mh.getTarget());
                    if (target != null && userManager.getMentionMode(target) && !CooldownManager.playerIsOnCooldown(sender,keyword) && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.playerCooldown.put(target.getName(), System.currentTimeMillis());
                        displayText = configManager.getString("player.display", "<#a9e871>{p}&f").replace("{p}", target.getName());
                        mentionCounter++;

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionPlayerEvent event = new MentionPlayerEvent(sender, target);
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.NEARBY) {
                    if (!CooldownManager.nearbyIsOnCooldown(sender) && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.nearbyCooldown.put(sender.getName(), System.currentTimeMillis());
                        displayText = configManager.getString("nearby.display", "<#ea79b8>@nearby&f");
                        mentionCounter++;

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionNearbyEvent event = new MentionNearbyEvent(sender, getNearbyPlayers(sender));
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.EVERYONE) {
                    if (!CooldownManager.everyoneIsOnCooldown(sender) && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.everyoneCooldown = System.currentTimeMillis();
                        displayText = configManager.getString("everyone.display", "<#8fb56c>@everyone&f");
                        mentionCounter++;

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionEveryoneEvent event = new MentionEveryoneEvent(sender, Bukkit.getOnlinePlayers().toArray(new Player[0]));
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.GROUP && LP.isConnected()) {
                    String group = mh.getTarget();
                    ConfigurationSection section = getGroupSection(group);
                    if (!CooldownManager.groupIsOnCooldown(sender,mh.getTarget()) && section != null && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.groupCooldown.put(group, System.currentTimeMillis());
                        //Getting from group section
                        displayText = section.getString("display", "<#73c7dc>{group}&f").replace("{group}", group);
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


    //CHAT MESSAGE
    public static void sendMessage(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = color(prefixedString(msg));
        player.sendMessage(formattedText);
    }
    //ACTION BAR
    public static void sendActionBar(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = color(prefixedString(msg));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedText));
    }
    //TITLE
    public static void sendTitle(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String[] lines = msg.split("\\|\\|");
        String title = color(prefixedString(lines[0]));
        String subtitle = lines.length > 1 ? color(prefixedString(lines[1])) : "";

        player.sendTitle(title, subtitle, 10, 30, 20);
    }

    //PREFIX REPLACE
    public static String prefixedString(String str) {
        if(str == null || str.isEmpty()) return "";
        String prefix = configManager.getString("prefix", "");
        return str.replaceAll("\\{PREFIX}", prefix);
    }

    //GROUP CONFIG SECTION
    public static ConfigurationSection getGroupSection(String groupName) {
        if(groupName == null || groupName.isEmpty()) return null;
        if(configManager.getStringList("group.disabled_groups").contains(groupName)) return null;

        ConfigurationSection section = configManager.getConfigurationSection("group.list." + groupName);
        if(section != null) {
            return section;
        }else {
            return configManager.getConfigurationSection("group.list.__OTHER__");
        }
    }

    //GET NEARBY PLAYERS
    public static Player[] getNearbyPlayers(Player player) {
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
    public static String getPermission(String str) {
        return (str == null || str.isEmpty()) ? "" : str;
    }

    //FORMATTED TIME
    public static String formattedTime(long millisecond) {
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
