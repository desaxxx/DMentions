package org.nandayo.dmentions.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dmentions.util.DUtil;

/**
 * @since 1.8.3
 */
@Getter
public class SoundProperty {

    private final @NotNull String key;
    private final float volume;
    private final float pitch;

    public SoundProperty(@NotNull String key, float volume, float pitch) {
        this.key = key;
        this.volume = Math.max(0, volume);
        this.pitch = Math.max(0, pitch);
    }


    /**
     * Get a {@link SoundProperty} from given String.
     * @param str String with format "sound_key;volume;float"
     * @return SoundProperty
     * @since 1.8.3
     */
    @NotNull
    static public SoundProperty deserialize(@NotNull String str) {
        String[] split = str.split(";", 3);
        float volume = split.length > 1 ? DUtil.parseFloat(split[1], 0.6f) : 0.6f;
        float pitch = split.length > 2 ? DUtil.parseFloat(split[2], 1.0f) : 1.0f;
        return new SoundProperty(split[0], volume, pitch);
    }
}
