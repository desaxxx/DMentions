package org.nandayo.dmentions.service;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.HexUtil;
import org.nandayo.dapi.Util;
import org.nandayo.dmentions.DMentions;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("CallToPrintStackTrace")
@Getter
public class Config {

    private FileConfiguration config;
    private FileConfiguration unsavedConfig;

    private final @NotNull DMentions plugin;
    public Config(@NotNull DMentions plugin) {
        this.plugin = plugin;
        File file = new File(this.plugin.getDataFolder(), "config.yml");
        if(!file.exists()) {
            this.plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        resetUnsavedConfig();
    }

    /*
     * Update configuration file.
     */
    public Config updateConfig() {
        if(compareVersions()) return this;

        FileConfiguration defConfig = getSourceConfiguration();
        if(defConfig == null) return this;

        // Backup old config
        saveBackupConfig();

        // Value pasting from old config
        for(String key : defConfig.getKeys(true)) {
            if(key.startsWith("suffix_color.group") ||
                    key.startsWith("disabled_worlds") ||
                    key.startsWith("group.disabled_groups") ||
                    key.startsWith("group.list")) {
                continue; // These will be handled separately within special cases.
            }
            if (defConfig.isConfigurationSection(key)) {
                continue; // Skip parent keys
            }
            if(config.contains(key)) {
                defConfig.set(key, config.get(key));
            }
        }

        handleSpecialCases(defConfig);

        defConfig.set("config_version", plugin.getDescription().getVersion());
        config = defConfig;
        resetUnsavedConfig();
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            Util.log("&aUpdated config file.");
        }catch (IOException e) {
            Util.log("&cFailed to save updated config file.");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Compare plugin and configuration file versions.
     * @return whether they match or not.
     */
    private boolean compareVersions() {
        String pluginVersion = plugin.getDescription().getVersion();
        String configVersion = config.getString("config_version", "0");
        return pluginVersion.equals(configVersion);
    }

    /**
     * Get the default configuration from source of plugin.
     * @return FileConfiguration
     */
    private FileConfiguration getSourceConfiguration() {
        InputStream defStream = plugin.getResource("config.yml");
        if(defStream == null) {
            Util.log("&cDefault config.yml not found in plugin resources.");
            return null;
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
    }

    /**
     * Save the old configuration to backups folder.
     */
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
            Util.log("&cFailed to save old config backup file.");
            e.printStackTrace();
        }
    }

    /**
     * Handle special cases of updating the configuration.
     * @param defConfig config from plugin source
     */
    private void handleSpecialCases(@NotNull FileConfiguration defConfig) {
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
    }


    /**
     * Reset the unsaved config to current config.
     */
    public void resetUnsavedConfig(CommandSender sender) {
        unsavedConfig = YamlConfiguration.loadConfiguration(new StringReader(config.saveToString()));
        if(sender != null) {
            sender.sendMessage(HexUtil.color(plugin.getLanguageManager().getString("command.config.reset_changes")));
        }
    }

    public void resetUnsavedConfig() {
        resetUnsavedConfig(null);
    }

    /**
     * Save the unsaved config.
     */
    public void saveUnsavedConfig(CommandSender sender) {
        config = YamlConfiguration.loadConfiguration(new StringReader(unsavedConfig.saveToString()));
        if(sender != null) {
            sender.sendMessage(HexUtil.color(plugin.getLanguageManager().getString("command.config.save_changes")));
            Util.log("&eUpdated config keys in-game by player " + sender.getName() + ".");
        }
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.updateVariables();
        } catch (IOException e) {
            Util.log("&cFailed to save old config file.");
        }
    }

    public void saveUnsavedConfig() {
        saveUnsavedConfig(null);
    }

    /**
     * Check if a path value is changed.
     * @param path String
     * @return whether changed or not.
     */
    public boolean isValueChanged(@NotNull String path) {
        return !Objects.equals(config.get(path), unsavedConfig.get(path));
    }

    /**
     * Get display message with replacements.
     * @param message String
     * @param configPath String
     * @return Replaced message
     */
    public String getValueDisplayMessage(@NotNull String message, @NotNull String configPath) {
        return message.replace("{value}", config.getString(configPath,""))
                .replace("{unsaved_value}", unsavedConfig.getString(configPath,""));
    }
}
