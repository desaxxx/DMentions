package org.nandayo.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.Main;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LangManager {

    private final List<String> languages = Arrays.asList("en-US","tr-TR");
    private final Main plugin;

    private final String defaultLang = "en-US";
    private final File file;
    private final FileConfiguration config;

    public LangManager(Main plugin, String fileName) {
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(), "lang");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        loadFiles();
        if(!languages.contains(fileName)) {
            plugin.getLogger().warning("Language " + fileName + " not found. Using default language.");
            fileName = defaultLang;
        }
        this.file = new File(dir, fileName + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadFiles() {
        for(String lang : languages) {
            String path = "lang/" + lang + ".yml";
            File file = new File(plugin.getDataFolder(), path);
            if(!file.exists() && plugin.getResource(path) != null) {
                plugin.saveResource(path, false);
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMsg(String path) {
        Object obj = config.get(path, "");
        if(obj instanceof String) {
            return obj.toString();
        }
        return "";
    }
    public String getMsg(ConfigurationSection section, String path) {
        Object obj = section.get(path, "");
        if(obj instanceof String) {
            return obj.toString();
        }
        return "";
    }
}
