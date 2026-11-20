package dev.dash.dmentions;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.listener.ChatListener;
import dev.yoshi.xconfig.XConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class DMentions extends JavaPlugin {

    @Override
    public void onEnable() {

        XConfig config;
        try {
            config = XConfig.builder(getDataPath().resolve("config.yml")).keys(ConfigKeys.class).logger(getLogger()).build();
            getServer().getPluginManager().registerEvents(new ChatListener(config), this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml, disabling the plugin.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }
}
