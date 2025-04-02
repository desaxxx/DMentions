package org.nandayo.DMentions.service;

import java.util.HashMap;

public class Cooldown<T> {

    private final HashMap<T, Long> cooldowns = new HashMap<>();

    /**
     * Update the cooldown as present time.
     * @param key Key
     */
    public void update(T key) {
        cooldowns.put(key, System.currentTimeMillis());
    }

    /**
     * Get remaining time of cooldown.
     * @param key Key
     * @param cooldownTime Cooldown time
     * @return Remained time
     */
    public long getRemaining(T key, long cooldownTime) {
        long lastTime = cooldowns.getOrDefault(key, 0L);
        long elapsed = System.currentTimeMillis() - lastTime;

        if (elapsed >= cooldownTime) {
            cooldowns.remove(key);
            return 0L;
        }
        return cooldownTime - elapsed;
    }

    /**
     * Remove key from map.
     * @param key Key
     */
    public void remove(T key) {
        cooldowns.remove(key);
    }
}
