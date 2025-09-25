package org.nandayo.dmentions.user;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SingleFolderMigrator {

    public static void migrate() {
        File oldPlayersFile = new File(DMentions.inst().getDataFolder(), "players.yml");
        if(!oldPlayersFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(oldPlayersFile);
        if(config.getBoolean("migrated")) return;

        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if(playersSection != null) {
            for(String id : playersSection.getKeys(false)) {
                UUID uuid = UUID.fromString(id);
                boolean mentions = playersSection.getBoolean(id + ".mentions");
                String mentionDisplay = playersSection.getString(id + ".mention_display");

                File newFile = new File(DMentions.inst().getDataFolder(), "players/" + uuid + ".yml");
                FileConfiguration newConfig = YamlConfiguration.loadConfiguration(newFile);
                String ns = "==.";
                newConfig.set(ns + "uuid", uuid.toString());
                newConfig.set(ns + "mention_mode", mentions);
                newConfig.set(ns + "customized_display_name", mentionDisplay);

                try {
                    newConfig.save(newFile);
                } catch (IOException e) {
                    Util.log("Failed to save new player folder for '" + uuid + "'. " + e);
                }
            }
        }

        try {
            if(!oldPlayersFile.delete()) {
                throw new IOException();
            }
        } catch (IOException e) {
            Util.log("Failed to delete old players file. " + e);
            // mark migrated if failed to delete.
            try {
                config.set("migrated", true);
                config.save(oldPlayersFile);
            } catch (IOException ee) {
                Util.log("Failed to mark old players file migrated. " + ee);
            }
        }
    }
}
