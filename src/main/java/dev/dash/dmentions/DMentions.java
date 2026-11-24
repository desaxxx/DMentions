package dev.dash.dmentions;

import com.tcoded.folialib.FoliaLib;
import dev.dash.dmentions.command.DMentionsCommand;
import dev.dash.dmentions.command.sub.ReloadSubCommand;
import dev.dash.dmentions.command.sub.SendSubCommand;
import dev.dash.dmentions.command.sub.ToggleSubCommand;
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
import dev.dash.dmentions.mention.type.EveryoneMentionType;
import dev.dash.dmentions.mention.type.GroupMentionType;
import dev.dash.dmentions.mention.type.NearbyMentionType;
import dev.dash.dmentions.mention.type.PlayerMentionType;
import dev.dash.dmentions.message.MessageService;
import dev.dash.dmentions.notify.MentionNotifier;
import dev.dash.dmentions.permission.PermissionRegistrar;
import dev.dash.dmentions.storage.SqliteUserRepository;
import dev.dash.dmentions.storage.UserRepository;
import dev.dash.dmentions.storage.YamlUserRepository;
import dev.dash.dmentions.user.UserListener;
import dev.dash.dmentions.user.UserService;
import dev.yoshi.xconfig.XConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class DMentions extends JavaPlugin {

    private PermissionRegistrar permissionRegistrar;
    private FoliaLib foliaLib;
    private UserService userService;

    private XConfig config;
    private MessageService messageService;
    private MentionRegistry mentionRegistry;
    private DMentionsCommand command;

    @Override
    public void onEnable() {

        try {
            config = XConfig.builder(getDataPath().resolve("config.yml")).keys(ConfigKeys.class).logger(getLogger()).build();
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

        CooldownTracker cooldownTracker = new CooldownTracker();
        PendingMentions pendingMentions = new PendingMentions();
        messageService = new MessageService(config, lang);
        MentionNotifier mentionNotifier = new MentionNotifier(foliaLib.getScheduler(), messageService, config, userService);
        mentionRegistry = new MentionRegistry(
                new EveryoneMentionType(config),
                new NearbyMentionType(config),
                new GroupMentionType(config, groupProvider),
                new PlayerMentionType(config, userService)
        );
        getServer().getPluginManager().registerEvents(new ChatListener(config, cooldownTracker, pendingMentions, mentionNotifier, mentionRegistry), this);

        getServer().getPluginManager().registerEvents(new UserListener(userService), this);
        // players already online
        for (Player player : getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            foliaLib.getScheduler().runAsync(task -> userService.load(playerId));
        }

        permissionRegistrar = new PermissionRegistrar(getServer().getPluginManager());
        permissionRegistrar.register(mentionRegistry.types());

        command = new DMentionsCommand(List.of(
                new ToggleSubCommand(messageService, userService),
                new SendSubCommand(config, messageService, mentionRegistry),
                new ReloadSubCommand(messageService, this::reload),
                new VersionSubCommand(messageService, getPluginMeta().getVersion())
        ), messageService);
        permissionRegistrar.registerCommands(command.subCommands());
        command.register(getLifecycleManager());
    }

    @Override
    public void onDisable() {
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
        config = null;
        messageService = null;
        mentionRegistry = null;
        command = null;
    }

    public boolean reload() {
        if (!isEnabled()) return false;
        try {
            config.update();
            saveBundledLanguages();
            messageService.setLang(loadLang());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not reload the configuration.", e);
            return false;
        }

        permissionRegistrar.unregisterAll();
        permissionRegistrar.register(mentionRegistry.types());
        permissionRegistrar.registerCommands(command.subCommands());
        return true;
    }

    private XConfig loadLang() {
        String langFile = config.get(ConfigKeys.LANG_FILE);
        if (!getDataPath().resolve("lang/" + langFile + ".yml").toFile().exists() && !langFile.equals("en-US")) {
            getLogger().warning("Could not find language file '%s', using default 'en-US'.".formatted(langFile));
            langFile = "en-US";
        }
        return XConfig.builder(getDataPath().resolve("lang/" + langFile + ".yml")).keys(LangKeys.class).logger(getLogger()).build();
    }

    private UserRepository prepareUserRepository(XConfig config) {
        String sqlTablePrefix = config.get(ConfigKeys.STORAGE_PREFIX);
        try {
            UserRepository repository = switch (config.get(ConfigKeys.STORAGE_TYPE)) {
                case YAML -> new YamlUserRepository(getDataPath().resolve("data/users.yml"), getLogger());
                case SQLITE -> new SqliteUserRepository(getDataPath().resolve("data/users.db"), sqlTablePrefix);
                case MYSQL -> throw new UnsupportedOperationException("MySQL storage is not supported yet.");
            };
            repository.init();

            return repository;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not initialize user repository, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return null;
        }
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

    private void saveBundledLanguages() {
        try (JarFile jarFile = new JarFile(getFile())) {
            jarFile.stream()
                    .filter(entry -> !entry.isDirectory() && entry.getName().startsWith("lang/") && entry.getName().endsWith(".yml"))
                    .forEach(entry -> {
                        if (getDataPath().resolve(entry.getName()).toFile().exists()) return;

                        saveResource(entry.getName(), false);
                    });
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save bundled languages.", e);
        }
    }
}
