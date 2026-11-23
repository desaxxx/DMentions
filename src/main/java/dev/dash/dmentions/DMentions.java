package dev.dash.dmentions;

import com.tcoded.folialib.FoliaLib;
import dev.dash.dmentions.command.DMentionsCommand;
import dev.dash.dmentions.command.sub.ReloadSubCommand;
import dev.dash.dmentions.command.sub.SendSubCommand;
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
import dev.yoshi.xconfig.XConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class DMentions extends JavaPlugin {

    private PermissionRegistrar permissionRegistrar;
    private FoliaLib foliaLib;

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

        foliaLib = new FoliaLib(this);

        GroupProvider groupProvider = fetchGroupProvider(config);

        CooldownTracker cooldownTracker = new CooldownTracker();
        PendingMentions pendingMentions = new PendingMentions();
        messageService = new MessageService(config, lang);
        MentionNotifier mentionNotifier = new MentionNotifier(foliaLib.getScheduler(), messageService, config);
        mentionRegistry = new MentionRegistry(
                new EveryoneMentionType(config),
                new NearbyMentionType(config),
                new GroupMentionType(config, groupProvider),
                new PlayerMentionType(config)
        );
        getServer().getPluginManager().registerEvents(new ChatListener(config, cooldownTracker, pendingMentions, mentionNotifier, mentionRegistry), this);

        permissionRegistrar = new PermissionRegistrar(getServer().getPluginManager());
        permissionRegistrar.register(mentionRegistry.types());

        command = new DMentionsCommand(List.of(
                new SendSubCommand(config, messageService, mentionRegistry),
                new ReloadSubCommand(messageService, this::reload),
                new VersionSubCommand(messageService, getPluginMeta().getVersion())
        ), messageService);
        permissionRegistrar.registerCommands(command.subCommands());
        command.register(getLifecycleManager());
    }

    @Override
    public void onDisable() {
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

    /**
     * Reloads config.yml and the language file in place: every component keeps its {@link XConfig},
     * only the values behind it change. Permission nodes come from the config, so they are re-registered.
     *
     * @return false if a file could not be read; the error is logged
     */
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

    /** Builds the language file named by {@code lang_file}, falling back to en-US if it does not exist. */
    private XConfig loadLang() {
        String langFile = config.get(ConfigKeys.LANG_FILE);
        if (!getDataPath().resolve("lang/" + langFile + ".yml").toFile().exists() && !langFile.equals("en-US")) {
            getLogger().warning("Could not find language file '%s', using default 'en-US'.".formatted(langFile));
            langFile = "en-US";
        }
        return XConfig.builder(getDataPath().resolve("lang/" + langFile + ".yml")).keys(LangKeys.class).logger(getLogger()).build();
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
