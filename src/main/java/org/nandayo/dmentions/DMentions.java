package org.nandayo.dmentions;

import lombok.Getter;
import lombok.Setter;
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
import org.nandayo.dmentions.integration.EssentialsHook;
import org.nandayo.dmentions.service.UserManager;
import org.nandayo.dmentions.integration.LP;
import org.nandayo.dmentions.integration.LPEvents;
import org.nandayo.dmentions.service.MentionManager;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.event.PluginEvents;
import org.nandayo.dmentions.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
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

        Objects.requireNonNull(getCommand("dmentions")).setExecutor(new MainCommand());

        setupDAPI();

        setupIntegrations();

        updateVariables();

        //UPDATE CHECK
        if(configuration.getConfig().getBoolean("check_for_updates", true)) {
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
        userManager.saveChanges();
    }

    private void setupDAPI() {
        DAPI dapi = new DAPI(plugin);
        dapi.registerMenuListener();
        Util.PREFIX = "&7[&eDMentions&7]&r ";
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    private void setupIntegrations() {
        if(Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            new LP();
            new LPEvents(this, LP.getApi()).register();
            Util.log("&aLuckPerms integration has been enabled. Make sure you are using v5.1 or newer.");
        }
        if(Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            new EssentialsHook();
            Bukkit.getPluginManager().registerEvents(new EssentialsHook.EssentialsListener(this), this);
            Util.log("&aEssentialX integration has been enabled. Make sure you are using v2.19.2 or newer.");
        }
    }

    //MANAGERS
    private Wrapper wrapper;
    private Config configuration;
    private MentionManager mentionManager;
    private UserManager userManager;
    private CooldownManager cooldownManager;
    private LanguageManager languageManager;
    private PermissionManager permissionManager = null;
    @Setter
    private Player guiConfigEditor = null;

    public void updateVariables() {
        //MANAGERS
        wrapper = new Wrapper(this);
        configuration = new Config(this).updateConfig();
        mentionManager = new MentionManager(this);
        if(userManager != null) {
            userManager.saveChanges();
        }
        userManager = new UserManager(this);
        cooldownManager = new CooldownManager(this);
        cooldownManager.updateConfigCooldowns();
        languageManager = new LanguageManager(this, configuration.getConfig().getString("lang_file","en-US"));

        //PERMISSION
        if(permissionManager == null) {
            permissionManager = new PermissionManager(this);
            permissionManager.clearAfterLoadPermissions();
            permissionManager.setupPermissions();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        mentionManager.addPlayer(event.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        mentionManager.removePlayer(event.getPlayer());
        cooldownManager.removeCooldown(MentionType.PLAYER, event.getPlayer().getName());
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String message = e.getMessage();
        if(e.isCancelled()) return;

        List<String> disabledWorlds = configuration.getConfig().getStringList("disabled_worlds");
        if(disabledWorlds.contains(sender.getWorld().getName())) {
            MessageManager.sendSortedMessage(sender, languageManager.getString("disabled_world_warn"));
            return;
        }
        String mentionedString = mentionManager.getMentionedString(this, sender, message);
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
        Material mat = dMaterial.parseMaterial();
        if (mat != null) return mat;
        else return def.parseMaterial();
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
        if(configuration.getConfig().getStringList("group.disabled_groups").contains(groupName)) return null;
        return configuration.getConfig().getConfigurationSection("group.list." + getGroupConfigTitle(groupName));
    }

    //LANG GROUP SECTION
    public ConfigurationSection getLanguageGroupSection(@NotNull String groupName) {
        return languageManager.getSection("group." + getGroupConfigTitle(groupName));
    }

    /**
     * Get group's config title within "group.list"
     * @param groupName Group name
     * @return Group name if found, or __OTHER__
     */
    @NotNull
    public String getGroupConfigTitle(@Nullable String groupName) {
        if(groupName == null) return "__OTHER__";
        ConfigurationSection section = configuration.getConfig().getConfigurationSection("group.list");
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
