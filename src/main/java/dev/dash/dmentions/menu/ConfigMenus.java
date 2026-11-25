package dev.dash.dmentions.menu;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.group.GroupProvider;
import dev.dash.loom.Button;
import dev.dash.loom.Click;
import dev.dash.loom.Loom;
import dev.dash.loom.Menu;
import dev.dash.loom.Signal;
import dev.dash.loom.Ui;
import dev.yoshi.xconfig.ConfigKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

// every menu watches one shared signal, so a save (by anyone) or /dms reload redraws all open config menus
public final class ConfigMenus {

    private static final Sound CLICK = Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 0.6f, 1f);

    private static final ItemStack GENERAL = icon(Material.COMPARATOR, "<#ffd16d>General",
            "Language, prefix, limits and", "the worlds without mentions.");
    private static final ItemStack PLAYER = icon(Material.PLAYER_HEAD, "<#a9e871>Player mentions",
            "Mentioning a player by name.");
    private static final ItemStack EVERYONE = icon(Material.BELL, "<#8fb56c>@everyone",
            "Mentioning every online player.");
    private static final ItemStack NEARBY = icon(Material.COMPASS, "<#ea79b8>@nearby",
            "Mentioning the players around you.");
    private static final ItemStack GROUPS = icon(Material.NAME_TAG, "<#73c7dc>Group mentions",
            "Mentioning a LuckPerms group,", "with per-group overrides.");
    private static final ItemStack FILTERS = icon(Material.ENDER_EYE, "<#c9a0ff>Who can be mentioned",
            "Vanished, ignoring and AFK players.");
    private static final ItemStack STORAGE = icon(Material.CHEST, "<#e0c48a>Storage",
            "Where player settings are saved.", "Changes need a server restart.");
    private static final ItemStack STORAGE_NOTE = icon(Material.CLOCK, "<gold>Restart required",
            "Storage settings are saved right away,", "but only used after a server restart.",
            "/dms reload does not switch the storage.");

    private final Settings settings;
    private final GroupProvider groups;
    private final Path langFolder;
    private final BooleanSupplier reloader;
    private final Signal<Integer> version = Loom.signal("dmentions-config", 0);

    private final Menu main;
    private final Menu general;
    private final Menu filters;
    private final Menu storage;
    private final Menu player;
    private final Menu everyone;
    private final Menu nearby;
    private final Menu group;
    private final ListMenus lists;

    public ConfigMenus(ConfigEditor editor, GroupProvider groups, Path langFolder, BooleanSupplier reloader) {
        this.settings = new Settings(editor, version);
        this.groups = groups;
        this.langFolder = langFolder;
        this.reloader = reloader;
        this.lists = new ListMenus(settings, groups);

        this.main = menu("config", this::renderMain);
        this.general = menu("config-general", this::renderGeneral);
        this.filters = menu("config-filters", this::renderFilters);
        this.storage = menu("config-storage", this::renderStorage);
        this.player = menu("config-player", this::renderPlayer);
        this.everyone = menu("config-everyone", this::renderEveryone);
        this.nearby = menu("config-nearby", this::renderNearby);
        this.group = menu("config-group", this::renderGroup);
    }

    static Menu menu(String name, Consumer<Ui> renderer) {
        return Loom.menu(renderer).name(name).sound(CLICK).cooldown(2).lockPlayerInventory(true);
    }

    public void open(Player viewer) {
        main.open(viewer);
    }

    public void refresh() {
        version.update(n -> n + 1);
    }

    // ---------------------------------------------------------------- main

    private void renderMain(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>DMentions settings");
        ui.layout(
                "#########",
                "#g p e n#",
                "##r f s##",
                "#########",
                "###l x###");
        ui.background(Icons.FILLER);
        ui.put('g', GENERAL, c -> c.open(general));
        ui.put('p', status(PLAYER, ConfigKeys.PLAYER_ENABLED), c -> c.open(player));
        ui.put('e', status(EVERYONE, ConfigKeys.EVERYONE_ENABLED), c -> c.open(everyone));
        ui.put('n', status(NEARBY, ConfigKeys.NEARBY_ENABLED), c -> c.open(nearby));
        ui.put('r', status(GROUPS, ConfigKeys.GROUP_ENABLED), c -> c.open(group));
        ui.put('f', FILTERS, c -> c.open(filters));
        ui.put('s', STORAGE, c -> c.open(storage));
        ui.put('l', RELOAD, c -> {
            long now = System.nanoTime();
            long allowedAt = reloadAllowedAt.get();
            if (now - allowedAt < 0 || !reloadAllowedAt.compareAndSet(allowedAt, now + RELOAD_COOLDOWN_NANOS)) {
                long seconds = Math.max(1, (reloadAllowedAt.get() - now + 999_999_999L) / 1_000_000_000L);
                c.deny("<gray>Wait " + seconds + "s before reloading again.");
                return;
            }
            if (!reloader.getAsBoolean()) c.deny("<red>Reload failed. See the console.");
        });
        ui.put('x', Icons.CLOSE, Click::close);
    }

    private static final ItemStack RELOAD = icon(Material.LIME_DYE, "<green>Reload",
            "Reads config.yml and the language", "file again, like /dms reload.");

    // global: one cooldown for everyone using the reload button
    private static final long RELOAD_COOLDOWN_NANOS = 5_000_000_000L;
    private final AtomicLong reloadAllowedAt = new AtomicLong(System.nanoTime());

    private ItemStack status(ItemStack base, ConfigKey<Boolean> enabled) {
        ItemStack copy = base.clone();
        copy.editMeta(meta -> {
            List<Component> lore = new ArrayList<>(meta.lore() == null ? List.of() : meta.lore());
            lore.add(Component.empty());
            lore.add(Icons.line("<!italic><gray>Status: " + Icons.onOff(settings.config().get(enabled))));
            meta.lore(lore);
        });
        return copy;
    }

    // ---------------------------------------------------------------- general

    private void renderGeneral(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>Settings › General");
        ui.layout(
                "#########",
                "#l u p m#",
                "#c a w  #",
                "#########",
                "b###x####");
        ui.background(Icons.FILLER);
        ui.put('l', language());
        ui.put('u', settings.toggle(ConfigKeys.CHECK_FOR_UPDATES, Material.WRITABLE_BOOK, "<white>Update check",
                "Checks Modrinth for a new version", "when the server starts."));
        ui.put('p', settings.text(ConfigKeys.PREFIX, Material.OAK_SIGN, "<white>Prefix", Settings.anything(),
                TagResolver.empty(), "Shown as <prefix> in messages."));
        ui.put('m', settings.number(ConfigKeys.MENTION_LIMIT, Material.REPEATER, "<white>Mention limit", 1, 50,
                "How many mentions one", "message can have."));
        ui.put('c', settings.toggle(ConfigKeys.CASE_SENSITIVE, Material.LECTERN, "<white>Case sensitive",
                "Whether \"steve\" also", "mentions Steve."));
        ui.put('a', settings.toggle(ConfigKeys.ALLOW_MULTIPLE_NOTIFICATIONS, Material.GOAT_HORN, "<white>Multiple notifications",
                "If a player is mentioned twice in", "one message (\"@everyone Steve\"),",
                "notify them for each mention."));
        ui.put('w', lists.disabledWorldsButton());
        back(ui);
    }

    private Button language() {
        String current = settings.config().get(ConfigKeys.LANG_FILE);
        List<Component> lore = new ArrayList<>(Icons.description("The language of the messages.", "Files are in the lang folder."));
        lore.add(Component.empty());
        lore.add(Icons.value("Language", current));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Left / right click: next / previous"));
        return Button.of(Icons.icon(Material.BOOK, "<white>Language", lore), c -> {
            List<String> files = languageFiles();
            if (files.size() < 2) {
                c.deny("<gray>No other language file in the lang folder.");
                return;
            }
            int index = files.indexOf(settings.config().get(ConfigKeys.LANG_FILE));
            int next = Math.floorMod(index + (c.isRight() ? -1 : 1), files.size());
            settings.save(c, ConfigKeys.LANG_FILE, files.get(next));
        });
    }

    // read on click only, never while rendering
    private List<String> languageFiles() {
        try (Stream<Path> files = Files.list(langFolder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".yml"))
                    .map(name -> name.substring(0, name.length() - ".yml".length()))
                    .sorted()
                    .toList();
        } catch (java.io.IOException e) {
            return List.of();
        }
    }

    // ---------------------------------------------------------------- mention types

    private TagResolver previewTags(Player viewer) {
        return TagResolver.resolver(
                Placeholder.unparsed("player", viewer.getName()),
                Placeholder.unparsed("custom_name", "Nick"),
                Placeholder.unparsed("group", "vip"));
    }

    private void renderType(Ui ui, String title, ConfigKey<Boolean> enabled, ConfigKey<String> permission,
                            ConfigKey<String> keyword, java.util.function.Function<String, String> keywordCheck,
                            ConfigKey<String> display, ConfigKey<String> sound, ConfigKey<Duration> cooldown) {
        settings.watch();
        ui.title("<dark_gray>Settings › " + title);
        ui.layout(
                "#########",
                "#e k d s#",
                "#p c 1 2#",
                "#########",
                "b###x####");
        ui.background(Icons.FILLER);
        ui.put('e', settings.toggle(enabled, Material.LEVER, "<white>Enabled"));
        ui.put('k', settings.text(keyword, Material.NAME_TAG, "<white>Keyword", keywordCheck,
                "What players type to mention."));
        ui.put('d', settings.text(display, Material.PAINTING, "<white>Display", Settings.notBlank(),
                previewTags(ui.player()), "How the mention looks in chat."));
        ui.put('s', settings.sound(sound, Material.NOTE_BLOCK, "<white>Sound", "Played to the mentioned player."));
        ui.put('p', settings.text(permission, Material.TRIPWIRE_HOOK, "<white>Permission", Settings.anything(),
                "Needed to use this mention.", "Empty: everyone can use it."));
        ui.put('c', settings.cooldown(cooldown, Material.CLOCK, "<white>Cooldown",
                "Time between two mentions", "of this kind by one player."));
        back(ui);
    }

    private void renderPlayer(Ui ui) {
        renderType(ui, "Player mentions", ConfigKeys.PLAYER_ENABLED, ConfigKeys.PLAYER_PERMISSION,
                ConfigKeys.PLAYER_KEYWORD, Settings.mustContain("<player>"),
                ConfigKeys.PLAYER_DISPLAY, ConfigKeys.PLAYER_SOUND, ConfigKeys.PLAYER_COOLDOWN);
        ui.put('1', settings.text(ConfigKeys.PLAYER_CUSTOM_DISPLAY, Material.ITEM_FRAME, "<white>Custom name display",
                Settings.notBlank(), previewTags(ui.player()),
                "Display for players who set", "a custom name (/dms customize)."));
    }

    private void renderEveryone(Ui ui) {
        renderType(ui, "@everyone", ConfigKeys.EVERYONE_ENABLED, ConfigKeys.EVERYONE_PERMISSION,
                ConfigKeys.EVERYONE_KEYWORD, Settings.notBlank(),
                ConfigKeys.EVERYONE_DISPLAY, ConfigKeys.EVERYONE_SOUND, ConfigKeys.EVERYONE_COOLDOWN);
    }

    private void renderNearby(Ui ui) {
        renderType(ui, "@nearby", ConfigKeys.NEARBY_ENABLED, ConfigKeys.NEARBY_PERMISSION,
                ConfigKeys.NEARBY_KEYWORD, Settings.notBlank(),
                ConfigKeys.NEARBY_DISPLAY, ConfigKeys.NEARBY_SOUND, ConfigKeys.NEARBY_COOLDOWN);
        ui.put('1', settings.number(ConfigKeys.NEARBY_RADIUS, Material.SPYGLASS, "<white>Radius", 1, 500,
                "Blocks around the sender,", "checked on each axis (a box)."));
    }

    private void renderGroup(Ui ui) {
        renderType(ui, "Group mentions", ConfigKeys.GROUP_ENABLED, ConfigKeys.GROUP_PERMISSION,
                ConfigKeys.GROUP_KEYWORD, Settings.mustContain("<group>"),
                ConfigKeys.GROUP_DISPLAY, ConfigKeys.GROUP_SOUND, ConfigKeys.GROUP_COOLDOWN);
        ui.put('1', lists.disabledGroupsButton());
        ui.put('2', lists.groupOverridesButton());
    }

    // ---------------------------------------------------------------- filters

    private void renderFilters(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>Settings › Who can be mentioned");
        ui.layout(
                "#########",
                "##v i a##",
                "#########",
                "b###x####");
        ui.background(Icons.FILLER);
        ui.put('v', settings.toggle(ConfigKeys.VANISH_RESPECT, Material.GLASS, "<white>Skip vanished players",
                "Vanished players get no mention,", "not even from @everyone.",
                "Works with any vanish plugin."));
        ui.put('i', settings.toggle(ConfigKeys.IGNORE_RESPECT, Material.STRUCTURE_VOID, "<white>Respect /ignore",
                "Players who ignore the sender get", "no mention, not even from @everyone.",
                "Needs Essentials."));
        ui.put('a', settings.toggle(ConfigKeys.AFK_RESPECT, Material.RED_BED, "<white>Skip AFK players",
                "AFK players get no mention,", "but @everyone still reaches them.",
                "Needs Essentials."));
        back(ui);
    }

    // ---------------------------------------------------------------- storage

    private void renderStorage(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>Settings › Storage");
        ui.layout(
                "#########",
                "#t h o n#",
                "#u w l f#",
                "####i####",
                "b###x####");
        ui.background(Icons.FILLER);
        ui.put('t', settings.choice(ConfigKeys.STORAGE_TYPE, Material.CHEST, "<white>Storage type"));
        ui.put('h', settings.text(ConfigKeys.STORAGE_HOST, Material.COMPASS, "<white>Host", Settings.notBlank(), "MySQL only."));
        ui.put('o', port());
        ui.put('n', settings.text(ConfigKeys.STORAGE_NAME, Material.BOOKSHELF, "<white>Database", Settings.notBlank(), "MySQL only."));
        ui.put('u', settings.text(ConfigKeys.STORAGE_USERNAME, Material.PLAYER_HEAD, "<white>Username", Settings.notBlank(), "MySQL only."));
        ui.put('w', password());
        ui.put('l', settings.toggle(ConfigKeys.STORAGE_USE_SSL, Material.IRON_BARS, "<white>Use SSL", "MySQL only."));
        ui.put('f', settings.text(ConfigKeys.STORAGE_PREFIX, Material.PAPER, "<white>Table prefix",
                typed -> typed.matches("[A-Za-z0-9_]*") ? null : "Only letters, numbers and _ are allowed.",
                "SQLite and MySQL."));
        ui.put('i', STORAGE_NOTE);
        back(ui);
    }

    private Button port() {
        int value = settings.config().get(ConfigKeys.STORAGE_PORT);
        List<Component> lore = new ArrayList<>(Icons.description("MySQL only."));
        lore.add(Component.empty());
        lore.add(Icons.value("Port", String.valueOf(value)));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to edit"));
        return Button.of(Icons.icon(Material.IRON_DOOR, "<white>Port", lore), c ->
                Settings.prompt(c.player(), "Port", String.valueOf(settings.config().get(ConfigKeys.STORAGE_PORT)), typed -> {
                    int port;
                    try {
                        port = Integer.parseInt(typed.strip());
                    } catch (NumberFormatException e) {
                        return "The port must be a number.";
                    }
                    if (port < 1 || port > 65535) return "The port must be between 1 and 65535.";
                    return settings.set(ConfigKeys.STORAGE_PORT, port) ? null : Settings.SAVE_FAILED;
                }));
    }

    // the password is never shown, so the anvil starts empty too
    private Button password() {
        boolean set = !settings.config().get(ConfigKeys.STORAGE_PASSWORD).isEmpty();
        List<Component> lore = new ArrayList<>(Icons.description("MySQL only."));
        lore.add(Component.empty());
        lore.add(Icons.value("Password", set ? "••••••" : ""));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to type a new one"));
        return Button.of(Icons.icon(Material.TRIAL_KEY, "<white>Password", lore), c ->
                Settings.prompt(c.player(), "New password", "", typed ->
                        settings.set(ConfigKeys.STORAGE_PASSWORD, typed) ? null : Settings.SAVE_FAILED));
    }

    // ---------------------------------------------------------------- shared

    static void back(Ui ui) {
        if (ui.canGoBack()) ui.put('b', Icons.BACK, Click::back);
        ui.put('x', Icons.CLOSE, Click::close);
    }

    private static ItemStack icon(Material material, String name, String... description) {
        List<Component> lore = new ArrayList<>(Icons.description(description));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to open"));
        return Icons.icon(material, name, lore);
    }
}
