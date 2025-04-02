package org.nandayo.DMentions.service;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.DAPI.HexUtil;
import org.nandayo.DAPI.Util;
import org.nandayo.DMentions.DMentions;

import java.util.List;
import java.util.Objects;

public class GUIManager {

    private final DMentions plugin;
    private final ConfigManager configManager; // Unsaved config manager

    @Getter
    private final Player player; // GUI modifier

    public GUIManager(DMentions plugin, ConfigManager configManager, Player modifier) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.player = modifier;
    }

    public Object getUValue(String key, Object def) {
        return configManager.get(key,def, true);
    }
    public void setUValue(String key, Object value) {
        configManager.set(key, value, true);
    }
    public ConfigurationSection getUSection(String key) {
        return configManager.getConfigurationSection(key, true);
    }
    public List<String> getUStringList(String key) {
        return configManager.getStringList(key, true);
    }
    public boolean contains(String key) {
        return configManager.get(key,null, true) != null;
    }

    /**
     * Check if value changed.
     * @param key Key path
     * @return boolean
     */
    public boolean isValueChanged(@NotNull String key) {
        return !Objects.equals(configManager.get(key, ""), configManager.get(key, "", true));
    }

    /*
     * Reset Changes
     */
    public void resetChanges() {
        configManager.resetGuiConfig();
        player.sendMessage(HexUtil.color((String) plugin.LANGUAGE_MANAGER.getMessage("command.config.reset_changes")));
    }

    /*
     * Save Changes
     */
    public void saveChanges() {
        configManager.saveGuiConfig();
        player.sendMessage(HexUtil.color((String) plugin.LANGUAGE_MANAGER.getMessage("command.config.save_changes")));
        Util.log("&eUpdated config keys in-game by player " + player.getName() + ".");
    }
}
