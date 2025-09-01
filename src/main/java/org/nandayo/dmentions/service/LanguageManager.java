package org.nandayo.dmentions.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.util.DUtil;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {
    private static final SimpleDateFormat backupDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private final @NotNull DMentions plugin;
    private final @NotNull File folder;
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

        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("lang_version", "0");
        if(version.equals(configVersion)) return config;

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

        // Adapter for 1.8.2 to 1.8.3
        int oldVersion = parseVersion(configVersion);
        // oldVersion may be 0 if it's the first time loading.
        if(oldVersion != 0 && oldVersion < 10803 && parseVersion(version) >= 10803) {
            ADAPTER_1_8_3.update(config, defConfig);
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
     * Parse a version to an Integer.<br>
     * <p>
     *     Example: 1.8.3 -> 10803
     * </p>
     * @param version Version
     * @return Integer
     * @since 1.8.3
     */
    private int parseVersion(@NotNull String version) {
        String[] parts = version.split("\\.");
        int major = DUtil.parseInt(parts[0],0);
        int minor = parts.length > 1 ? DUtil.parseInt(parts[1],0) : 0;
        int patch = parts.length > 2 ? DUtil.parseInt(parts[2],0) : 0;
        return major * 10000 + minor * 100 + patch;
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
        String date = backupDateFormat.format(new Date());
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
     * Get an object from {@link #SELECTED_LANGUAGE_CONFIG} with given path.
     * @param path Path
     * @return Object if found, else {@code null}.
     * @since 1.8.3
     */
    @NotNull
    public Object get(@NotNull String path) {
        Object obj = SELECTED_LANGUAGE_CONFIG.contains(path)
                ? SELECTED_LANGUAGE_CONFIG.get(path)
                : DEFAULT_LANGUAGE_CONFIG.get(path);
        if(obj == null) {
            Util.log("&cNull object at path '" + path + "'");
            return "";
        }
        return obj;
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


    /**
     * @since 1.8.3
     */
    static private class ADAPTER_1_8_3 {

        /**
         * Adapt old message file keys and values to new one.
         * @param oldConfig Updated configuration file upon old one.
         * @param defConfig Default latest configuration of the plugin
         * @since 1.8.3
         */
        static private void update(@NotNull FileConfiguration oldConfig, @NotNull FileConfiguration defConfig) {

            // #1(paths), #2(placeholders)
            ConfigurationSection groupSection = oldConfig.getConfigurationSection("group");
            List<String> key1List = new ArrayList<>(Arrays.asList("player", "nearby", "everyone"));
            if(groupSection != null) {
                for(String group : groupSection.getKeys(false)) {
                    key1List.add("group." + group);
                }
            }
            for(String key2 : Arrays.asList("sender_message","target_message")) {
                for(String key1 : key1List) {
                    defConfig.set(key1 + ".title", null);
                    defConfig.set(key1 + ".action_bar", null);
                    List<String> value = new ArrayList<>();
                    boolean isSenderMessageAndPlayerSection = key1.equals("player") && key2.equals("sender_message");

                    if(oldConfig.contains(key1 + ".action_bar." + key2)) {
                        value.add("ACTION_BAR=" + oldConfig.getString(key1 + ".action_bar." + key2,"")
                                .replace("{p}", isSenderMessageAndPlayerSection ? "{target}" : "{sender}"));
                    }
                    if(oldConfig.contains(key1 + ".title." + key2)) {
                        value.add("TITLE=" + oldConfig.getString(key1 + ".title." + key2,"")
                                .replace("{p}", isSenderMessageAndPlayerSection ? "{target}" : "{sender}"));
                    }
                    defConfig.set(key1 + "." + key2, value);
                }
            }


            // #2.1(placeholders)
            defConfig.set("cooldown_warn", oldConfig.getString("cooldown_warn","").replace("{REMAINED}","{remained}"));
            defConfig.set("command.user.mentions.success", oldConfig.getString("command.user.mentions.success","").replace("{p}","{target}"));
            defConfig.set("command.user.display.success", oldConfig.getString("command.user.display.success","").replace("{p}","{target}"));

            // #3(update hex patterns)
            for(Map.Entry<String, Object> entry : defConfig.getValues(true).entrySet()) {
                String key = entry.getKey();
                if (defConfig.isConfigurationSection(key)) {
                    continue; // Skip parent keys
                }
                Object value = entry.getValue();
                if(value instanceof String) {
                    String str = (String) value;
                    defConfig.set(key, replaceHexPatterns(str));
                }else if(value instanceof List<?>) {
                    List<?> list = (List<?>) value;
                    List<Object> updated = new ArrayList<>();
                    for(Object o : list) {
                        if(o instanceof String) {
                            String str = (String) o;
                            updated.add(replaceHexPatterns(str));
                        }else {
                            updated.add(o);
                        }
                    }
                    defConfig.set(key, updated);
                }
            }
        }

        private static final Pattern OLD_HEX_PATTERN = Pattern.compile("<(#[0-9A-Fa-f]{6})>");

        private static String replaceHexPatterns(final String input) {
            if(input == null || input.isEmpty()) return "";
            String output = input;

            Matcher matcher = OLD_HEX_PATTERN.matcher(input);
            while(matcher.find()) {
                String original = matcher.group(); // -> <#RRGGBB>
                String hex = matcher.group(1); // -> #RRGGBB

                output = output.replace(original, "&" + hex); // -> &#RRGGBB
            }
            return output;
        }
    }
}
