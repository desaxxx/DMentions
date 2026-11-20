package dev.dash.dmentions.mention;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundSpecTest {

    private static Sound parsed(String spec) {
        Optional<Sound> sound = SoundSpec.parse(spec);
        assertTrue(sound.isPresent(), "expected a sound for: " + spec);
        return sound.get();
    }

    @Test
    void keyOnlyUsesDefaultVolumeAndPitch() {
        Sound sound = parsed("block.note_block.pling");
        assertEquals(Key.key("minecraft", "block.note_block.pling"), sound.name());
        assertEquals(1f, sound.volume());
        assertEquals(1f, sound.pitch());
    }

    @Test
    void volumeAndPitch() {
        Sound sound = parsed("block.note_block.pling;0.6;1.5");
        assertEquals(0.6f, sound.volume());
        assertEquals(1.5f, sound.pitch());
    }

    @Test
    void volumeOnly() {
        Sound sound = parsed("block.note_block.pling;0.3");
        assertEquals(0.3f, sound.volume());
        assertEquals(1f, sound.pitch());
    }

    @Test
    void namespacedKey() {
        assertEquals(Key.key("custom", "ping"), parsed("custom:ping").name());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        Sound sound = parsed(" block.note_block.pling ; 0.5 ; 2 ");
        assertEquals(Key.key("minecraft", "block.note_block.pling"), sound.name());
        assertEquals(0.5f, sound.volume());
        assertEquals(2f, sound.pitch());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankMeansNoSound(String spec) {
        assertTrue(SoundSpec.parse(spec).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Block.Note_Block.Pling", "bad key", "block.note_block.pling;loud",
            "block.note_block.pling;1;high"})
    void invalidSpecIsEmpty(String spec) {
        assertTrue(SoundSpec.parse(spec).isEmpty());
    }
}
