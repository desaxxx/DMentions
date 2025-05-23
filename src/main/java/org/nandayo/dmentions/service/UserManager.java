package org.nandayo.dmentions.service;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nandayo.dmentions.DMentions;

import java.io.File;
import java.io.IOException;

public class UserManager {

    private final String ns = "players.";
    private final File file;
    private final FileConfiguration config;

    private final DMentions plugin;

    public UserManager(DMentions plugin) {
        this.plugin = plugin;
        this.file = new File(this.plugin.getDataFolder(), "players.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveChanges() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save players.yml. " + e.getMessage());
        }
    }


    public boolean getMentionMode(Player player) {
        return config.getBoolean(ns + player.getUniqueId() + ".mentions", true);
    }

    public void setMentionMode(Player player, boolean mode) {
        config.set(ns + player.getUniqueId() + ".mentions", mode);
    }

    public String getMentionDisplay(Player player) {
        return config.getString(ns + player.getUniqueId() + ".mention_display", player.getName());
    }

    public void setMentionDisplay(Player player, String display) {
        config.set(ns + player.getUniqueId() + ".mention_display", display);
    }
}
