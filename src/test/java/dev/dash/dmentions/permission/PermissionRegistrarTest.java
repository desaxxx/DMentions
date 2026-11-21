package dev.dash.dmentions.permission;

import dev.dash.dmentions.mention.MentionCandidate;
import dev.dash.dmentions.mention.MentionType;
import dev.dash.dmentions.message.Notification;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionRegistrarTest {

    /** Only id, permission and its default matter to the registrar. */
    private record FakeType(String id, String permission, PermissionDefault permissionDefault) implements MentionType {
        @Override public boolean enabled() { return true; }
        @Override public List<MentionCandidate> candidates() { return List.of(); }
        @Override public Component display(MentionCandidate candidate) { return Component.empty(); }
        @Override public Duration cooldown() { return Duration.ZERO; }
        @Override public Optional<Sound> sound() { return Optional.empty(); }
        @Override public Collection<Player> resolveTargets(Player sender, MentionCandidate candidate) { return List.of(); }
        @Override public ConfigKey<List<Notification>> senderMessage() { return null; }
        @Override public ConfigKey<List<Notification>> targetMessage() { return null; }
        @Override public TagResolver senderPlaceholders(MentionCandidate candidate) { return TagResolver.empty(); }
    }

    private ServerMock server;
    private PluginManager pluginManager;
    private PermissionRegistrar registrar;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        pluginManager = server.getPluginManager();
        registrar = new PermissionRegistrar(pluginManager);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void registersEachPermissionWithItsDefault() {
        registrar.register(List.of(
                new FakeType("player", "custom.player", PermissionDefault.TRUE),
                new FakeType("everyone", "custom.everyone", PermissionDefault.OP)));

        Permission player = pluginManager.getPermission("custom.player");
        Permission everyone = pluginManager.getPermission("custom.everyone");
        assertNotNull(player);
        assertNotNull(everyone);
        assertEquals(PermissionDefault.TRUE, player.getDefault());
        assertEquals(PermissionDefault.OP, everyone.getDefault());
    }

    @Test
    void registeredDefaultDecidesWhoHasIt() {
        registrar.register(List.of(
                new FakeType("player", "custom.player", PermissionDefault.TRUE),
                new FakeType("everyone", "custom.everyone", PermissionDefault.OP)));
        PlayerMock alex = server.addPlayer("Alex");

        assertTrue(alex.hasPermission("custom.player"));
        assertFalse(alex.hasPermission("custom.everyone"));

        alex.setOp(true);
        assertTrue(alex.hasPermission("custom.everyone"));
    }

    @Test
    void emptyPermissionIsSkipped() {
        assertDoesNotThrow(() -> registrar.register(List.of(new FakeType("player", "", PermissionDefault.TRUE))));
        assertNull(pluginManager.getPermission(""));
    }

    @Test
    void existingPermissionIsLeftUntouched() {
        Permission foreign = new Permission("shared.perm", PermissionDefault.FALSE);
        pluginManager.addPermission(foreign);

        registrar.register(List.of(new FakeType("player", "shared.perm", PermissionDefault.TRUE)));
        assertSame(foreign, pluginManager.getPermission("shared.perm"));
        assertEquals(PermissionDefault.FALSE, foreign.getDefault());

        registrar.unregisterAll();
        assertSame(foreign, pluginManager.getPermission("shared.perm"), "someone else's permission was removed");
    }

    @Test
    void twoTypesSharingAPermissionRegisterItOnce() {
        assertDoesNotThrow(() -> registrar.register(List.of(
                new FakeType("player", "shared.perm", PermissionDefault.TRUE),
                new FakeType("nearby", "shared.perm", PermissionDefault.OP))));
        assertEquals(PermissionDefault.TRUE, pluginManager.getPermission("shared.perm").getDefault());
    }

    @Test
    void unregisterAllRemovesOnlyOurPermissions() {
        pluginManager.addPermission(new Permission("other.perm"));
        registrar.register(List.of(new FakeType("player", "custom.player", PermissionDefault.TRUE)));

        registrar.unregisterAll();

        assertNull(pluginManager.getPermission("custom.player"));
        assertNotNull(pluginManager.getPermission("other.perm"));
    }

    @Test
    void canRegisterAgainAfterUnregister() {
        // The reload path: unregister, config changes, register again.
        registrar.register(List.of(new FakeType("player", "old.perm", PermissionDefault.TRUE)));
        registrar.unregisterAll();
        registrar.register(List.of(new FakeType("player", "new.perm", PermissionDefault.TRUE)));

        assertNull(pluginManager.getPermission("old.perm"));
        assertNotNull(pluginManager.getPermission("new.perm"));

        registrar.unregisterAll();
        assertNull(pluginManager.getPermission("new.perm"));
    }

    @Test
    void unregisterAllTwiceIsHarmless() {
        registrar.register(List.of(new FakeType("player", "custom.player", PermissionDefault.TRUE)));
        registrar.unregisterAll();
        assertDoesNotThrow(registrar::unregisterAll);
    }
}
