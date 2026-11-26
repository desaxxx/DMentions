package org.nandayo.dmentions.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.util.Util;
import org.nandayo.dmentions.DMentions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class UpdateChecker {
    static private final String VERSIONS_URL = "https://api.modrinth.com/v2/project/dmentions/version";
    static private final String PROJECT_PAGE = "https://modrinth.com/plugin/dmentions";
    static private final int TIMEOUT_MILLIS = 10_000;
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

        String current = plugin.getDescription().getVersion();
        fetchLatest(plugin, latest -> {
            if (!isNewer(latest, current)) {
                Util.log("&aPlugin is up-to-date.");
            } else if (majorOf(latest) < 2) {
                Util.log("&fThere is a new version update. (&e" + latest + "&f)");
            } else if (canRun2()) {
                Util.log("&fDMentions &e" + latest + "&f is available. Your config and player data are migrated automatically. " + PROJECT_PAGE);
            } else {
                Util.log("&fDMentions &e" + latest + "&f is available but needs Paper 1.21+ and Java 21+. This server can keep using " + current + ".");
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
     * Fetch the latest release of DMentions from Modrinth.
     * @since 1.8.5
     */
    private void fetchLatest(@NotNull DMentions plugin, @NotNull Consumer<String> consumer) {
        String userAgent = "DMentions/" + plugin.getDescription().getVersion() + " (github.com/desaxxx/DMentions)";
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(VERSIONS_URL).openConnection();
                connection.setRequestProperty("User-Agent", userAgent);
                connection.setConnectTimeout(TIMEOUT_MILLIS);
                connection.setReadTimeout(TIMEOUT_MILLIS);
                if (connection.getResponseCode() != 200) {
                    Util.log("&cUnable to check for updates: Modrinth answered " + connection.getResponseCode() + ".");
                    return;
                }
                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    // the instance method still works with the old Gson of 1.16 servers
                    String latest = latestRelease(new JsonParser().parse(reader));
                    if (latest == null) {
                        Util.log("&cUnable to check for updates: no release found on Modrinth.");
                        return;
                    }
                    consumer.accept(latest);
                }
            } catch (IOException | RuntimeException e) {
                Util.log("&cUnable to check for updates: " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    /**
     * Modrinth lists the newest version first; betas and alphas are skipped.
     * @param versions the version list from Modrinth
     * @return version number of the newest release, or null
     * @since 1.8.5
     */
    @Nullable
    static String latestRelease(@NotNull JsonElement versions) {
        if (!versions.isJsonArray()) return null;
        for (JsonElement element : versions.getAsJsonArray()) {
            if (!element.isJsonObject()) continue;
            JsonObject version = element.getAsJsonObject();
            if (!version.has("version_type") || !version.has("version_number")) continue;
            if ("release".equals(version.get("version_type").getAsString())) {
                return version.get("version_number").getAsString();
            }
        }
        return null;
    }

    /**
     * Compares numerically, so 1.10 is newer than 1.9. A suffix (2.0.0-beta) comes before the release with the same numbers.
     * @return whether latest is newer than current
     * @since 1.8.5
     */
    static boolean isNewer(@NotNull String latest, @NotNull String current) {
        int[] a = numbers(latest);
        int[] b = numbers(current);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        return !hasSuffix(latest) && hasSuffix(current);
    }

    private static int[] numbers(@NotNull String version) {
        String[] parts = version.split("[-+]", 2)[0].split("\\.");
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String digits = parts[i].replaceAll("\\D.*", "");
            numbers[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
        }
        return numbers;
    }

    private static boolean hasSuffix(@NotNull String version) {
        return version.contains("-") || version.contains("+");
    }

    private static int majorOf(@NotNull String version) {
        return numbers(version)[0];
    }

    /**
     * DMentions 2.0 needs Paper (or a fork) 1.21+ and Java 21+.
     * @since 1.8.5
     */
    private static boolean canRun2() {
        return isPaper() && isMinecraft121OrNewer() && Runtime.version().major() >= 21;
    }

    private static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.ServerBuildInfo");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 1.21.10-R0.1-SNAPSHOT, or 26.1-R0.1-SNAPSHOT since the new version scheme
    private static boolean isMinecraft121OrNewer() {
        int[] version = numbers(Bukkit.getBukkitVersion());
        if (version[0] != 1) return version[0] > 1;
        return version.length > 1 && version[1] >= 21;
    }
}
