package org.nandayo;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.nandayo.integration.LP;
import org.nandayo.mention.Events.MentionEveryoneEvent;
import org.nandayo.mention.Events.MentionGroupEvent;
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
    }

    public static void updateVariables() {
        //MANAGERS
        Config config = new Config();
        configManager = new ConfigManager(config.get());
        mentionManager = new MentionManager(configManager);
        //PERMISSION
        clearAfterLoadPermissions();
        setupPermissions();
    }

    //PERMISSION SETUP
    public static void setupPermissions() {
        String playerPermission = getPermission(configManager.getString("player.permission", "dmentions.mention.player"));
        String everyonePermission = getPermission(configManager.getString("everyone.permission", "dmentions.mention.everyone"));

        Bukkit.getPluginManager().addPermission(new Permission(playerPermission, PermissionDefault.OP));
        Bukkit.getPluginManager().addPermission(new Permission(everyonePermission, PermissionDefault.OP));
        afterLoadPermissions.add(playerPermission);
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
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String message = e.getMessage();

        Pattern mentionPattern = mentionManager.getMentionPattern();
        Matcher matcher = mentionPattern.matcher(message);

        StringBuilder updatedMessage = new StringBuilder();
        int lastAppendPosition = 0;

        while (matcher.find()) {
            String keyword = matcher.group(1);
            MentionHolder mh = mentionManager.getMentionHolder(keyword);

            if (mh != null) {
                int matchStart = matcher.start();
                int matchEnd = matcher.end();

                updatedMessage.append(message, lastAppendPosition, matchStart);
                String displayText = keyword;

                if (mh.getType() == MentionType.PLAYER) {
                    Player target = Bukkit.getPlayerExact(mh.getTarget());
                    if (target != null && !CooldownManager.playerIsOnCooldown(keyword) && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.playerCooldown.put(target.getName(), System.currentTimeMillis());
                        displayText = configManager.getString("player.display", "&a{p}&f").replace("{p}", target.getName());

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionPlayerEvent event = new MentionPlayerEvent(sender, target);
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.EVERYONE) {
                    if (!CooldownManager.everyoneIsOnCooldown() && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.everyoneCooldown = System.currentTimeMillis();
                        displayText = configManager.getString("everyone.display", "&2@everyone&f");

                        Bukkit.getScheduler().runTask(this, () -> {
                            MentionEveryoneEvent event = new MentionEveryoneEvent(sender, Bukkit.getOnlinePlayers().toArray(new Player[0]));
                            Bukkit.getPluginManager().callEvent(event);
                        });
                    }
                } else if (mh.getType() == MentionType.GROUP && LP.isConnected()) {
                    String group = mh.getTarget();
                    ConfigurationSection section = getGroupSection(group);
                    if (!CooldownManager.groupIsOnCooldown(mh.getTarget()) && section != null && sender.hasPermission(mh.getPerm())) {
                        CooldownManager.groupCooldown.put(group, System.currentTimeMillis());
                        //Getting from group section
                        displayText = section.getString("display", "&b{group}&f").replace("{group}", group);

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


    //ACTION BAR
    public static void sendActionBar(Player player, String msg) {
        if(msg == null || msg.isEmpty()) return;
        String formattedText = color(prefixedString(msg));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedText));
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

    //GET PERMISSION
    public static String getPermission(String str) {
        return (str == null || str.isEmpty()) ? "" : str;
    }
}
