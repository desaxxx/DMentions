package dev.dash.dmentions.update;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Logger;

public final class UpdateChecker {

    public static final String PROJECT_PAGE = "https://modrinth.com/plugin/dmentions";
    private static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/dmentions/version?loaders="
            + URLEncoder.encode("[\"paper\"]", StandardCharsets.UTF_8);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final Logger logger;
    private final String currentVersion;
    private volatile String newerVersion;

    public UpdateChecker(Logger logger, String currentVersion) {
        this.logger = logger;
        this.currentVersion = currentVersion;
    }

    public void check() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(VERSIONS_URL))
                .timeout(TIMEOUT)
                .header("User-Agent", "DMentions/" + currentVersion + " (github.com/desaxxx/DMentions)")
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    client.close();
                    if (error != null) {
                        logger.warning("Could not check for updates: " + error.getMessage());
                    } else if (response.statusCode() != 200) {
                        logger.warning("Could not check for updates: Modrinth answered " + response.statusCode() + ".");
                    } else {
                        handle(response.body());
                    }
                });
    }

    void handle(String body) {
        Optional<String> latest = ModrinthResponse.latestRelease(body);
        if (latest.isEmpty()) {
            logger.warning("Could not check for updates: no release found on Modrinth.");
        } else if (Versions.isNewer(latest.get(), currentVersion)) {
            newerVersion = latest.get();
            logger.info("A new version of DMentions is available: " + latest.get()
                    + " (current: " + currentVersion + "). " + PROJECT_PAGE);
        } else {
            logger.info("DMentions is up to date.");
        }
    }

    public Optional<String> newerVersion() {
        return Optional.ofNullable(newerVersion);
    }

    public String currentVersion() {
        return currentVersion;
    }
}
