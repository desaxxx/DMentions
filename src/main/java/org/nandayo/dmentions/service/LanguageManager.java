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

    private final DMentions plugin;
    private final File folder;
    public LanguageManager(@NotNull DMentions plugin, @NotNull File folder, @NotNull String fileName) {
        this.plugin = plugin;
        this.folder = folder;
        if(!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }
        this.loadFiles(fileName);
    }

    // Default values
    public final List<String> DEFAULT_LANGUAGE_LIST = Arrays.asList("en-US","tr-TR","zh-CN");
    private final String DEFAULT_LANGUAGE = "en-US";
    private FileConfiguration DEFAULT_LANGUAGE_CONFIG;

    //
    private FileConfiguration SELECTED_LANGUAGE_CONFIG;

    /**
     * Load language files and setup default & selected language file configuration.
     * @param searchingFor File name of selected language
     */
    private void loadFiles(@NotNull String searchingFor) {
        File[] files = folder.listFiles();
        if (files != null) {
            for(String fileName : DEFAULT_LANGUAGE_LIST) {
                File file = new File(folder, fileName + ".yml");
                if(!file.exists()) {
                    plugin.saveResource("lang" + File.separator + fileName + ".yml", false);
                }
                // Setup selected language file.
                if(fileName.equals(searchingFor)) {
                    this.SELECTED_LANGUAGE_CONFIG = updateLanguage(fileName);
                }
                // Setup default language file.
                if(fileName.equals(DEFAULT_LANGUAGE)) {
                    this.DEFAULT_LANGUAGE_CONFIG = updateLanguage(fileName);
                }
            }
        }
        // If selected language was not found.
        if(this.SELECTED_LANGUAGE_CONFIG == null) {
            this.SELECTED_LANGUAGE_CONFIG = this.DEFAULT_LANGUAGE_CONFIG;
            Util.log("&cLanguage " + searchingFor + " was not found. Using default language.");
        }
    }

    /**
     * Update selected language file.
     * @return Updated FileConfiguration
     */
    public FileConfiguration updateLanguage(@NotNull String languageName) {
        File file = new File(folder, languageName + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("lang_version", "0");

        if(version.equals(configVersion)) return config;

        InputStream defStream = plugin.getResource("lang/" + languageName + ".yml");
        if(defStream == null) {
            Util.log("&cDefault " + languageName + ".yml not found in plugin resources.");
            return config;
        }

        // Backup old config
        saveBackupConfig(languageName, config);

        // Value pasting from old config
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        for(String key : defConfig.getKeys(true)) {
            if (defConfig.isConfigurationSection(key)) {
                continue; // Skip parent keys
            }
            if(config.contains(key)) {
                defConfig.set(key, config.get(key));
            }
        }

        try {
            defConfig.set("lang_version", version);
            defConfig.save(new File(folder, languageName + ".yml"));
            config = defConfig;
            Util.log("&aUpdated language file.");
        }catch (Exception e) {
            Util.log("&cFailed to save updated language file. " + e.getMessage());
        }
        return config;
    }

    /**
     * Save backup of old config.
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
            Util.log("&cFailed to save old language backup file. " + e.getMessage());
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
    public Object getMessage(@NotNull String path) {
        return SELECTED_LANGUAGE_CONFIG.contains(path)
                ? SELECTED_LANGUAGE_CONFIG.get(path, "")
                : DEFAULT_LANGUAGE_CONFIG.get(path,"");
    }

    /**
     * Get message from selected language config section.
     * IF selected language config doesn't contain it, it gets from default language config section.
     * @param section Message Section
     * @param subPath Message sub path
     * @return Object value
     */
    @NotNull
    public Object getMessage(@Nullable ConfigurationSection section, @NotNull String subPath) {
        final String currentPath = section == null ? "" : section.getCurrentPath();
        return getMessage(currentPath + "." + subPath);
    }

    /**
     * Get Replacer class to replace a config message that's from selected language config.
     * IF selected language config doesn't contain it, it gets from default language config.
     * @param path Message path
     * @return Replacer
     */
    @NotNull
    public Replacer getMessageReplaceable(@NotNull String path) {
        Object message = getMessage(path);
        if(message instanceof String) {
            return new Replacer((String) message);
        }else if(message instanceof String[]) {
            return new Replacer((String[]) message);
        }else if(message instanceof List) {
            //noinspection unchecked
            return new Replacer(((List<String>) message).toArray(new String[0]));
        }
        return new Replacer("");
    }

    /**
     * Get Replacer class to replace a config message that's from selected language config section
     * IF selected language config doesn't contain it, it gets from default language config section.
     * @param section Message Section
     * @param subPath Message sub path
     * @return Replacer
     */
    @NotNull
    public Replacer getMessageReplaceable(@Nullable ConfigurationSection section, @NotNull String subPath) {
        final String currentPath = section == null ? "" : section.getCurrentPath();
        return getMessageReplaceable(currentPath + "." + subPath);
    }


    static public class Replacer {

        public Replacer(@NotNull String... str) {
            this.str = str;
        }

        private String[] str;

        /**
         * Replace placeholders using this method.
         * @param replacement Placeholder
         * @param value Replaced value
         * @return Replacer
         */
        public Replacer replace(@NotNull String replacement, @NotNull Object value) {
            String[] replaced = new String[str.length];
            for(int index = 0; index < str.length; index++) {
                replaced[index] = str[index].replace(replacement, value.toString());
            }
            this.str = replaced;
            return this;
        }

        /**
         * Get the result Strings.
         * @return String[]
         */
        public String[] get() {
            return this.str;
        }
    }

    /**
     * Get changeable value display message from selected language config.
     * IF selected language config doesn't contain it, it gets from default language config.
     * @param langPath Message path
     * @param configPath Config path
     * @param configManager ConfigManager
     * @return String[]
     */
    public String[] getValueDisplayMessage(@NotNull String langPath, @NotNull String configPath, @NotNull ConfigManager configManager) {
        return getMessageReplaceable(langPath)
                .replace("{value}", configManager.get(configPath, ""))
                .replace("{unsaved_value}", configManager.get(configPath, "", true))
                .get();
    }

    /**
     * Get changeable value display from selected language config section.
     * IF selected language config doesn't contain it, it gets from default language config section.
     * @param section Message Section
     * @param langSubPath Message sub path
     * @param configPath Config path
     * @param configManager ConfigManager
     * @return String[]
     */
    public String[] getValueDisplayMessage(@Nullable ConfigurationSection section, @NotNull String langSubPath, @NotNull String configPath, @NotNull ConfigManager configManager) {
        return getMessageReplaceable(section, langSubPath)
                .replace("{value}", configManager.get(configPath, ""))
                .replace("{unsaved_value}", configManager.get(configPath, "", true))
                .get();
    }
}
