package dev.dash.dmentions.menu;

import dev.dash.dmentions.config.ConfigKeys;
import dev.dash.dmentions.group.GroupProvider;
import dev.dash.dmentions.mention.type.GroupSettings;
import dev.dash.loom.Button;
import dev.dash.loom.Click;
import dev.dash.loom.Menu;
import dev.dash.loom.Pager;
import dev.dash.loom.Ui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class ListMenus {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String[] LIST_LAYOUT = {
            "#########",
            "#ooooooo#",
            "#ooooooo#",
            "#ooooooo#",
            "#ooooooo#",
            "b<  x  >#"};

    private static final ItemStack NO_GROUPS = Icons.icon(Material.BARRIER, "<red>No groups found",
            Icons.description("Group mentions need LuckPerms.", "Without it there are no groups to list."));

    private final Settings settings;
    private final GroupProvider groups;

    private final Menu disabledWorlds;
    private final Menu disabledGroups;
    private final Menu groupOverrides;

    ListMenus(Settings settings, GroupProvider groups) {
        this.settings = settings;
        this.groups = groups;
        this.disabledWorlds = ConfigMenus.menu("config-disabled-worlds", this::renderDisabledWorlds);
        this.disabledGroups = ConfigMenus.menu("config-disabled-groups", this::renderDisabledGroups);
        this.groupOverrides = ConfigMenus.menu("config-group-overrides", this::renderGroupOverrides);
    }

    // ---------------------------------------------------------------- entry buttons

    Button disabledWorldsButton() {
        List<String> worlds = settings.config().get(ConfigKeys.DISABLED_WORLDS);
        return Button.of(summary(Material.GRASS_BLOCK, "<white>Worlds without mentions",
                worlds, "Nobody can mention anyone", "in these worlds."), c -> c.open(disabledWorlds));
    }

    Button disabledGroupsButton() {
        List<String> names = settings.config().get(ConfigKeys.GROUP_DISABLED_GROUPS);
        return Button.of(summary(Material.IRON_BARS, "<white>Disabled groups",
                names, "Groups that cannot be mentioned."), c -> c.open(disabledGroups));
    }

    Button groupOverridesButton() {
        List<String> names = List.copyOf(settings.config().get(ConfigKeys.GROUP_LIST).keySet());
        return Button.of(summary(Material.WRITABLE_BOOK, "<white>Per-group settings",
                names, "Own display, sound or cooldown", "for single groups."), c -> c.open(groupOverrides));
    }

    private static ItemStack summary(Material material, String name, List<String> entries, String... description) {
        List<Component> lore = new ArrayList<>(Icons.description(description));
        lore.add(Component.empty());
        if (entries.isEmpty()) {
            lore.add(Icons.line("<dark_gray>None"));
        } else {
            int shown = Math.min(entries.size(), 5);
            for (String entry : entries.subList(0, shown)) {
                lore.add(Icons.line("<dark_gray>- <white>").append(Component.text(entry)));
            }
            if (entries.size() > shown) lore.add(Icons.line("<dark_gray>... and " + (entries.size() - shown) + " more"));
        }
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to open"));
        return Icons.icon(material, name, lore);
    }

    // ---------------------------------------------------------------- disabled worlds

    private void renderDisabledWorlds(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>Settings › Worlds without mentions");
        ui.layout(LIST_LAYOUT);
        ui.background(Icons.FILLER);

        List<String> disabled = settings.config().get(ConfigKeys.DISABLED_WORLDS);
        // loaded worlds first, then names in the config that are not loaded (e.g. a typo)
        Set<String> names = new LinkedHashSet<>();
        Bukkit.getWorlds().forEach(world -> names.add(world.getName()));
        names.addAll(disabled);

        Pager pager = ui.paginate('o', List.copyOf(names), name -> worldButton(name, disabled.contains(name)));
        pages(ui, pager);
    }

    private Button worldButton(String name, boolean disabled) {
        World world = Bukkit.getWorld(name);
        Material material = world == null ? Material.BARRIER : switch (world.getEnvironment()) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.GRASS_BLOCK;
        };
        List<Component> lore = new ArrayList<>();
        if (world == null) lore.add(Icons.line("<gray>Not loaded on this server."));
        lore.add(Icons.line("<gray>Mentions: " + (disabled ? "<red>Blocked" : "<green>Allowed")));
        lore.add(Component.empty());
        lore.add(Icons.line(world == null ? "<yellow>Click to remove from the list"
                : disabled ? "<yellow>Click to allow mentions" : "<yellow>Click to block mentions"));
        ItemStack icon = Icons.icon(material, (disabled ? "<red>" : "<green>") + MM.escapeTags(name), lore);
        return Button.of(icon, c -> settings.save(c, ConfigKeys.DISABLED_WORLDS,
                toggled(settings.config().get(ConfigKeys.DISABLED_WORLDS), name)));
    }

    // ---------------------------------------------------------------- disabled groups

    private void renderDisabledGroups(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>Settings › Disabled groups");
        ui.layout(LIST_LAYOUT);
        ui.background(Icons.FILLER);

        List<String> disabled = settings.config().get(ConfigKeys.GROUP_DISABLED_GROUPS);
        Set<String> names = knownGroups();
        names.addAll(disabled);
        if (names.isEmpty()) {
            ui.put(22, NO_GROUPS);
        }
        Pager pager = ui.paginate('o', List.copyOf(names), name -> {
            boolean off = containsIgnoreCase(disabled, name);
            boolean known = groups.groups().stream().anyMatch(name::equalsIgnoreCase);
            List<Component> lore = new ArrayList<>();
            if (!known) lore.add(Icons.line("<gray>Not a group of the permission plugin."));
            lore.add(Icons.line("<gray>Mentions: " + (off ? "<red>Disabled" : "<green>Allowed")));
            lore.add(Component.empty());
            lore.add(Icons.line(!known ? "<yellow>Click to remove from the list"
                    : off ? "<yellow>Click to allow mentions" : "<yellow>Click to disable mentions"));
            ItemStack icon = Icons.icon(known ? Material.NAME_TAG : Material.BARRIER,
                    (off ? "<red>" : "<green>") + MM.escapeTags(name), lore);
            return Button.of(icon, c -> settings.save(c, ConfigKeys.GROUP_DISABLED_GROUPS,
                    toggled(settings.config().get(ConfigKeys.GROUP_DISABLED_GROUPS), name)));
        });
        pages(ui, pager);
    }

    // ---------------------------------------------------------------- group overrides

    private void renderGroupOverrides(Ui ui) {
        settings.watch();
        ui.title("<dark_gray>Settings › Per-group settings");
        ui.layout(LIST_LAYOUT);
        ui.background(Icons.FILLER);

        Map<String, GroupSettings> overrides = settings.config().get(ConfigKeys.GROUP_LIST);
        Set<String> names = knownGroups();
        names.addAll(overrides.keySet());
        if (names.isEmpty()) {
            ui.put(22, NO_GROUPS);
        }
        Pager pager = ui.paginate('o', List.copyOf(names), name -> {
            GroupSettings own = find(overrides, name);
            List<Component> lore = new ArrayList<>();
            if (own == null) {
                lore.add(Icons.line("<gray>Uses the group mention settings."));
            } else {
                lore.add(overrideLine("Display", own.display()));
                lore.add(overrideLine("Sound", own.sound()));
                lore.add(overrideLine("Cooldown", own.cooldown() == null ? null : format(own.cooldown())));
            }
            lore.add(Component.empty());
            lore.add(Icons.line("<yellow>Click to edit"));
            ItemStack icon = Icons.icon(own == null ? Material.PAPER : Material.WRITABLE_BOOK,
                    "<white>" + MM.escapeTags(name), lore);
            return Button.of(icon, c -> c.open(ConfigMenus.menu("config-group-override", u -> renderOverride(u, name))));
        });
        pages(ui, pager);
    }

    private static Component overrideLine(String label, String value) {
        return value == null ? Icons.line("<gray>" + label + ": <dark_gray>default") : Icons.value(label, value);
    }

    private void renderOverride(Ui ui, String groupName) {
        settings.watch();
        ui.title("<dark_gray>Group › " + MM.escapeTags(groupName));
        ui.layout(
                "#########",
                "##d s c##",
                "####r####",
                "b###x####");
        ui.background(Icons.FILLER);

        GroupSettings own = find(settings.config().get(ConfigKeys.GROUP_LIST), groupName);
        GroupSettings current = own == null ? new GroupSettings(null, null, null) : own;

        String display = current.display();
        String fallbackDisplay = settings.config().get(ConfigKeys.GROUP_DISPLAY);
        List<Component> displayLore = valueLore(display, fallbackDisplay, "Left click to edit");
        displayLore.add(0, Component.text("Preview: ").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                .append(MM.deserialize(display != null ? display : fallbackDisplay, Placeholder.unparsed("group", groupName))));
        ui.put('d', Icons.icon(Material.PAINTING, "<white>Display", displayLore), c -> {
            if (c.isRight()) {
                change(c, groupName, s -> new GroupSettings(null, s.sound(), s.cooldown()));
                return;
            }
            Settings.prompt(c.player(), "Display of " + groupName, display != null ? display : fallbackDisplay, typed ->
                    typed.isBlank() ? "The value cannot be empty."
                            : saveOverride(groupName, s -> new GroupSettings(typed, s.sound(), s.cooldown())));
        });

        String sound = current.sound();
        String fallbackSound = settings.config().get(ConfigKeys.GROUP_SOUND);
        List<Component> soundLore = valueLore(sound, fallbackSound, "Left click to edit");
        soundLore.add(Icons.line("<yellow>Shift + left click to listen"));
        ui.put('s', Icons.icon(Material.NOTE_BLOCK, "<white>Sound", soundLore), c -> {
            if (c.isRight()) {
                change(c, groupName, s -> new GroupSettings(s.display(), null, s.cooldown()));
            } else if (c.isShift()) {
                Settings.play(c, sound != null ? sound : fallbackSound);
            } else {
                Settings.prompt(c.player(), "Sound of " + groupName, sound != null ? sound : fallbackSound, typed -> {
                    String error = Settings.checkSound(typed);
                    return error != null ? error
                            : saveOverride(groupName, s -> new GroupSettings(s.display(), typed.strip(), s.cooldown()));
                });
            }
        });

        Duration cooldown = current.cooldown();
        Duration fallbackCooldown = settings.config().get(ConfigKeys.GROUP_COOLDOWN);
        List<Component> cooldownLore = valueLore(cooldown == null ? null : format(cooldown), format(fallbackCooldown),
                "Left click to edit, e.g. 10m 5s");
        ui.put('c', Icons.icon(Material.CLOCK, "<white>Cooldown", cooldownLore), c -> {
            if (c.isRight()) {
                change(c, groupName, s -> new GroupSettings(s.display(), s.sound(), null));
                return;
            }
            Settings.prompt(c.player(), "Cooldown of " + groupName, format(cooldown != null ? cooldown : fallbackCooldown),
                    typed -> {
                        Duration parsed = Settings.parseDuration(typed);
                        return parsed == null ? Settings.INVALID_DURATION
                                : saveOverride(groupName, s -> new GroupSettings(s.display(), s.sound(), parsed));
                    });
        });

        if (own != null) {
            ui.put('r', REMOVE_OVERRIDE, c -> change(c, groupName, s -> new GroupSettings(null, null, null)));
        }
        ConfigMenus.back(ui);
    }

    private static final ItemStack REMOVE_OVERRIDE = Icons.icon(Material.LAVA_BUCKET, "<red>Use the defaults",
            Icons.description("Removes every own setting", "of this group."));

    private static List<Component> valueLore(String value, String fallback, String editHint) {
        List<Component> lore = new ArrayList<>();
        lore.add(value == null
                ? Icons.line("<gray>Value: <dark_gray>default (").append(Component.text(fallback)).append(Component.text(")"))
                : Icons.value("Value", value));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>" + editHint));
        lore.add(Icons.line("<yellow>Right click: use the default"));
        return lore;
    }

    private void change(Click c, String groupName, Function<GroupSettings, GroupSettings> edit) {
        String error = saveOverride(groupName, edit);
        if (error != null) c.deny("<red>" + error);
    }

    // an entry left with nothing of its own is removed; returns an error or null
    private String saveOverride(String groupName, Function<GroupSettings, GroupSettings> edit) {
        Map<String, GroupSettings> overrides = new LinkedHashMap<>(settings.config().get(ConfigKeys.GROUP_LIST));
        String key = overrides.keySet().stream().filter(groupName::equalsIgnoreCase).findFirst().orElse(groupName);
        GroupSettings before = overrides.getOrDefault(key, new GroupSettings(null, null, null));
        GroupSettings after = edit.apply(before);
        if (after.display() == null && after.sound() == null && after.cooldown() == null) {
            overrides.remove(key);
        } else {
            overrides.put(key, after);
        }
        return settings.set(ConfigKeys.GROUP_LIST, overrides) ? null : Settings.SAVE_FAILED;
    }

    // ---------------------------------------------------------------- helpers

    private Set<String> knownGroups() {
        Set<String> names = new LinkedHashSet<>();
        groups.groups().stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(names::add);
        return names;
    }

    private static void pages(Ui ui, Pager pager) {
        if (pager.hasPrevious()) ui.put('<', Icons.PREVIOUS_PAGE, c -> pager.previous());
        if (pager.hasNext()) ui.put('>', Icons.NEXT_PAGE, c -> pager.next());
        ConfigMenus.back(ui);
    }

    private static List<String> toggled(List<String> list, String name) {
        List<String> next = new ArrayList<>(list);
        if (!next.removeIf(name::equalsIgnoreCase)) next.add(name);
        return next;
    }

    private static boolean containsIgnoreCase(List<String> list, String name) {
        return list.stream().anyMatch(name::equalsIgnoreCase);
    }

    private static GroupSettings find(Map<String, GroupSettings> overrides, String name) {
        return overrides.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    private static String format(Duration duration) {
        return Settings.formatDuration(duration);
    }
}
