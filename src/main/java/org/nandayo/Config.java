package org.nandayo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.utils.Util;

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
    }

    //UPDATE CONFIG
    public Config updateConfig() {
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("config_version", "0");

        if(version.equals(configVersion)) return this;

        InputStream defStream = plugin.getResource("config.yml");
        if(defStream == null) {
            Util.log("&cDefault config.yml not found in plugin resources.");
            return this;
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

        // NEW CONFIG ADAPTER
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        for(String key : defConfig.getKeys(true)) {
            if(backupConfig.contains(key)) {
                defConfig.set(key, backupConfig.get(key));
            }
        }

        try {
            defConfig.set("config_version", version);
            defConfig.save(file);
            config = defConfig;
            Util.log("&aUpdated config file.");
        }catch (Exception e) {
            Util.log("&cFailed to save updated config file.");
            e.printStackTrace();
        }
        return this;
    }

    private void saveBackupConfig(File backupFile, FileConfiguration backupConfig) {
        try {
            backupConfig.save(backupFile);
            Util.log("&aBacked up old config file.");
        } catch (Exception e) {
            Util.log("&cFailed to save backup file.");
            e.printStackTrace();
        }
    }
}
