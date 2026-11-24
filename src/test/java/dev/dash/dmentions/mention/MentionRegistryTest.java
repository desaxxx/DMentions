package dev.dash.dmentions.mention;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.message.Notification;
import dev.dash.dmentions.status.NoPlayerStatusProvider;
import dev.yoshi.xconfig.XConfig;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import dev.dash.dmentions.storage.UserRepository;
import dev.dash.dmentions.user.UserData;
import dev.dash.dmentions.user.UserService;
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
import java.util.UUID;
import java.util.logging.Logger;

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

        /** Makes the candidate for this keyword point at the given target id instead of "id:keyword". */
        FakeType withTarget(String keyword, String targetId) {
            targetIds.put(keyword, targetId);
            return this;
        }

        private final Map<String, String> targetIds = new java.util.HashMap<>();

        /** Makes the candidate for this keyword point at a single online player, like a player mention. */
        FakeType withSingleTarget(String keyword, Player target) {
            singleTargets.put(keyword, target);
            return withTarget(keyword, target.getUniqueId().toString());
        }

        FakeType ignoringToggle() {
            this.respectsToggle = false;
            return this;
        }

        private final Map<String, Player> singleTargets = new java.util.HashMap<>();
        private boolean respectsToggle = true;

        @Override public boolean respectsToggle() { return respectsToggle; }
        @Override public Optional<Player> singleTarget(MentionCandidate candidate) {
            return Optional.ofNullable(singleTargets.get(candidate.keyword()));
        }

        @Override public String id() { return id; }
        @Override public boolean enabled() { return enabled; }
        @Override public List<MentionCandidate> candidates() {
            candidateCalls++;
            return keywords.stream()
                    .map(k -> new MentionCandidate(k, this, targetIds.getOrDefault(k, id + ":" + k), k))
                    .toList();
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

    /** Keeps nothing: the filter only needs the cached toggle state. */
    private static final UserRepository NO_STORAGE = new UserRepository() {
        @Override public void init() {}
        @Override public Optional<UserData> load(UUID userId) { return Optional.empty(); }
        @Override public void save(UserData userData) {}
        @Override public void close() {}
    };

    private ServerMock server;
    private Plugin plugin;
    private PlayerMock alex;
    private UserService userService;
    private TargetFilter filter;

    @TempDir
    Path dir;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        alex = server.addPlayer("Alex");
        userService = new UserService(NO_STORAGE, Logger.getLogger("MentionRegistryTest"));
        XConfig config = XConfig.builder(dir.resolve("config.yml")).keys(ConfigKeys.class)
                .logger(Logger.getLogger("MentionRegistryTest")).build();
        filter = new TargetFilter(config, userService, new NoPlayerStatusProvider());
    }

    @AfterEach
    void tearDown() {
        userService.shutdown();
        MockBukkit.unmock();
    }

    @Test
    void emptyRegistryHasNoCandidates() {
        assertTrue(new MentionRegistry(filter).candidatesFor(alex).isEmpty());
    }

    @Test
    void candidatesOfAllEnabledTypesAreMerged() {
        FakeType everyone = new FakeType("everyone", true, "@everyone");
        FakeType player = new FakeType("player", true, "Alex", "Steve");

        Map<String, MentionCandidate> candidates = new MentionRegistry(filter, everyone, player).candidatesFor(alex);

        assertEquals(3, candidates.size());
        assertSame(everyone, candidates.get("@everyone").type());
        assertSame(player, candidates.get("Alex").type());
        assertSame(player, candidates.get("Steve").type());
    }

    @Test
    void disabledTypeOffersNothing() {
        FakeType everyone = new FakeType("everyone", false, "@everyone");
        FakeType player = new FakeType("player", true, "Alex");

        Map<String, MentionCandidate> candidates = new MentionRegistry(filter, everyone, player).candidatesFor(alex);

        assertEquals(List.of("Alex"), List.copyOf(candidates.keySet()));
    }

    @Test
    void firstRegisteredTypeWinsAKeywordConflict() {
        FakeType everyone = new FakeType("everyone", true, "@everyone");
        FakeType player = new FakeType("player", true, "@everyone", "@Alex");

        Map<String, MentionCandidate> candidates = new MentionRegistry(filter, everyone, player).candidatesFor(alex);

        assertSame(everyone, candidates.get("@everyone").type());
        assertEquals("everyone:@everyone", candidates.get("@everyone").targetId());
        assertSame(player, candidates.get("@Alex").type());
    }

    @Test
    void disabledTypeDoesNotBlockAConflictingKeyword() {
        FakeType everyone = new FakeType("everyone", false, "@everyone");
        FakeType player = new FakeType("player", true, "@everyone");

        assertSame(player, new MentionRegistry(filter, everyone, player).candidatesFor(alex).get("@everyone").type());
    }

    @Test
    void candidatesAreAskedForOnEveryCall() {
        // Types read live state (online players, config), so the registry must not cache.
        FakeType player = new FakeType("player", true, "Alex");
        MentionRegistry registry = new MentionRegistry(filter, player);

        registry.candidatesFor(alex);
        registry.candidatesFor(alex);

        assertEquals(2, player.candidateCalls);
    }

    @Test
    void typesKeepsRegistrationOrderAndIsReadOnly() {
        FakeType everyone = new FakeType("everyone", true);
        FakeType player = new FakeType("player", true);
        MentionRegistry registry = new MentionRegistry(filter, everyone, player);

        assertEquals(List.of(everyone, player), registry.types());
        assertThrows(UnsupportedOperationException.class, () -> registry.types().add(player));
    }

    @Test
    void candidatePointingAtTheSenderIsNotOffered() {
        FakeType player = new FakeType("player", true, "Alex", "Steve")
                .withTarget("Alex", alex.getUniqueId().toString());

        Map<String, MentionCandidate> candidates = new MentionRegistry(filter, player).candidatesFor(alex);

        assertEquals(List.of("Steve"), List.copyOf(candidates.keySet()));
    }

    @Test
    void selfCandidateIsOnlyHiddenFromThatSender() {
        PlayerMock steve = server.addPlayer("Steve");
        FakeType player = new FakeType("player", true, "Alex")
                .withTarget("Alex", alex.getUniqueId().toString());

        assertTrue(new MentionRegistry(filter, player).candidatesFor(steve).containsKey("Alex"));
    }

    @Nested
    class Permissions {

        @Test
        void senderWithoutPermissionGetsNoCandidatesOfThatType() {
            FakeType everyone = new FakeType("everyone", true, "@everyone").withPermission("test.everyone");
            FakeType player = new FakeType("player", true, "Steve");

            Map<String, MentionCandidate> candidates = new MentionRegistry(filter, everyone, player).candidatesFor(alex);

            assertEquals(List.of("Steve"), List.copyOf(candidates.keySet()));
        }

        @Test
        void permissionIsCheckedPerCandidate() {
            PerCandidatePermissionType groups = new PerCandidatePermissionType("vip", "staff");
            alex.addAttachment(plugin, "test.group.vip", true);

            Map<String, MentionCandidate> candidates = new MentionRegistry(filter, groups).candidatesFor(alex);

            assertEquals(List.of("@vip"), List.copyOf(candidates.keySet()));
        }

        @Test
        void senderWithPermissionGetsCandidates() {
            FakeType everyone = new FakeType("everyone", true, "@everyone").withPermission("test.everyone");
            alex.addAttachment(plugin, "test.everyone", true);

            assertTrue(new MentionRegistry(filter, everyone).candidatesFor(alex).containsKey("@everyone"));
        }

        @Test
        void negatedPermissionBlocksEvenAnOperator() {
            FakeType player = new FakeType("player", true, "Steve").withPermission("test.player");
            alex.setOp(true);
            alex.addAttachment(plugin, "test.player", false);

            assertTrue(new MentionRegistry(filter, player).candidatesFor(alex).isEmpty());
        }

        @Test
        void forbiddenTypeDoesNotBlockAConflictingKeyword() {
            FakeType everyone = new FakeType("everyone", true, "@everyone").withPermission("test.everyone");
            FakeType player = new FakeType("player", true, "@everyone");

            assertSame(player, new MentionRegistry(filter, everyone, player).candidatesFor(alex).get("@everyone").type());
        }
    }

    // ---- target filter: toggle and restricted ----

    @Test
    void targetWhoTurnedMentionsOffIsNotOffered() {
        PlayerMock steve = server.addPlayer("Steve");
        FakeType player = new FakeType("player", true, "Steve").withSingleTarget("Steve", steve);
        userService.setMentionable(steve.getUniqueId(), false);

        assertTrue(new MentionRegistry(filter, player).candidatesFor(alex).isEmpty());
    }

    @Test
    void restrictedTargetIsOnlyOfferedToSendersWithBypass() {
        PlayerMock steve = server.addPlayer("Steve");
        steve.addAttachment(plugin, TargetFilter.RESTRICTED, true);
        MentionRegistry registry = new MentionRegistry(filter, new FakeType("player", true, "Steve").withSingleTarget("Steve", steve));

        assertTrue(registry.candidatesFor(alex).isEmpty());

        alex.addAttachment(plugin, TargetFilter.RESTRICTED_BYPASS, true);
        assertTrue(registry.candidatesFor(alex).containsKey("Steve"));
    }

    @Test
    void typeThatIgnoresTheToggleIgnoresRestrictedToo() {
        PlayerMock steve = server.addPlayer("Steve");
        steve.addAttachment(plugin, TargetFilter.RESTRICTED, true);
        userService.setMentionable(steve.getUniqueId(), false);
        FakeType type = new FakeType("player", true, "Steve").withSingleTarget("Steve", steve).ignoringToggle();

        assertTrue(new MentionRegistry(filter, type).candidatesFor(alex).containsKey("Steve"));
    }
}
