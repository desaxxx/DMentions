package dev.dash.dmentions;

import com.tcoded.folialib.FoliaLib;
import dev.dash.dmentions.command.DMentionsCommand;
import dev.dash.dmentions.command.sub.ConfigSubCommand;
import dev.dash.dmentions.command.sub.CustomizeSubCommand;
import dev.dash.dmentions.command.sub.ReloadSubCommand;
import dev.dash.dmentions.command.sub.SendSubCommand;
import dev.dash.dmentions.command.sub.ToggleSubCommand;
import dev.dash.dmentions.command.sub.UserSubCommand;
import dev.dash.dmentions.command.sub.VersionSubCommand;
import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.group.GroupProvider;
import dev.dash.dmentions.group.LuckPermsGroupProvider;
import dev.dash.dmentions.group.NoGroupProvider;
import dev.dash.dmentions.listener.ChatListener;
import dev.dash.dmentions.mention.CooldownTracker;
import dev.dash.dmentions.mention.MentionRegistry;
import dev.dash.dmentions.mention.PendingMentions;
import dev.dash.dmentions.mention.TargetFilter;
import dev.dash.dmentions.mention.type.EveryoneMentionType;
import dev.dash.dmentions.mention.type.GroupMentionType;
import dev.dash.dmentions.mention.type.NearbyMentionType;
import dev.dash.dmentions.mention.type.PlayerMentionType;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.notify.MentionNotifier;
import dev.dash.dmentions.permission.PermissionRegistrar;
import dev.dash.dmentions.status.EssentialsStatusProvider;
import dev.dash.dmentions.status.NoPlayerStatusProvider;
import dev.dash.dmentions.status.PlayerStatusProvider;
import dev.dash.dmentions.storage.*;
import dev.dash.dmentions.user.UserListener;
import dev.dash.dmentions.user.UserService;
import dev.yoshi.xconfig.WritableXConfig;
import dev.yoshi.xconfig.XConfig;
import dev.dash.dmentions.menu.ConfigEditor;
import dev.dash.dmentions.menu.ConfigMenus;
import dev.dash.dmentions.metrics.LanguageMetric;
import dev.dash.loom.Loom;
import dev.dash.dmentions.update.UpdateChecker;
import dev.dash.dmentions.update.UpdateNotifyListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class DMentions extends JavaPlugin {

    private PermissionRegistrar permissionRegistrar;
    private FoliaLib foliaLib;
    private UserService userService;

    private WritableXConfig config;
    private MessageService messageService;
    private MentionRegistry mentionRegistry;
    private DMentionsCommand command;

    private ConfigMenus configMenus;
    private Metrics metrics;
    private volatile Set<String> bundledLanguages = Set.of(DEFAULT_LANGUAGE);
    private volatile String loadedLanguage;

    private static final String DEFAULT_LANGUAGE = "en-US";

    @Override
    public void onEnable() {

        try {
            config = XConfig.builder(getDataPath().resolve("config.yml")).keys(ConfigKeys.class).logger(getLogger()).buildWritable();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveBundledLanguages();

        XConfig lang;
        try {
            lang = loadLang();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load lang file, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        UserRepository repository = prepareUserRepository(config);
        if (repository == null) return; // Disabled the plugin
        userService = new UserService(repository, getLogger());

        foliaLib = new FoliaLib(this);

        GroupProvider groupProvider = fetchGroupProvider(config);
        PlayerStatusProvider statusProvider = fetchStatusProvider(config);

        CooldownTracker cooldownTracker = new CooldownTracker();
        PendingMentions pendingMentions = new PendingMentions();
        messageService = new MessageService(config, lang);
        TargetFilter targetFilter = new TargetFilter(config, userService, statusProvider);
        MentionNotifier mentionNotifier = new MentionNotifier(foliaLib.getScheduler(), messageService, config, targetFilter);
        mentionRegistry = new MentionRegistry(targetFilter,
                new EveryoneMentionType(config),
                new NearbyMentionType(config),
                new GroupMentionType(config, groupProvider),
                new PlayerMentionType(config, userService)
        );
        getServer().getPluginManager().registerEvents(new ChatListener(config, cooldownTracker, pendingMentions, mentionNotifier, mentionRegistry), this);

        getServer().getPluginManager().registerEvents(new UserListener(userService), this);
        // players already online
        // Captured once: the field is cleared in onDisable, which may run before these tasks do.
        UserService service = userService;
        for (Player player : getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            foliaLib.getScheduler().runAsync(task -> service.load(playerId));
        }

        permissionRegistrar = new PermissionRegistrar(getServer().getPluginManager());
        permissionRegistrar.register(mentionRegistry.types());

        Loom.install(this);
        configMenus = new ConfigMenus(new ConfigEditor(config, this::applyConfig, getLogger()), groupProvider,
                getDataPath().resolve("lang"), this::reload);

        command = new DMentionsCommand(List.of(
                new ToggleSubCommand(messageService, userService),
                new CustomizeSubCommand(messageService, userService),
                new UserSubCommand(messageService, userService),
                new SendSubCommand(config, messageService, mentionRegistry),
                new ConfigSubCommand(messageService, configMenus),
                new ReloadSubCommand(messageService, this::reload),
                new VersionSubCommand(messageService, getPluginMeta().getVersion())
        ), messageService);
        permissionRegistrar.registerCommands(command.subCommands());
        command.register(getLifecycleManager());

        this.metrics = new Metrics(this, 24381);
        String storageType = config.get(ConfigKeys.STORAGE_TYPE).name();
        metrics.addCustomChart(new SimplePie("storage_type", () -> storageType));
        metrics.addCustomChart(new SimplePie("language", () -> {
            String language = loadedLanguage;
            return language == null ? null : LanguageMetric.valueFor(language, bundledLanguages);
        }));

        startUpdateCheck();
    }

    /** Checked once per server start; /dms reload does not repeat it. */
    private void startUpdateCheck() {
        if (!config.get(ConfigKeys.CHECK_FOR_UPDATES) || Boolean.getBoolean("dmentions.skipUpdateCheck")) return;

        UpdateChecker updateChecker = new UpdateChecker(getLogger(), getPluginMeta().getVersion());
        getServer().getPluginManager().registerEvents(new UpdateNotifyListener(updateChecker, messageService), this);
        updateChecker.check();
    }

    @Override
    public void onDisable() {
        // closes open menus first, while the config they show is still there
        Loom.uninstall();
        configMenus = null;
        if (userService != null) {
            userService.shutdown();
            userService = null;
        }
        if (permissionRegistrar != null) {
            permissionRegistrar.unregisterAll();
            permissionRegistrar = null;
        }
        if (foliaLib != null) {
            foliaLib.getScheduler().cancelAllTasks();
            foliaLib = null;
        }
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        config = null;
        messageService = null;
        mentionRegistry = null;
        command = null;
    }

    public boolean reload() {
        if (!isEnabled()) return false;
        try {
            config.update();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not reload the configuration.", e);
            return false;
        }
        return applyConfig();
    }

    // applies what depends on the loaded config; the menus call only this, since config.set already loaded the change
    public boolean applyConfig() {
        if (!isEnabled()) return false;
        try {
            saveBundledLanguages();
            messageService.setLang(loadLang());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not reload the language file.", e);
            return false;
        }

        permissionRegistrar.unregisterAll();
        permissionRegistrar.register(mentionRegistry.types());
        permissionRegistrar.registerCommands(command.subCommands());
        configMenus.refresh();
        return true;
    }

    private XConfig loadLang() {
        String langFile = config.get(ConfigKeys.LANG_FILE);
        if (!getDataPath().resolve("lang/" + langFile + ".yml").toFile().exists() && !langFile.equals("en-US")) {
            getLogger().warning("Could not find language file '%s', using default 'en-US'.".formatted(langFile));
            langFile = "en-US";
        }
        XConfig lang = XConfig.builder(getDataPath().resolve("lang/" + langFile + ".yml")).keys(LangKeys.class).logger(getLogger()).build();
        loadedLanguage = langFile;
        return lang;
    }

    private UserRepository prepareUserRepository(XConfig config) {
        String sqlTablePrefix = config.get(ConfigKeys.STORAGE_PREFIX);
        try {
            UserRepository repository = switch (config.get(ConfigKeys.STORAGE_TYPE)) {
                case YAML -> new YamlUserRepository(getDataPath().resolve("data/users.yml"), getLogger());
                case SQLITE -> new SqliteUserRepository(getDataPath().resolve("data/users.db"), sqlTablePrefix);
                case MYSQL -> new MySqlUserRepository(settings(config), sqlTablePrefix);
            };
            repository.init();

            return repository;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not initialize user repository, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return null;
        }
    }

    private MySqlSettings settings(XConfig config) {
        return new MySqlSettings(
                config.get(ConfigKeys.STORAGE_HOST),
                config.get(ConfigKeys.STORAGE_PORT),
                config.get(ConfigKeys.STORAGE_NAME),
                config.get(ConfigKeys.STORAGE_USERNAME),
                config.get(ConfigKeys.STORAGE_PASSWORD),
                config.get(ConfigKeys.STORAGE_USE_SSL)
        );
    }

    private GroupProvider fetchGroupProvider(XConfig config) {
        GroupProvider provider = getServer().getServicesManager().load(GroupProvider.class);
        if (provider == null && getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                provider = new LuckPermsGroupProvider();
            } catch (IllegalStateException e) {
                getLogger().log(Level.WARNING, "LuckPerms found but was not loaded yet.", e);
            }
        }

        if (provider == null) {
            provider = new NoGroupProvider();
            if (config.find(ConfigKeys.GROUP_ENABLED).orElse(false)) {
                getLogger().warning("Group mentioning is enabled but no group provider was found.");
            }
        }

        return provider;
    }

    private PlayerStatusProvider fetchStatusProvider(XConfig config) {
        PlayerStatusProvider provider = getServer().getServicesManager().load(PlayerStatusProvider.class);
        if (provider == null && getServer().getPluginManager().isPluginEnabled("Essentials")) {
            try {
                provider = new EssentialsStatusProvider();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Essentials found but failed to setup.", e);
            }
        }

        if (provider == null) {
            provider = new NoPlayerStatusProvider();
            // ignore_respect is on by default, so only the opt-in afk_respect is worth a warning
            if (config.get(ConfigKeys.AFK_RESPECT)) {
                getLogger().warning("afk_respect is enabled but Essentials was not found; AFK players can still be mentioned.");
            }
        }
        return provider;
    }

    private void saveBundledLanguages() {
        Set<String> names = new HashSet<>();
        names.add(DEFAULT_LANGUAGE);
        try (JarFile jarFile = new JarFile(getFile())) {
            jarFile.stream()
                    .filter(entry -> !entry.isDirectory() && entry.getName().startsWith("lang/") && entry.getName().endsWith(".yml"))
                    .forEach(entry -> {
                        String name = entry.getName();
                        names.add(name.substring("lang/".length(), name.length() - ".yml".length()));
                        if (getDataPath().resolve(name).toFile().exists()) return;

                        saveResource(name, false);
                    });
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save bundled languages.", e);
        }
        bundledLanguages = Set.copyOf(names);
    }
}
