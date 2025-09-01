package org.nandayo.dmentions.service;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    static private final int RESOURCE_ID = 121452;
    static public final UpdateChecker INSTANCE = new UpdateChecker();

    private UpdateChecker() {}

    private boolean isChecked = false;

    /**
     * Version checker.
     * @since 1.8.3
     */
    public void check(@NotNull DMentions plugin) {
        if(isChecked) return;
        isChecked = true;

        if(!isCheckerEnabled(plugin.getConfiguration())) return;

        fetchLatest(plugin, latest -> {
            if (plugin.getDescription().getVersion().equals(latest)) {
                Util.log("&aPlugin is up-to-date.");
            } else {
                Util.log("&fThere is a new version update. (&e" + latest + "&f)");
            }
        });
    }

    /**
     * Check if Update checker is enabled in {@link Config}.
     * @param config Config
     * @return whether enabled or not
     * @since 1.8.3
     */
    private boolean isCheckerEnabled(@NotNull Config config) {
        return config.getConfig().getBoolean("check_for_updates", true);
    }

    /**
     * Fetch the latest release of DMentions from Spigot.
     * @since 1.8.3
     */
    private void fetchLatest(@NotNull DMentions plugin, @NotNull Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID + "/~").openStream();
                 Scanner scann = new Scanner(is)) {
                if (scann.hasNext()) {
                    consumer.accept(scann.next());
                }
            } catch (IOException | IllegalStateException e) {
                Util.log("&cUnable to check for updates: " + e.getMessage());
            }
        });
    }
}