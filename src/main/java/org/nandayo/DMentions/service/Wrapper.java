package org.nandayo.DMentions.service;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.DAPI.Util;
import org.nandayo.DMentions.DMentions;

import java.util.Locale;

@Getter
public class Wrapper {

    private final DMentions plugin;
    public Wrapper(DMentions plugin) {
        this.plugin = plugin;
        version = fetchVersion();
    }

    private final int version;

    private int fetchVersion() {
        String[] ver = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        if(ver.length < 2) {
            Util.log("{WARN}Could not fetch server version!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
        int major = 0;
        try {
            major = Integer.parseInt(ver[1]);
        } catch (NumberFormatException ignored) {}
        int minor = 0;
        try {
            minor = Integer.parseInt(ver[2]);
        } catch (NumberFormatException ignored) {}

        int version = major * 10 + minor;
        if(version < 165) {
            Util.log(String.format("&cYou are using an unsupported server version '%s'!", String.join(".", ver)),
                    "&cPlease use v1.16.5 or newer.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
        return version;
    }


    @Nullable
    public Sound getSound(@NotNull String soundName) {
        // since v1.16.4
        return Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase(Locale.ENGLISH)));
    }
}
