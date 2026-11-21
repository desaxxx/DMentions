package dev.dash.dmentions;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.config.LangKeys;
import dev.dash.dmentions.listener.ChatListener;
import dev.dash.dmentions.mention.CooldownTracker;
import dev.dash.dmentions.mention.MentionRegistry;
import dev.dash.dmentions.mention.PendingMentions;
import dev.dash.dmentions.mention.type.EveryoneMentionType;
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
import java.util.jar.JarFile;
import java.util.logging.Level;

public class DMentions extends JavaPlugin {

    private PermissionRegistrar permissionRegistrar;

    @Override
    public void onEnable() {

        XConfig config;
        try {
            config = XConfig.builder(getDataPath().resolve("config.yml")).keys(ConfigKeys.class).logger(getLogger()).build();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveBundledLanguages();

        String langFile = config.get(ConfigKeys.LANG_FILE);
        if (!getDataPath().resolve("lang/" + langFile + ".yml").toFile().exists() && !langFile.equals("en-US")) {
            getLogger().warning("Could not find language file '%s', using default 'en-US'.".formatted(langFile));
            langFile = "en-US";
        }
        XConfig lang;
        try {
            lang = XConfig.builder(getDataPath().resolve("lang/" + langFile + ".yml")).keys(LangKeys.class).logger(getLogger()).build();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load lang file, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        CooldownTracker cooldownTracker = new CooldownTracker();
        PendingMentions pendingMentions = new PendingMentions();
        MessageService messageService = new MessageService(config, lang);
        MentionNotifier mentionNotifier = new MentionNotifier(this, messageService);
        MentionRegistry mentionRegistry = new MentionRegistry(
                new EveryoneMentionType(config),
                new NearbyMentionType(config),
                new PlayerMentionType(config)
        );
        getServer().getPluginManager().registerEvents(new ChatListener(config, cooldownTracker, pendingMentions, mentionNotifier, mentionRegistry), this);

        permissionRegistrar = new PermissionRegistrar(getServer().getPluginManager());
        permissionRegistrar.register(mentionRegistry.types());
    }

    @Override
    public void onDisable() {
        if (permissionRegistrar != null) {
            permissionRegistrar.unregisterAll();
            permissionRegistrar = null;
        }
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
