package org.nandayo.DMentions.service;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.DAPI.Util;
import org.nandayo.DMentions.DMentions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.StringReader;

public class ConfigManager {

    @Getter
    private YamlConfiguration config; // Main config
    private YamlConfiguration uConfig; // Unsaved config

    public ConfigManager(FileConfiguration config) {
        this.config = YamlConfiguration.loadConfiguration(new StringReader(config.saveToString()));
        this.uConfig = YamlConfiguration.loadConfiguration(new StringReader(config.saveToString()));
    }

    /*
     * SETTER
     */
    public void set(String key, Object value, boolean isGuiConfig) {
        if (isGuiConfig) {
            uConfig.set(key, value);
        } else {
            config.set(key, value);
        }
    }

    /*
     * GETTERS
     */
    public Object get(String key, Object defaultValue, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.get(key, defaultValue) : config.get(key, defaultValue);
    }
    public Object get(String key, Object defaultValue) {
        return get(key, defaultValue, false);
    }

    public String getString(String key, String defaultValue, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.getString(key, defaultValue) : config.getString(key, defaultValue);
    }
    public String getString(String key, String defaultValue) {
        return getString(key, defaultValue, false);
    }

    public int getInt(String key, int defaultValue, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.getInt(key, defaultValue) : config.getInt(key, defaultValue);
    }
    public int getInt(String key, int defaultValue) {
        return getInt(key, defaultValue, false);
    }

    public long getLong(String key, long defaultValue, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.getLong(key, defaultValue) : config.getLong(key, defaultValue);
    }
    public long getLong(String key, long defaultValue) {
        return getLong(key, defaultValue, false);
    }

    public boolean getBoolean(String key, boolean defaultValue, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.getBoolean(key, defaultValue) : config.getBoolean(key, defaultValue);
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, false);
    }

    public ConfigurationSection getConfigurationSection(String key, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.getConfigurationSection(key) : config.getConfigurationSection(key);
    }
    public ConfigurationSection getConfigurationSection(String key) {
        return getConfigurationSection(key, false);
    }

    public List<String> getStringList(String key, boolean isGuiConfig) {
        return isGuiConfig ? uConfig.getStringList(key) : config.getStringList(key);
    }
    public List<String> getStringList(String key) {
        return getStringList(key, false);
    }


    /*
     * GUI SAVE AND RESET
     */
    public void saveGuiConfig() {
        try {
            config = uConfig;
            config.save(new File(DMentions.inst().getDataFolder(), "config.yml"));
            DMentions.inst().updateVariables();
        } catch (IOException e) {
            Util.log("Could not save GUI Config. " + e.getMessage());
        }
    }

    public void resetGuiConfig() {
        uConfig = YamlConfiguration.loadConfiguration(new StringReader(config.saveToString()));
    }
}