package org.nandayo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    private final File file;
    private FileConfiguration config;

    public Config() {
        this.file = new File(Main.inst().getDataFolder(), "config.yml");
        if(!file.exists()) {
            Main.inst().saveResource("config.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }
}
