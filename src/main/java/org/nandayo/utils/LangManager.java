package org.nandayo.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.Main;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LangManager {

    private final List<String> languages = Arrays.asList("en-US","tr-TR");
    private final Main plugin;

    private final String defaultLang = "en-US";

    private final String selectedLang;
    private final File file;
    private FileConfiguration config;

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
        this.selectedLang = fileName;
        this.file = new File(dir, fileName + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        updateLanguage();
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

    //UPDATE LANGUAGE
    private void updateLanguage() {
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("lang_version", "0");

        if(version.equals(configVersion)) return;

        InputStream defStream = plugin.getResource("lang/" + selectedLang + ".yml");
        if(defStream == null) {
            plugin.getLogger().warning("Default " + selectedLang + ".yml not found in plugin resources.");
            return;
        }

        // BACKUP OF OLD LANG.YML
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(plugin.getDataFolder(), "backups/lang_" + selectedLang + "_" + date + ".yml");
        saveBackupConfig(backupFile, config);
        FileConfiguration backupConfig = YamlConfiguration.loadConfiguration(backupFile);

        // NEW CONFIG ADAPTER
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        for(String key : defConfig.getKeys(true)) {
            if(backupConfig.contains(key)) {
                defConfig.set(key, backupConfig.get(key));
            }
        }

        try {
            defConfig.set("lang_version", version);
            defConfig.save(file);
            config = defConfig;
            plugin.updateVariables();
            plugin.getLogger().info("Updated language file.");
        }catch (Exception e) {
            plugin.getLogger().warning("Failed to save updated language file.");
            e.printStackTrace();
        }
    }

    private void saveBackupConfig(File backupFile, FileConfiguration backupConfig) {
        try {
            backupConfig.save(backupFile);
            plugin.getLogger().info("Backed up old language file.");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save old language backup file.");
            e.printStackTrace();
        }
    }
}
