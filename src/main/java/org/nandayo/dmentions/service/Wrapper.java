package org.nandayo.dmentions.service;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.util.Util;
import org.nandayo.dapi.util.VersionUtil;
import org.nandayo.dmentions.DMentions;

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
        int version = org.nandayo.dapi.util.Wrapper.getMinecraftVersion();
        if (version < 1605) {
            Util.log(String.format("&cYou are using an unsupported server version '%s'!", VersionUtil.stringify(10000 + version)),
                    "&cPlease use v1.16.5 or newer.");
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
        }
        return version;
    }


    @Nullable
    public Sound getSound(@NotNull String soundName) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(soundName.toLowerCase(Locale.ENGLISH));
        return namespacedKey == null ? null : Registry.SOUNDS.get(namespacedKey); // since 1.16.4
    }

    /**
     * Play a sound to the Player with given properties.
     * @param player Player
     * @param sound Sound
     * @param volume Volume
     * @param pitch Pitch
     * @since 1.8.3
     */
    public void playSound(@NotNull Player player, @NotNull Sound sound, float volume, float pitch) {
        if(version >= 1801) {
            player.playSound(player, sound, volume, pitch);
        }else {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void playSound(@NotNull Player player, @NotNull Sound sound) {
        playSound(player, sound, 0.6f, 1.0f);
    }
}
