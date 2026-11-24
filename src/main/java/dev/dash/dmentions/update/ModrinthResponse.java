package dev.dash.dmentions.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Optional;

public final class ModrinthResponse {

    private ModrinthResponse() {}

    public static Optional<String> latestRelease(String json) {
        try {
            JsonElement root = JsonParser.parseString(json);
            if (!root.isJsonArray()) return Optional.empty();

            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) continue;
                JsonObject version = element.getAsJsonObject();
                if (!"release".equals(text(version, "version_type"))) continue;

                String number = text(version, "version_number");
                if (number != null && !number.isBlank()) return Optional.of(number);
            }
        } catch (RuntimeException e) { // malformed JSON
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static String text(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }
}
