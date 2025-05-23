package org.nandayo.dmentions;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.DAPI;
import org.nandayo.dapi.Util;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dmentions.data.UserManager;
import org.nandayo.dmentions.integration.LP;
import org.nandayo.dmentions.integration.LPEvents;
import org.nandayo.dmentions.mention.MentionManager;
import org.nandayo.dmentions.mention.MentionType;
import org.nandayo.dmentions.mention.PluginEvents;
import org.nandayo.dmentions.service.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DMentions extends JavaPlugin implements Listener {

    private static DMentions plugin;
    public static DMentions inst() {
        return plugin;
    }

    //AFTER LOAD PERMISSIONS
    public final List<String> afterLoadPermissions = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new PluginEvents(), this);
        DAPI dapi = new DAPI(plugin);
        dapi.registerMenuListener();
        Util.PREFIX = "&7[&eDMentions&7]&r ";

        Objects.requireNonNull(getCommand("dmentions")).setExecutor(new MainCommand());

        //APIS
        if(pm.getPlugin("LuckPerms") != null) {
            new LP(this);
            new LPEvents(this, LP.getApi()).register();
            Util.log("&aLuckPerms found. Integration has been enabled!");
        }else {
            Util.log("&fLuckPerms not found. Skipping integration.");
        }

        //UPDATE VALUES
        updateVariables();

        //UPDATE CHECK
        if(CONFIG_MANAGER.getBoolean("check_for_updates", true)) {
            //SPIGOT RESOURCE ID
            new UpdateChecker(this, 121452).getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    Util.log("&aPlugin is up-to-date.");
                } else {
                    Util.log("&fThere is a new version update. (&e" + version + "&f)");
                }
            });
        }

        //bStats
        new Metrics(this, 24381);
    }

    @Override
    public void onDisable() {
        USER_MANAGER.saveChanges();
    }

    //MANAGERS
    public Wrapper WRAPPER;
    public Config CONFIG;
    public ConfigManager CONFIG_MANAGER;
    public MentionManager MENTION_MANAGER;
    public UserManager USER_MANAGER;
    public CooldownManager COOLDOWN_MANAGER;
    public LanguageManager LANGUAGE_MANAGER;
    public PermissionManager PERMISSION_MANAGER = null;
    public Player GUI_CONFIG_EDITOR = null;

    public void updateVariables() {
        //MANAGERS
        WRAPPER = new Wrapper(this);
        CONFIG = new Config(this).updateConfig();
        CONFIG_MANAGER = new ConfigManager(CONFIG.get());
        MENTION_MANAGER = new MentionManager(this);
        if(USER_MANAGER != null) {
            USER_MANAGER.saveChanges();
        }
        USER_MANAGER = new UserManager(this);
        COOLDOWN_MANAGER = new CooldownManager(this);
        COOLDOWN_MANAGER.updateConfigCooldowns();
        LANGUAGE_MANAGER = new LanguageManager(this, new File(getDataFolder(), "lang"), CONFIG_MANAGER.getString("lang_file","en-US"));

        //PERMISSION
        if(PERMISSION_MANAGER == null) {
            PERMISSION_MANAGER = new PermissionManager(this);
            PERMISSION_MANAGER.clearAfterLoadPermissions();
            PERMISSION_MANAGER.setupPermissions();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MENTION_MANAGER.addPlayer(event.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MENTION_MANAGER.removePlayer(event.getPlayer());
        COOLDOWN_MANAGER.removeCooldown(MentionType.PLAYER, event.getPlayer().getName());
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String message = e.getMessage();
        if(e.isCancelled()) return;

        List<String> disabledWorlds = CONFIG_MANAGER.getStringList("disabled_worlds");
        if(disabledWorlds != null && disabledWorlds.contains(sender.getWorld().getName())) {
            new MessageManager(CONFIG_MANAGER).sendSortedMessage(sender, (String) LANGUAGE_MANAGER.getMessage("disabled_world_warn"));
            return;
        }
        String mentionedString = MENTION_MANAGER.getMentionedString(this, sender, message);
        e.setMessage(mentionedString);
    }

    /**
     * Get cross-version material
     *
     * @param dMaterial DMaterial
     * @param def Default DMaterial
     * @return Material
     */
    public Material getMaterial(@NotNull DMaterial dMaterial, @NotNull DMaterial def) {
        Material mat = dMaterial.get();
        if (mat != null) return mat;
        else return def.get();
    }

    /**
     * Get cross-version enchantment
     * @param dEnchantment DEnchantment
     * @param def Default DEnchantment
     * @return Enchantment
     */
    public Enchantment getEnchantment(@NotNull DEnchantment dEnchantment, @NotNull DEnchantment def) {
        Enchantment mat = dEnchantment.get();
        if (mat != null) return mat;
        else return def.get();
    }

    //CHECK RESTRICTED PERMISSIONS
    public boolean isRestricted(@NotNull Player sender, @NotNull Player target) {
        if(target.hasPermission("dmentions.mention.restricted")) {
            return !sender.hasPermission("dmentions.mention.restricted.bypass");
        }
        return false;
    }

    //CONFIG GROUP SECTION
    public ConfigurationSection getConfigGroupSection(@NotNull String groupName) {
        if(groupName.isEmpty()) return null;
        if(CONFIG_MANAGER.getStringList("group.disabled_groups").contains(groupName)) return null;
        return CONFIG_MANAGER.getConfigurationSection("group.list." + getGroupConfigTitle(groupName));
    }

    //LANG GROUP SECTION
    public ConfigurationSection getLanguageGroupSection(@NotNull String groupName) {
        return LANGUAGE_MANAGER.getSection("group." + getGroupConfigTitle(groupName));
    }

    /**
     * Get group's config title within "group.list"
     * @param groupName Group name
     * @return Group name if found, or __OTHER__
     */
    @NotNull
    public String getGroupConfigTitle(@Nullable String groupName) {
        if(groupName == null) return "__OTHER__";
        ConfigurationSection section = CONFIG_MANAGER.getConfigurationSection("group.list");
        if(section == null || !section.contains(groupName)) return "__OTHER__";
        return groupName;
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

    public int parseInt(@NotNull String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
