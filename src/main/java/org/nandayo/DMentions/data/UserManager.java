package org.nandayo.DMentions.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nandayo.DMentions.Main;

import java.io.File;
import java.io.IOException;

public class UserManager implements IUser{

    private final String ns = "players.";
    private final File file;
    private final FileConfiguration config;

    private final Main plugin;

    public UserManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(this.plugin.getDataFolder(), "players.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveChanges() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save players.yml");
            e.printStackTrace();
        }
    }


    @Override
    public boolean getMentionMode(Player player) {
        return config.getBoolean(ns + player.getUniqueId() + ".mentions", true);
    }

    @Override
    public void setMentionMode(Player player, boolean mode) {
        config.set(ns + player.getUniqueId() + ".mentions", mode);
    }

    @Override
    public String getMentionDisplay(Player player) {
        return config.getString(ns + player.getUniqueId() + ".mention_display", player.getName());
    }

    @Override
    public void setMentionDisplay(Player player, String display) {
        config.set(ns + player.getUniqueId() + ".mention_display", display);
    }
}
