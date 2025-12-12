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
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dapi.configuration.YAMLRegistry;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.command.MainCommand;
import org.nandayo.dmentions.integration.EssentialsHook;
import org.nandayo.dmentions.integration.LuckPermsHook;
import org.nandayo.dmentions.integration.StaffPPHook;
import org.nandayo.dmentions.module.ModuleManager;
import org.nandayo.dmentions.provider.VanishProvider;
import org.nandayo.dmentions.user.SingleFolderMigrator;
import org.nandayo.dmentions.user.UserListener;
import org.nandayo.dmentions.user.UserManager;
import org.nandayo.dmentions.integration.LPEvents;
import org.nandayo.dmentions.service.MentionManager;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.event.PluginEvents;
import org.nandayo.dmentions.service.*;
import org.nandayo.dmentions.service.message.Message;
import org.nandayo.dmentions.service.registry.GUIRegistry;
import org.nandayo.dmentions.util.DUtil;

import java.util.List;
import java.util.Objects;

@Getter
public final class DMentions extends JavaPlugin implements Listener {

    private Wrapper wrapper;
    private Config configuration;
    private MentionManager mentionManager;
    private UserManager userManager;
    private CooldownManager cooldownManager;
    private LanguageManager languageManager;
    private GUIRegistry guiRegistry;
    private PermissionManager permissionManager = null;
    @Setter
    private Player guiConfigEditor = null;
    private EssentialsHook essentialsHook;
    private LuckPermsHook luckPermsHook;
    private StaffPPHook staffPPHook;

    private VanishProvider vanishProvider;

    private static DMentions plugin;
    public static DMentions inst() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(PluginEvents.INSTANCE, this);
        pm.registerEvents(UserListener.INSTANCE, this);

        Objects.requireNonNull(getCommand("dmentions")).setExecutor(new MainCommand());

        setupDAPI();

        setupIntegrations();

        updateVariables();

        setupProviders();

        SingleFolderMigrator.migrate();

        /*
         Method call order:
         onEnable()
         onUserLogin()
         */
        ModuleManager.INSTANCE.loadModules();
        userManager.registerAll();

        UpdateChecker.INSTANCE.check(this);

