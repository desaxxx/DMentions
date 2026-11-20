package dev.dash.dmentions.mention;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownTracker {

    private final ConcurrentHashMap<Key, Long> expiresAt = new ConcurrentHashMap<>();

    public long remaining(UUID sender, String type, String target, long now) {
        Key key = new Key(sender, type, target);
        Long expires = expiresAt.get(key);
        if (expires == null) return 0L;
        long remaining = expires - now;
        if (remaining <= 0) {
            expiresAt.remove(key, expires);
            return 0;
        }
        return remaining;
    }

    public void start(UUID sender, String type, String target, Duration cooldown, long now) {
        long cooldownMs = cooldown.toMillis();
        if (cooldownMs <= 0) return;
        Key key = new Key(sender, type, target);
        expiresAt.put(key, now + cooldownMs);
    }


    public record Key(UUID sender, String type, String target) {}
}
