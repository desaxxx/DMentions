package org.nandayo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigManager {

    private final FileConfiguration config;
    private final ConcurrentMap<String, Object> cachedValues = new ConcurrentHashMap<>();

    public ConfigManager(FileConfiguration config) {
        this.config = config;
        reloadCache();
    }

    public void reloadCache() {
        cachedValues.clear();
        for (String key : config.getKeys(true)) {
            cachedValues.put(key, config.get(key));
        }
    }

    //UNSPECIFIED TYPES
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) cachedValues.getOrDefault(key, defaultValue);
    }

    //COMMON TYPES
    public String getString(String key, String defaultValue) {
        Object value = cachedValues.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = cachedValues.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        Object value = cachedValues.get(key);
        if(value instanceof Long) {
            return (Long) value;
        }else if(value instanceof Integer) {
            return ((Integer) value).longValue();
        }else {
            return defaultValue;
        }
    }

    public List<String> getStringList(String key) {
        return config.isList(key) ? config.getStringList(key) : new ArrayList<>();
    }

    //CONFIGURATION SECTION
    public ConfigurationSection getConfigurationSection(String key) {
        return config.getConfigurationSection(key);
    }
}
