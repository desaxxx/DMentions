package org.nandayo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {

    private final File file;
    private final FileConfiguration config;

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

    public void updateFileKeys() {
        InputStream defStream = Main.inst().getResource("config.yml");
        if(defStream == null) {
            Main.inst().getLogger().warning("Default config.yml not found in plugin resources.");
            return;
        }
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));

        boolean changed = false;
        for(String key : config.getKeys(true)) {
            if(!defConfig.contains(key)) {
                config.set(key, null);
                changed = true;
            }
        }
        for(String key : defConfig.getKeys(true)) {
            if(!config.contains(key)) {
                config.set(key, defConfig.get(key));
                changed = true;
            }
        }

        if(changed) {
            try {
                config.save(file);
                Main.config = new Config();
                Main.inst().getLogger().info("Updated config file.");
            }catch (Exception e) {
                Main.inst().getLogger().warning("Failed to save updated config file.");
                e.printStackTrace();
            }
        }
    }
}
