package dev.dash.dmentions;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.listener.ChatListener;
import dev.dash.dmentions.mention.CooldownTracker;
import dev.dash.dmentions.mention.PendingMentions;
import dev.dash.dmentions.notify.MentionNotifier;
import dev.yoshi.xconfig.XConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class DMentions extends JavaPlugin {

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

        CooldownTracker cooldownTracker = new CooldownTracker();
        PendingMentions pendingMentions = new PendingMentions();
        MentionNotifier mentionNotifier = new MentionNotifier(this, config);
        getServer().getPluginManager().registerEvents(new ChatListener(config, cooldownTracker, pendingMentions, mentionNotifier), this);
    }
}
