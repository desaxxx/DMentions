package dev.dash.dmentions.mention;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.Optional;

public final class SoundSpec {

    // key;volume;pitch
    public static Optional<Sound> parse(String soundSpecification) {
        if (soundSpecification == null || soundSpecification.isBlank()) return Optional.empty();
        String[] parts = soundSpecification.split(";",3);

        Key key;
        try {
            key = Key.key(parts[0].trim());
        } catch (InvalidKeyException e) {
            return Optional.empty();
        }

        float volume = 1.0f;
        if (parts.length > 1) {
            try {
                volume = Float.parseFloat(parts[1].trim());
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        float pitch = 1.0f;
        if (parts.length > 2) {
            try {
                pitch = Float.parseFloat(parts[2].trim());
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.of(Sound.sound(key, Sound.Source.MASTER, volume, pitch));
    }
}
