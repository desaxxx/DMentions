package org.nandayo.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nandayo.Main;

import java.io.File;
import java.io.IOException;

public class UserManager implements IUser{

    private final String ns = "players.";
    private final File file;
    private final FileConfiguration config;

    public UserManager() {
        this.file = new File(Main.inst().getDataFolder(), "players.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveChanges() {
        try {
            config.save(file);
        } catch (IOException e) {
            Main.inst().getLogger().warning("Could not save players.yml");
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
}