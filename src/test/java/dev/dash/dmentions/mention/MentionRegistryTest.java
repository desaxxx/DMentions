package dev.dash.dmentions.mention;

import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentionRegistryTest {

    /** A type that only knows its id, whether it is enabled and which keywords it offers. */
    private static final class FakeType implements MentionType {
        private final String id;
        private final boolean enabled;
        private final List<String> keywords;
        int candidateCalls;

        FakeType(String id, boolean enabled, String... keywords) {
            this.id = id;
            this.enabled = enabled;
            this.keywords = Arrays.asList(keywords);
        }

        @Override public String id() { return id; }
        @Override public boolean enabled() { return enabled; }
        @Override public List<MentionCandidate> candidates() {
            candidateCalls++;
            return keywords.stream().map(k -> new MentionCandidate(k, this, id + ":" + k, k)).toList();
        }
        @Override public Component display(MentionCandidate candidate) { return Component.text(candidate.keyword()); }
        @Override public Duration cooldown() { return Duration.ZERO; }
        @Override public Optional<Sound> sound() { return Optional.empty(); }
        @Override public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) { return List.of(); }
        @Override public ConfigKey<List<Notification>> senderMessage() { return null; }
        @Override public ConfigKey<List<Notification>> targetMessage() { return null; }
        @Override public TagResolver senderPlaceholders(MentionCandidate candidate) { return TagResolver.empty(); }
    }

    @Test
    void emptyRegistryHasNoCandidates() {
        assertTrue(new MentionRegistry().candidatesFor(null).isEmpty());
    }

    @Test
    void candidatesOfAllEnabledTypesAreMerged() {
        FakeType everyone = new FakeType("everyone", true, "@everyone");
        FakeType player = new FakeType("player", true, "Alex", "Steve");

        Map<String, MentionCandidate> candidates = new MentionRegistry(everyone, player).candidatesFor(null);

        assertEquals(3, candidates.size());
        assertSame(everyone, candidates.get("@everyone").type());
        assertSame(player, candidates.get("Alex").type());
        assertSame(player, candidates.get("Steve").type());
    }

    @Test
    void disabledTypeOffersNothing() {
        FakeType everyone = new FakeType("everyone", false, "@everyone");
        FakeType player = new FakeType("player", true, "Alex");

        Map<String, MentionCandidate> candidates = new MentionRegistry(everyone, player).candidatesFor(null);

        assertEquals(List.of("Alex"), List.copyOf(candidates.keySet()));
    }

    @Test
    void firstRegisteredTypeWinsAKeywordConflict() {
        FakeType everyone = new FakeType("everyone", true, "@everyone");
        FakeType player = new FakeType("player", true, "@everyone", "@Alex");

        Map<String, MentionCandidate> candidates = new MentionRegistry(everyone, player).candidatesFor(null);

        assertSame(everyone, candidates.get("@everyone").type());
        assertEquals("everyone:@everyone", candidates.get("@everyone").targetId());
        assertSame(player, candidates.get("@Alex").type());
    }

    @Test
    void disabledTypeDoesNotBlockAConflictingKeyword() {
        FakeType everyone = new FakeType("everyone", false, "@everyone");
        FakeType player = new FakeType("player", true, "@everyone");

        assertSame(player, new MentionRegistry(everyone, player).candidatesFor(null).get("@everyone").type());
    }

    @Test
    void candidatesAreAskedForOnEveryCall() {
        // Types read live state (online players, config), so the registry must not cache.
        FakeType player = new FakeType("player", true, "Alex");
        MentionRegistry registry = new MentionRegistry(player);

        registry.candidatesFor(null);
        registry.candidatesFor(null);

        assertEquals(2, player.candidateCalls);
    }
}
