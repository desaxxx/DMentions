package dev.dash.dmentions.mention;

import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentionRegistryTest {

    /** A type that only knows its id, whether it is enabled and which keywords it offers. */
    private static final class FakeType implements MentionType {
        private final String id;
        private final boolean enabled;
        private final List<String> keywords;
        String permission = "";
        int candidateCalls;

        FakeType(String id, boolean enabled, String... keywords) {
            this.id = id;
            this.enabled = enabled;
            this.keywords = Arrays.asList(keywords);
        }

        FakeType withPermission(String permission) {
            this.permission = permission;
            return this;
        }

        @Override public String id() { return id; }
        @Override public boolean enabled() { return enabled; }
        @Override public List<MentionCandidate> candidates() {
            candidateCalls++;
            return keywords.stream().map(k -> new MentionCandidate(k, this, id + ":" + k, k)).toList();
        }
        @Override public Component display(MentionCandidate candidate) { return Component.text(candidate.keyword()); }
        @Override public String permission() { return permission; }
        @Override public PermissionDefault permissionDefault() { return PermissionDefault.TRUE; }
        @Override public Duration cooldown() { return Duration.ZERO; }
        @Override public Optional<Sound> sound() { return Optional.empty(); }
        @Override public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) { return List.of(); }
        @Override public ConfigKey<List<Notification>> senderMessage() { return null; }
        @Override public ConfigKey<List<Notification>> targetMessage() { return null; }
        @Override public TagResolver senderPlaceholders(MentionCandidate candidate) { return TagResolver.empty(); }
        @Override public TagResolver targetPlaceholders(MentionCandidate candidate) { return TagResolver.empty(); }
    }

    /** A type whose permission depends on the candidate, like group mentions. */
    private static final class PerCandidatePermissionType implements MentionType {
        private final List<String> keywords;

        PerCandidatePermissionType(String... keywords) {
            this.keywords = Arrays.asList(keywords);
        }

        @Override public String id() { return "group"; }
        @Override public boolean enabled() { return true; }
        @Override public List<MentionCandidate> candidates() {
            return keywords.stream().map(k -> new MentionCandidate("@" + k, this, k, k)).toList();
        }
        @Override public Component display(MentionCandidate candidate) { return Component.text(candidate.keyword()); }
        @Override public String permission() { return ""; }
        @Override public String permission(MentionCandidate candidate) { return "test.group." + candidate.label(); }
        @Override public PermissionDefault permissionDefault() { return PermissionDefault.OP; }
        @Override public Duration cooldown() { return Duration.ZERO; }
        @Override public Optional<Sound> sound() { return Optional.empty(); }
        @Override public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) { return List.of(); }
        @Override public ConfigKey<List<Notification>> senderMessage() { return null; }
        @Override public ConfigKey<List<Notification>> targetMessage() { return null; }
        @Override public TagResolver senderPlaceholders(MentionCandidate candidate) { return TagResolver.empty(); }
        @Override public TagResolver targetPlaceholders(MentionCandidate candidate) { return TagResolver.empty(); }
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

    @Test
    void typesKeepsRegistrationOrderAndIsReadOnly() {
        FakeType everyone = new FakeType("everyone", true);
        FakeType player = new FakeType("player", true);
        MentionRegistry registry = new MentionRegistry(everyone, player);

        assertEquals(List.of(everyone, player), registry.types());
        assertThrows(UnsupportedOperationException.class, () -> registry.types().add(player));
    }

    @Nested
    class Permissions {

        private ServerMock server;
        private Plugin plugin;
        private PlayerMock alex;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
            server = MockBukkit.mock();
            plugin = MockBukkit.createMockPlugin();
            alex = server.addPlayer("Alex");
        }

        @AfterEach
        void tearDown() {
            MockBukkit.unmock();
        }

        @Test
        void senderWithoutPermissionGetsNoCandidatesOfThatType() {
            FakeType everyone = new FakeType("everyone", true, "@everyone").withPermission("test.everyone");
            FakeType player = new FakeType("player", true, "Steve");

            Map<String, MentionCandidate> candidates = new MentionRegistry(everyone, player).candidatesFor(alex);

            assertEquals(List.of("Steve"), List.copyOf(candidates.keySet()));
        }

        @Test
        void permissionIsCheckedPerCandidate() {
            PerCandidatePermissionType groups = new PerCandidatePermissionType("vip", "staff");
            alex.addAttachment(plugin, "test.group.vip", true);

            Map<String, MentionCandidate> candidates = new MentionRegistry(groups).candidatesFor(alex);

            assertEquals(List.of("@vip"), List.copyOf(candidates.keySet()));
        }

        @Test
        void senderWithPermissionGetsCandidates() {
            FakeType everyone = new FakeType("everyone", true, "@everyone").withPermission("test.everyone");
            alex.addAttachment(plugin, "test.everyone", true);

            assertTrue(new MentionRegistry(everyone).candidatesFor(alex).containsKey("@everyone"));
        }

        @Test
        void negatedPermissionBlocksEvenAnOperator() {
            FakeType player = new FakeType("player", true, "Steve").withPermission("test.player");
            alex.setOp(true);
            alex.addAttachment(plugin, "test.player", false);

            assertTrue(new MentionRegistry(player).candidatesFor(alex).isEmpty());
        }

        @Test
        void forbiddenTypeDoesNotBlockAConflictingKeyword() {
            FakeType everyone = new FakeType("everyone", true, "@everyone").withPermission("test.everyone");
            FakeType player = new FakeType("player", true, "@everyone");

            assertSame(player, new MentionRegistry(everyone, player).candidatesFor(alex).get("@everyone").type());
        }
    }
}
