package org.nandayo.dmentions.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.Util;
import org.nandayo.dmentions.DMentions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class LanguageManager {

    private final @NotNull DMentions plugin;
    private final File folder;
    public LanguageManager(@NotNull DMentions plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "lang");
        if(!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }
        this.loadDefaultFiles();
        this.loadFiles(fileName);
    }

    // Default values
    public final List<String> REGISTERED_LANGUAGES = new ArrayList<>();
    private final List<String> DEFAULT_LANGUAGES = Arrays.asList("en-US","tr-TR","zh-CN");
    private final String DEFAULT_LANGUAGE = "en-US";
    private FileConfiguration DEFAULT_LANGUAGE_CONFIG;

    //
    private FileConfiguration SELECTED_LANGUAGE_CONFIG;

    /**
     * Load language files and setup default & selected language file configuration.
     * @param searchingFor File name of selected language
     */
    private void loadFiles(@NotNull String searchingFor) {
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files != null) {
            for(File file : files) {
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                REGISTERED_LANGUAGES.add(fileName);
                // Setup selected language file.
                if(fileName.equals(searchingFor)) {
                    this.SELECTED_LANGUAGE_CONFIG = (DEFAULT_LANGUAGES.contains(fileName)) ? updateLanguage(fileName) : YamlConfiguration.loadConfiguration(file);
                }
                // Setup default language file.
                if(fileName.equals(DEFAULT_LANGUAGE)) {
                    this.DEFAULT_LANGUAGE_CONFIG = updateLanguage(fileName);
                }
            }
        }
        // Fallback if selected language wasn't found
        if(this.SELECTED_LANGUAGE_CONFIG == null) {
            this.SELECTED_LANGUAGE_CONFIG = this.DEFAULT_LANGUAGE_CONFIG;
            Util.log("&cLanguage " + searchingFor + " was not found. Using default language.");
        }
    }

    /**
     * Load default language files.
     */
    private void loadDefaultFiles() {
        for(String fileName : DEFAULT_LANGUAGES) {
            File file = new File(folder, fileName + ".yml");
            if(file.exists() || plugin.getResource("lang/" + fileName + ".yml") == null) continue;

            plugin.saveResource("lang/" + fileName + ".yml", false);
        }
    }

    /**
     * Update selected language file.
     * @return Updated FileConfiguration
     */
    public FileConfiguration updateLanguage(@NotNull String languageName) {
        File file = new File(folder, languageName + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if(compareVersions(config)) return config;

        FileConfiguration defConfig = getSourceConfiguration(languageName);
        if (defConfig == null) return config;

        // Backup old config
        saveBackupConfig(languageName, config);

        // Value pasting from old config
        for(String key : defConfig.getKeys(true)) {
            if (defConfig.isConfigurationSection(key)) {
                continue; // Skip parent keys
            }
            if(config.contains(key)) {
                defConfig.set(key, config.get(key));
            }
        }

        defConfig.set("lang_version", plugin.getDescription().getVersion());
        config = defConfig;
        try {
            config.save(new File(folder, languageName + ".yml"));
            Util.log("&aUpdated language file.");
        }catch (Exception e) {
            Util.log("&cFailed to save updated language file. " + e.getMessage());
        }
        return config;
    }

    /**
     * Compare plugin and configuration file versions.
     * @return whether they match or not.
     */
    private boolean compareVersions(@NotNull FileConfiguration config) {
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("lang_version", "0");
        return version.equals(configVersion);
    }

    /**
     * Get the default configuration from source of plugin.
     * @return FileConfiguration
     */
    private FileConfiguration getSourceConfiguration(@NotNull String languageName) {
        InputStream defStream = plugin.getResource("lang/" + languageName + ".yml");
        if(defStream == null) {
            Util.log("&cDefault '" + languageName + ".yml' was not found in plugin resources.");
            return null;
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
    }

    /**
     * Save backup of old language config.
     */
    private void saveBackupConfig(@NotNull String languageName, @NotNull FileConfiguration config) {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            backupDir.mkdirs();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(backupDir, "lang_" + languageName + "_" + date + ".yml");
        try {
            config.save(backupFile);
            Util.log("&aBacked up old language file.");
        } catch (Exception e) {
            Util.log("&cFailed to save old language backup file. ");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }


    /**
     * Get configuration section from selected language config.
     * IF selected language config doesn't contain it, it gets from default language config.
     * @param path Section path
     * @return ConfigurationSection
     */
    @Nullable
    public ConfigurationSection getSection(@NotNull String path) {
        ConfigurationSection section = SELECTED_LANGUAGE_CONFIG.getConfigurationSection(path);
        if(section != null) return section;
        return DEFAULT_LANGUAGE_CONFIG.getConfigurationSection(path);
    }

    /**
     * Get message from selected language config.
     * IF selected language config doesn't contain it, it gets from default language config.
     * @param path Message path
     * @return Object value
     */
    @NotNull
    public String getString(@NotNull String path) {
        String str = SELECTED_LANGUAGE_CONFIG.contains(path)
                ? SELECTED_LANGUAGE_CONFIG.getString(path)
                : DEFAULT_LANGUAGE_CONFIG.getString(path);
        if(str == null) {
            Util.log("&cNull message at path '" + path + "'");
            return "";
        }
        return str;
    }

    /**
     * Get message from selected language config.
     * IF selected language config doesn't contain it, it gets from default language config.
     * @param path Message path
     * @return Object value
     */
    @NotNull
    public List<String> getStringList(@NotNull String path) {
        return SELECTED_LANGUAGE_CONFIG.contains(path)
                ? SELECTED_LANGUAGE_CONFIG.getStringList(path)
                : DEFAULT_LANGUAGE_CONFIG.getStringList(path);
    }

    /**
     * Get message from selected language config.
     * IF selected language config doesn't contain it, it gets from default language config.
     * @param path Message path
     * @return Object value
     */
    @NotNull
    public Boolean getBoolean(@NotNull String path) {
        return SELECTED_LANGUAGE_CONFIG.contains(path)
                ? SELECTED_LANGUAGE_CONFIG.getBoolean(path)
                : DEFAULT_LANGUAGE_CONFIG.getBoolean(path);
    }

    /**
     * Get message from selected language config section.
     * IF selected language config doesn't contain it, it gets from default language config section.
     * @param section Message Section
     * @param subPath Message sub path
     * @return Object value
     */
    @NotNull
    public String getString(@Nullable ConfigurationSection section, @NotNull String subPath) {
        final String currentPath = section == null ? "" : section.getCurrentPath();
        return getString(currentPath + "." + subPath);
    }

    /**
     * Get message from selected language config section.
     * IF selected language config doesn't contain it, it gets from default language config section.
     * @param section Message Section
     * @param subPath Message sub path
     * @return Object value
     */
    @NotNull
    public List<String> getStringList(@Nullable ConfigurationSection section, @NotNull String subPath) {
        final String currentPath = section == null ? "" : section.getCurrentPath();
        return getStringList(currentPath + "." + subPath);
    }

    /**
     * Get message from selected language config section.
     * IF selected language config doesn't contain it, it gets from default language config section.
     * @param section Message Section
     * @param subPath Message sub path
     * @return Object value
     */
    @NotNull
    public Boolean getBoolean(@Nullable ConfigurationSection section, @NotNull String subPath) {
        final String currentPath = section == null ? "" : section.getCurrentPath();
        return getBoolean(currentPath + "." + subPath);
    }
}
