package org.nandayo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("CallToPrintStackTrace")
public class Config {

    public FileConfiguration get() {
        return config;
    }

    private static File file;
    private static FileConfiguration config;

    private final Main plugin;

    public Config(Main plugin) {
        this.plugin = plugin;
        file = new File(this.plugin.getDataFolder(), "config.yml");
        if(!file.exists()) {
            this.plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        updateFileKeys();
    }

    //UPDATE KEYS
    private void updateFileKeys() {
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("config_version");

        if(version.equals(configVersion)) {
            return;
        }
        InputStream defStream = plugin.getResource("config.yml");
        if(defStream == null) {
            plugin.getLogger().warning("Default config.yml not found in plugin resources.");
            return;
        }

        // BACKUP OF OLD CONFIG.YML
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(plugin.getDataFolder(), "backups/config_" + date + ".yml");
        saveBackupConfig(backupFile, config);
        FileConfiguration backupConfig = YamlConfiguration.loadConfiguration(backupFile);

        // CLEAR CONFIG
        file.delete();
        file = new File(plugin.getDataFolder(), "config.yml");
        if(!file.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        // NEW CONFIG ADAPTER
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        for(String key : defConfig.getKeys(true)) {
            if(backupConfig.contains(key)) {
                config.set(key, backupConfig.get(key));
            }
        }

        try {
            config.set("config_version", version);
            config.save(file);
            plugin.updateVariables();
            plugin.getLogger().info("Updated config file.");
        }catch (Exception e) {
            plugin.getLogger().warning("Failed to save updated config file.");
            e.printStackTrace();
        }
    }

    private void saveBackupConfig(File backupFile, FileConfiguration backupConfig) {
        try {
            backupConfig.save(backupFile);
            plugin.getLogger().info("Backed up old config file.");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save backup file.");
            e.printStackTrace();
        }
    }
}
