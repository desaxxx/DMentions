package org.nandayo.dmentions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.dapi.Util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("CallToPrintStackTrace")
public class Config {

    public FileConfiguration get() {
        return config;
    }

    private static File file;
    private static FileConfiguration config;

    private final DMentions plugin;

    public Config(DMentions plugin) {
        this.plugin = plugin;
        file = new File(this.plugin.getDataFolder(), "config.yml");
        if(!file.exists()) {
            this.plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /*
     * Update config
     */
    public Config updateConfig() {
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("config_version", "0");

        if(version.equals(configVersion)) return this;

        InputStream defStream = plugin.getResource("config.yml");
        if(defStream == null) {
            Util.log("&cDefault config.yml not found in plugin resources.");
            return this;
        }

        // Backup old config
        saveBackupConfig();

        // Value pasting from old config
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        for(String key : defConfig.getKeys(true)) {
            if(key.startsWith("suffix_color.group") || key.startsWith("disabled_worlds") || key.startsWith("group.disabled_groups") || key.startsWith("group.list")) {
                continue; // These will be handled separately
            }
            if (defConfig.isConfigurationSection(key)) {
                continue; // Skip parent keys
            }
            if(config.contains(key)) {
                defConfig.set(key, config.get(key));
            }
        }

        // Suffix color section     (Special Case I)
        defConfig.set("suffix_color.group", null); // clear default
        ConfigurationSection suffixSection = config.getConfigurationSection("suffix_color.group");
        if(suffixSection != null) {
            for(String group : suffixSection.getKeys(true)) {
                defConfig.set(suffixSection.getCurrentPath() + "." + group, suffixSection.getString(group));
            }
        }

        // Disabled worlds list     (Special Case II)
        defConfig.set("disabled_worlds", config.getStringList("disabled_worlds"));

        // Disabled groups list     (Special Case III)
        defConfig.set("group.disabled_groups", config.getStringList("group.disabled_groups"));

        // Group list section       (Special Case IV)
        ConfigurationSection groupSection = config.getConfigurationSection("group.list");
        ConfigurationSection keySection = defConfig.getConfigurationSection("group.list.default");
        final Set<String> keys = (keySection != null) ? keySection.getKeys(true) : new HashSet<>();
        defConfig.set("group.list", null); // clear default
        if(groupSection != null) {
            for(String group : groupSection.getKeys(false)) {
                for(String key : keys) {
                    if(config.isConfigurationSection(group + "." + key)) {
                        continue; // Skip parent keys
                    }
                    defConfig.set(groupSection.getCurrentPath() + "." + group + "." + key, groupSection.get(group + "." + key));
                }
            }
        }

        // Save new config
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

    private void saveBackupConfig() {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            backupDir.mkdirs();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(backupDir, "config_" + date + ".yml");
        try {
            config.save(backupFile);
            Util.log("&aBacked up old config file.");
        } catch (Exception e) {
            Util.log("&cFailed to save backup file.");
            e.printStackTrace();
        }
    }
}
