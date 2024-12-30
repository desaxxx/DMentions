package org.nandayo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Config {

    public FileConfiguration get() {
        return config;
    }

    private static File file;
    private static FileConfiguration config;

    public Config() {
        file = new File(Main.inst().getDataFolder(), "config.yml");
        if(!file.exists()) {
            Main.inst().saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        updateFileKeys();
    }

    //UPDATE KEYS
    public static void updateFileKeys() {
        String version = Main.inst().getDescription().getVersion();
        String configVersion = config.getString("config_version");

        if(version.equals(configVersion)) {
            return;
        }
        InputStream defStream = Main.inst().getResource("config.yml");
        if(defStream == null) {
            Main.inst().getLogger().warning("Default config.yml not found in plugin resources.");
            return;
        }

        // BACKUP OF OLD CONFIG.YML
        File backupDir = new File(Main.inst().getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(Main.inst().getDataFolder(), "backups/config_" + date + ".yml");
        saveBackupConfig(backupFile, config);
        FileConfiguration backupConfig = YamlConfiguration.loadConfiguration(backupFile);

        // CLEAR CONFIG
        file.delete();
        file = new File(Main.inst().getDataFolder(), "config.yml");
        if(!file.exists()) {
            Main.inst().saveResource("config.yml", false);
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
            Main.updateVariables();
            Main.inst().getLogger().info("Updated config file.");
        }catch (Exception e) {
            Main.inst().getLogger().warning("Failed to save updated config file.");
            e.printStackTrace();
        }
    }

    private static void saveBackupConfig(File backupFile, FileConfiguration backupConfig) {
        try {
            backupConfig.save(backupFile);
            Main.inst().getLogger().info("Backed up old config file.");
        } catch (Exception e) {
            Main.inst().getLogger().warning("Failed to save backup file.");
            e.printStackTrace();
        }
    }
}
