package dev.dash.dmentions.menu;

import dev.dash.dmentions.DMentions;
import dev.dash.dmentions.group.GroupProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The /dms config menus on a mocked server: clicks change config.yml and the menus show the new values. */
class ConfigMenusTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    // main menu
    private static final int GENERAL = 10, PLAYER = 12, NEARBY = 16, GROUPS = 20, STORAGE = 24, RELOAD = 39, CLOSE_MAIN = 41;
    // general menu
    private static final int MENTION_LIMIT = 16, CASE_SENSITIVE = 19, WORLDS = 23, BACK = 36;
    // mention type menus
    private static final int ENABLED = 10, COOLDOWN = 21, EXTRA_2 = 25;
    // storage menu
    private static final int STORAGE_TYPE = 10;
    // list menus: first entry
    private static final int FIRST_ENTRY = 10;
    // group override menu
    private static final int OVERRIDE_COOLDOWN = 15;

    private ServerMock server;
    private DMentions plugin;
    private PlayerMock admin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(DMentions.class);
        admin = server.addPlayer("Admin");
        admin.setOp(true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ---------------------------------------------------------------- helpers

    private void openConfig() {
        admin.performCommand("dms config");
        tick();
    }

    private void tick() {
        server.getScheduler().performTicks(2);
    }

    private void click(int slot, ClickType type) {
        InventoryView view = admin.getOpenInventory();
        InventoryAction action = type.isShiftClick() ? InventoryAction.MOVE_TO_OTHER_INVENTORY
                : type.isRightClick() ? InventoryAction.PICKUP_HALF : InventoryAction.PICKUP_ALL;
        server.getPluginManager().callEvent(new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, type, action));
        tick();
    }

    private void click(int slot) {
        click(slot, ClickType.LEFT);
    }

    private String title() {
        return PLAIN.serialize(admin.getOpenInventory().title());
    }

    private String name(int slot) {
        ItemStack item = admin.getOpenInventory().getTopInventory().getItem(slot);
        return item == null ? null : PLAIN.serialize(item.getItemMeta().displayName());
    }

    private String lore(int slot) {
        ItemStack item = admin.getOpenInventory().getTopInventory().getItem(slot);
        List<Component> lore = item == null ? null : item.getItemMeta().lore();
        return lore == null ? "" : String.join("\n", lore.stream().map(PLAIN::serialize).toList());
    }

    private YamlConfiguration file() {
        return YamlConfiguration.loadConfiguration(plugin.getDataPath().resolve("config.yml").toFile());
    }

    // ---------------------------------------------------------------- tests

    @Test
    void commandOpensTheMainMenu() {
        openConfig();

        assertEquals("DMentions settings", title());
        assertEquals("General", name(GENERAL));
        assertTrue(lore(PLAYER).contains("Status: Enabled"), lore(PLAYER));
    }

    @Test
    void consoleCannotOpenTheMenu() {
        server.dispatchCommand(server.getConsoleSender(), "dms config");

        assertEquals("[DMentions] You must be a player to use this command.",
                PLAIN.serialize(((org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock) server.getConsoleSender()).nextComponentMessage()));
    }

    @Test
    void toggleIsSavedAppliedAndShown() {
        openConfig();
        click(GENERAL);
        assertEquals("Settings › General", title());

        click(CASE_SENSITIVE);

        assertTrue(file().getBoolean("case_sensitive"));
        assertTrue(lore(CASE_SENSITIVE).contains("Status: Enabled"), lore(CASE_SENSITIVE));
        // applied like /dms reload: "steve" no longer mentions Steve
        PlayerMock steve = server.addPlayer("Steve");
        admin.closeInventory();
        tick();
        admin.performCommand("dms send steve");
        tick();
        assertEquals(0, steve.getHeardSounds().size());
    }

    @Test
    void numbersGoUpAndDownWithinTheirLimits() {
        openConfig();
        click(GENERAL);

        click(MENTION_LIMIT);
        assertEquals(3, file().getInt("mention_limit"));
        click(MENTION_LIMIT, ClickType.RIGHT);
        click(MENTION_LIMIT, ClickType.RIGHT);
        click(MENTION_LIMIT, ClickType.RIGHT);
        assertEquals(1, file().getInt("mention_limit"), "the limit stops at 1");
    }

    @Test
    void shiftRightClickResetsToTheDefault() {
        openConfig();
        click(GENERAL);
        click(MENTION_LIMIT, ClickType.SHIFT_LEFT);
        assertEquals(12, file().getInt("mention_limit"));

        click(MENTION_LIMIT, ClickType.SHIFT_RIGHT);

        assertEquals(2, file().getInt("mention_limit"));
    }

    @Test
    void cooldownIsShownReadableAndResets() {
        writeConfig("player.cooldown", "1h30m5s");
        admin.performCommand("dms reload");
        openConfig();
        click(PLAYER);
        assertEquals("Settings › Player mentions", title());
        assertTrue(lore(COOLDOWN).contains("Cooldown: 1h 30m 5s"), lore(COOLDOWN));

        click(COOLDOWN, ClickType.SHIFT_RIGHT);

        assertEquals("5s", file().getString("player.cooldown"));
        assertTrue(lore(COOLDOWN).contains("Cooldown: 5s"), lore(COOLDOWN));
    }

    @Test
    void reloadButtonHasAGlobalCooldown() {
        openConfig();
        click(RELOAD);
        writeConfig("nearby.enabled", false);

        click(RELOAD);

        assertTrue(lore(NEARBY).contains("Status: Enabled"), "the second reload within 5s is refused");
    }

    private void writeConfig(String path, Object value) {
        try {
            YamlConfiguration yaml = file();
            yaml.set(path, value);
            yaml.save(plugin.getDataPath().resolve("config.yml").toFile());
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    @Test
    void disablingAMentionTypeShowsInTheMainMenu() {
        openConfig();
        click(NEARBY);
        click(ENABLED);
        assertFalse(file().getBoolean("nearby.enabled"));

        click(BACK);

        assertEquals("DMentions settings", title());
        assertTrue(lore(NEARBY).contains("Status: Disabled"), lore(NEARBY));
    }

    @Test
    void storageTypeCyclesThroughTheOptions() {
        openConfig();
        click(STORAGE);

        click(STORAGE_TYPE);
        assertEquals("SQLITE", file().getString("storage.type"));
        click(STORAGE_TYPE, ClickType.RIGHT);
        click(STORAGE_TYPE, ClickType.RIGHT);
        assertEquals("MYSQL", file().getString("storage.type"), "going back from YAML wraps around");
    }

    @Test
    void worldsCanBeBlockedAndAllowed() {
        server.addSimpleWorld("arena");
        openConfig();
        click(GENERAL);
        click(WORLDS);
        assertEquals("Settings › Worlds without mentions", title());

        String world = server.getWorlds().getFirst().getName();
        assertEquals(world, name(FIRST_ENTRY));
        click(FIRST_ENTRY);
        assertTrue(file().getStringList("disabled_worlds").contains(world));

        click(FIRST_ENTRY);
        assertFalse(file().getStringList("disabled_worlds").contains(world));
    }

    /** Stands in for LuckPerms. */
    private static final GroupProvider VIP_ONLY = new GroupProvider() {
        @Override public List<String> groups() { return List.of("vip"); }
        @Override public boolean isMember(Player player, String group) { return false; }
    };

    @Test
    void groupCooldownIsShownAndGoesBackToTheDefault() throws Exception {
        server.getServicesManager().register(GroupProvider.class, VIP_ONLY, MockBukkit.createMockPlugin(), ServicePriority.Normal);
        writeConfig("group.list.vip.cooldown", "10m5s");
        // a fresh instance picks the provider (and the file) up in onEnable
        server.getPluginManager().disablePlugin(plugin);
        plugin = MockBukkit.load(DMentions.class);
        openConfig();
        click(GROUPS);
        click(EXTRA_2);
        assertEquals("Settings › Per-group settings", title());

        assertEquals("vip", name(FIRST_ENTRY));
        click(FIRST_ENTRY);
        assertEquals("Group › vip", title());
        assertTrue(lore(OVERRIDE_COOLDOWN).contains("Value: 10m 5s"), lore(OVERRIDE_COOLDOWN));

        click(OVERRIDE_COOLDOWN, ClickType.RIGHT);
        assertNull(file().getConfigurationSection("group.list.vip"), "an entry with nothing of its own is removed");
    }

    @Test
    void closeButtonClosesTheMenu() {
        openConfig();

        assertTrue(dev.dash.loom.Loom.isViewing(admin));

        click(CLOSE_MAIN);

        assertFalse(dev.dash.loom.Loom.isViewing(admin));
    }
}