        //bStats
        new Metrics(this, 24381);
    }

    @Override
    public void onDisable() {
        /*
         Method call order:
         onUserLogout()
         onDisable()
         */
        userManager.unregisterAll();
        ModuleManager.INSTANCE.unloadModules();
    }

    private void setupDAPI() {
        DAPI.registerMenuListener();
        Util.PREFIX = "&7[&eDMentions&7]&r ";
    }

    private void setupIntegrations() {
        luckPermsHook = new LuckPermsHook(this);
        if(!luckPermsHook.isMaskNull()) {
            new LPEvents(plugin, luckPermsHook).register();
            Util.log("&aLuckPerms integration has been enabled. Make sure you are using v5.1 or newer.");
        }
        essentialsHook = new EssentialsHook(this);
        if(!essentialsHook.isMaskNull()) {
            Bukkit.getPluginManager().registerEvents(new EssentialsHook.EssentialsListener(this), this);
            Util.log("&aEssentialsX integration has been enabled. Make sure you are using v2.19.2 or newer.");
        }
        staffPPHook = new StaffPPHook(this);
        if(!staffPPHook.isMaskNull()) {
            Bukkit.getPluginManager().registerEvents(new StaffPPHook.StaffPlusPlusListener(this), this);
            Util.log("&aStaff++ integration has been enabled.");
        }
    }

    public void updateVariables() {
        //MANAGERS
        wrapper = new Wrapper(this);
        configuration = new Config(this).updateConfig();
        mentionManager = new MentionManager(this);
        if(userManager != null) {
            userManager.saveAllToFile();
        }
        userManager = new UserManager();
        cooldownManager = new CooldownManager(this);
        cooldownManager.updateConfigCooldowns();

        languageManager = new LanguageManager(this, configuration.getConfig().getString("lang_file","en-US"));
        guiRegistry = new GUIRegistry(this).updateConfiguration();
        YAMLRegistry.loadRegistries();
        Message.init(languageManager);

        //PERMISSION
        if(permissionManager == null) {
            permissionManager = new PermissionManager(this);
            permissionManager.clearAfterLoadPermissions();
            permissionManager.setupPermissions();
        }
    }

    private void setupProviders() {
        String vanishProvider = configuration.getConfig().getString("vanish_provider", "auto");
        VanishProvider.Type type = VanishProvider.Type.find(vanishProvider);
        boolean isAuto = type == VanishProvider.Type.AUTO;
        if(!staffPPHook.isMaskNull() && (isAuto || type == VanishProvider.Type.STAFFPLUSPLUS)) {
            this.vanishProvider = staffPPHook;
            Util.log("&aUsing Staff++ as VanishProvider.");
        }
        else if(!essentialsHook.isMaskNull() && (isAuto || type == VanishProvider.Type.ESSENTIALS)) {
            this.vanishProvider = essentialsHook;
            Util.log("&aUsing EssentialsX as VanishProvider.");
        }
        else {
            this.vanishProvider = player -> false;
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

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        String message = e.getMessage();

        List<String> disabledWorlds = configuration.getConfig().getStringList("disabled_worlds");
        String worldName = sender.getWorld().getName();
        if(disabledWorlds.contains(worldName)) {
            Message.DISABLED_WORLD_WARN
                    .replaceValue("{world}", worldName)
                    .sendMessage(sender);
            return;
        }
        String mentionedString = mentionManager.getMentionedString(this, sender, message);
        e.setMessage(mentionedString);
    }




    @Deprecated(since = "1.9", forRemoval = true)
    private void loadOnlinePlayers() {}

    /**
     * @deprecated in favor of {@link DUtil#getMaterial(DMaterial, DMaterial)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    public Material getMaterial(@NotNull DMaterial dMaterial, @NotNull DMaterial def) {
        return DUtil.getMaterial(dMaterial, def);
    }

    /**
     * @deprecated in favor of {@link DUtil#getEnchantment(DEnchantment, DEnchantment)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    public Enchantment getEnchantment(@NotNull DEnchantment dEnchantment, @NotNull DEnchantment def) {
        return DUtil.getEnchantment(dEnchantment, def);
    }

    /**
     * @deprecated in favor of {@link DUtil#isRestricted(Player, Player)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    public boolean isRestricted(@NotNull Player sender, @NotNull Player target) {
        return DUtil.isRestricted(sender, target);
    }

    /**
     * @deprecated in favor of {@link DUtil#getGroupConfigSection(String)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    @Nullable
    public ConfigurationSection getConfigGroupSection(String groupName) {
        return DUtil.getGroupConfigSection(groupName);
    }

    /**
     * @deprecated in favor of {@link DUtil#getGroupLanguageSection(String)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    @Nullable
    public ConfigurationSection getLanguageGroupSection(String groupName) {
        return DUtil.getGroupLanguageSection(groupName);
    }

    /**
     * @deprecated in favor of {@link DUtil#getGroupConfigKey(String)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    @NotNull
    public String getGroupConfigTitle(@Nullable String groupName) {
        return DUtil.getGroupConfigKey(groupName);
    }

    /**
     * @deprecated redundant
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    @NotNull
    public String getPermission(String str) {
        return str == null ? "" : str;
    }

    /**
     * @deprecated in favor of {@link DUtil#formattedTime(long)}.
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    @NotNull
    public String formattedTime(long millisecond) {
        return DUtil.formattedTime(millisecond);
    }

    /**
     * @deprecated in favor of {@link DUtil#parseInt(String, int)}
     */
    @Deprecated(since = "1.8.3", forRemoval = true)
    public int parseInt(String str) {
        return DUtil.parseInt(str,0);
    }
}
