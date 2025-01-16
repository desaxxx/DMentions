package org.nandayo.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.nandayo.Main;

import java.util.List;
import java.util.Objects;

import static org.nandayo.utils.HexUtil.color;

public class GUIManager {

    private final Main plugin;
    private ConfigManager configManager; // Unsaved config manager

    private final Player player; // GUI modifier

    public GUIManager(Main plugin, ConfigManager configManager, Player modifier) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.player = modifier;
    }

    /*
     * GETTERS
     */
    public Player getPlayer() {
        return player;
    }
    public ConfigManager getUConfigManager() {
        return configManager;
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

    /*
     * Value Display
     */
    public String getValueDisplay(String key) {
        return getValueDisplay(key, "");
    }
    public String getValueDisplay(String key, String placeholder) {
        Object curValue = configManager.get(key, "");
        Object changedValue = configManager.get(key, "", true);
        if(Objects.equals(curValue, changedValue)) {
            return "&f" + curValue + placeholder;
        }
        return "&7" + curValue + placeholder + "&a " + Main.arrow + "&f " + getUValue(key, "") + placeholder;
    }

    /*
     * Reset Changes
     */
    public void resetChanges() {
        configManager.resetGuiConfig();
        player.sendMessage(color(plugin.langManager.getMsg("command.config.reset_changes")));
    }

    /*
     * Save Changes
     */
    public void saveChanges() {
        configManager.saveGuiConfig();
        player.sendMessage(color(plugin.langManager.getMsg("command.config.save_changes")));
        Util.log("&eUpdated config keys in-game by player " + player.getName() + ".");
    }
}
