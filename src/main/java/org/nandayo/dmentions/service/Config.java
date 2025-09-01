package org.nandayo.dmentions.service;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.message.Message;
import org.nandayo.dmentions.util.DUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("CallToPrintStackTrace")
@Getter
public class Config {
    private static final SimpleDateFormat backupDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

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
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("config_version", "0");
        if(version.equals(configVersion)) return this;

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

        // Adapter for 1.8.2 to 1.8.3
        int oldVersion = parseVersion(configVersion);
        // oldVersion may be 0 if it's the first time loading.
        if(oldVersion != 0 && oldVersion < 10803 && parseVersion(version) >= 10803) {
            ADAPTER_1_8_3.update(config, defConfig);
        }

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
        String date = backupDateFormat.format(new Date());
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
            Message.COMMAND_CONFIG_RESET_CHANGES.sendMessage(sender);
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
            Message.COMMAND_CONFIG_SAVE_CHANGES.sendMessage(sender);
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





    /**
     * @since 1.8.3
     */
    static private class ADAPTER_1_8_3 {

        /**
         * Adapt old config file keys and values to new one.
         * @param oldConfig Updated configuration file upon old one.
         * @param defConfig Default latest configuration of the plugin
         * @since 1.8.3
         */
        static private void update(@NotNull FileConfiguration oldConfig, @NotNull FileConfiguration defConfig) {

            // #1(update hex patterns)
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
