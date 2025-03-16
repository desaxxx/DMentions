package org.nandayo.DMentions.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.DAPI.Util;
import org.nandayo.DMentions.Main;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SuppressWarnings("CallToPrintStackTrace")
public class LangManager {

    private final Main plugin;

    private final List<String> languages = Arrays.asList("en-US","tr-TR","zh-CN");
    private final String defaultLang = "en-US";
    private FileConfiguration DEFAULT_CONFIG = null;

    private final String selectedLang;
    private final File file;
    private FileConfiguration config;

    public LangManager(Main plugin, String fileName) {
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(), "lang");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        loadFiles();
        if(!languages.contains(fileName)) {
            Util.log("&cLanguage " + fileName + " not found. Using default language.");
            fileName = defaultLang;
        }
        this.selectedLang = fileName;
        this.file = new File(dir, fileName + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        updateLanguage();
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void loadFiles() {
        for(String lang : languages) {
            String path = "lang/" + lang + ".yml";
            File file = new File(plugin.getDataFolder(), path);
            if(!file.exists() && plugin.getResource(path) != null) {
                plugin.saveResource(path, false);
            }
            //Default lang file
            if(lang.equals(defaultLang)) {
                DEFAULT_CONFIG = YamlConfiguration.loadConfiguration(file);
            }
        }
        if(DEFAULT_CONFIG == null) {
            Util.log("&cDefault language (" + defaultLang + ") not found. This may lead to empty messages.");
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    /*
     * Return the message
     */
    public String getMsg(String path) {
        Object obj = getDefaultIfMissing(path);
        if(obj instanceof String) {
            return (String) obj;
        }
        return "";
    }
    public String getMsg(ConfigurationSection section, String subPath) {
        String path = section.getCurrentPath() + "." + subPath;
        return getMsg(path);
    }

    /*
     * Missing keys on language files
     */
    private Object getDefaultIfMissing(String path) {
        if(config.contains(path)) {
            return config.get(path, "");
        }else if(DEFAULT_CONFIG != null && DEFAULT_CONFIG.contains(path)) {
            return DEFAULT_CONFIG.get(path, "");
        }
        return "";
    }

    //UPDATE LANGUAGE
    public LangManager updateLanguage() {
        String version = plugin.getDescription().getVersion();
        String configVersion = config.getString("lang_version", "0");

        if(version.equals(configVersion)) return this;

        InputStream defStream = plugin.getResource("lang/" + selectedLang + ".yml");
        if(defStream == null) {
            Util.log("&cDefault " + selectedLang + ".yml not found in plugin resources.");
            return this;
        }

        // Backup old config
        saveBackupConfig();

        // Value pasting from old config
        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        for(String key : defConfig.getKeys(true)) {
            if(config.contains(key)) {
                defConfig.set(key, config.get(key));
            }
        }

        try {
            defConfig.set("lang_version", version);
            defConfig.save(file);
            config = defConfig;
            Util.log("&aUpdated language file.");
        }catch (Exception e) {
            Util.log("&cFailed to save updated language file.");
            e.printStackTrace();
        }
        return this;
    }

    private void saveBackupConfig() {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File backupFile = new File(backupDir, "lang_" + selectedLang + "_" + date + ".yml");
        try {
            config.save(backupFile);
            Util.log("&aBacked up old language file.");
        } catch (Exception e) {
            Util.log("&cFailed to save old language backup file.");
            e.printStackTrace();
        }
    }
}
