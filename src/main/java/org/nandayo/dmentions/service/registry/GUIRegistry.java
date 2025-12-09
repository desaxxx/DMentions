package org.nandayo.dmentions.service.registry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.configuration.YAMLRegistry;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @since 1.8.3
 */
public class GUIRegistry extends YAMLRegistry {
    private static final SimpleDateFormat backupDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private @NotNull FileConfiguration config;
    public GUIRegistry(@NotNull DMentions plugin) {
        super(plugin);
        this.config = getConfiguration();
    }

    @Override
    protected @NotNull String filePath() {
        return "gui.yml";
    }

    private boolean loaded;
    @Override
    public boolean isLoaded() {
        return loaded;
    }
    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void onLoad() {
        setLoaded(true);
    }

    @Override
    public void onUnload() {
        setLoaded(false);
    }


    /**
     * Update gui file.
     * @return Updated FileConfiguration
     */
    public GUIRegistry updateConfiguration() {
        String version = getPlugin().getDescription().getVersion();
        String configVersion = config.getString("gui_version", "0");
        if(version.equals(configVersion)) return this;

        FileConfiguration defConfig = getSourceConfiguration();
        if (defConfig == null) return this;

        // Backup old config
        saveBackupConfig(config);

        // Value pasting from old config
        for(String key : defConfig.getKeys(true)) {
            if (defConfig.isConfigurationSection(key)) {
                continue; // Skip parent keys
            }
            if(config.contains(key)) {
                defConfig.set(key, config.get(key));
            }
        }

        defConfig.set("gui_version", getPlugin().getDescription().getVersion());
        config = defConfig;
        try {
            config.save(getFile());
            Util.log("&aUpdated gui file.");
        }catch (Exception e) {
            Util.log("&cFailed to save updated gui file. " + e.getMessage());
        }
        return this;
    }


    /**
     * Get the default configuration from source of plugin.
     * @return FileConfiguration
     */
    private FileConfiguration getSourceConfiguration() {
        InputStream defStream = getPlugin().getResource(filePath());
        if(defStream == null) {
            Util.log("&cDefault '" + filePath() + "' was not found in plugin resources.");
            return null;
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
    }

    /**
     * Save backup of old gui config.
     */
    private void saveBackupConfig(@NotNull FileConfiguration config) {
        File backupDir = new File(getPlugin().getDataFolder(), "backups");
        if (!backupDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            backupDir.mkdirs();
        }
        String date = backupDateFormat.format(new Date());
        File backupFile = new File(backupDir, "gui_" + date + ".yml");
        try {
            config.save(backupFile);
            Util.log("&aBacked up old gui file.");
        } catch (Exception e) {
            Util.log("&cFailed to save old gui backup file. ");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }



    /**
     * Get ConfigurationSection from {@link #config} with given path.
     * @param path Path
     * @return ConfigurationSection
     * @since 1.8.3
     */
    @Nullable
    public ConfigurationSection getSection(@NotNull String path) {
        return config.getConfigurationSection(path);
    }

    /**
     * Get String from {@link #config} with given path.
     * @param path Path
     * @return String
     * @since 1.8.3
     */
    @NotNull
    public String getString(@NotNull String path) {
        String str = config.getString(path);
        if(str == null) {
            Util.log("&cNull message at path '" + path + "'");
            return "";
        }
        return str;
    }

    /**
     * Get String from given section and given sub path.
     * @param section ConfigurationSection
     * @param path Path
     * @return String
     * @since 1.8.3
     */
    @NotNull
    public String getString(@Nullable ConfigurationSection section, @NotNull String path) {
        final String currentPath = section == null || section.getCurrentPath() == null ? "" : section.getCurrentPath();
        return getString(currentPath + "." + path);
    }

    /**
     * Get List of String from {@link #config} with given path.
     * @param path Path
     * @return List of String
     * @since 1.8.3
     */
    @NotNull
    public List<String> getStringList(@NotNull String path) {
        return config.getStringList(path);
    }

    /**
     * Get List of String from given section and given sub path.
     * @param section Section
     * @param path Path
     * @return List of String
     * @since 1.8.3
     */
    @NotNull
    public List<String> getStringList(@Nullable ConfigurationSection section, @NotNull String path) {
        final String currentPath = section == null || section.getCurrentPath() == null ? "" : section.getCurrentPath();
        return getStringList(currentPath + "." + path);
    }
}
