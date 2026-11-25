package dev.dash.dmentions.menu;

import dev.dash.dmentions.mention.SoundSpec;
import dev.dash.loom.Button;
import dev.dash.loom.Click;
import dev.dash.loom.Prompt;
import dev.dash.loom.Signal;
import dev.yoshi.xconfig.ConfigKey;
import dev.yoshi.xconfig.XConfig;
import dev.yoshi.xconfig.adapters.DurationAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// shift + right click resets any setting to its default
final class Settings {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component RESET_HINT = Icons.line("<dark_gray>Shift + right click: reset to default");
    private static final Sound DENY = Sound.sound(Key.key("entity.villager.no"), Sound.Source.MASTER, 1f, 1f);

    private final ConfigEditor editor;
    private final Signal<Integer> version;

    Settings(ConfigEditor editor, Signal<Integer> version) {
        this.editor = editor;
        this.version = version;
    }

    XConfig config() {
        return editor.config();
    }

    // call first in every render so the menu redraws after a save or /dms reload
    void watch() {
        version.get();
    }

    // ---------------------------------------------------------------- kinds

    Button toggle(ConfigKey<Boolean> key, Material material, String name, String... description) {
        boolean value = config().get(key);
        List<Component> lore = lore(description);
        lore.add(Icons.line("<gray>Status: " + Icons.onOff(value)));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to " + (value ? "disable" : "enable")));
        lore.add(RESET_HINT);
        return Button.of(Icons.icon(material, name, lore), c -> {
            if (resetRequested(c, key)) return;
            save(c, key, !config().get(key));
        });
    }

    Button number(ConfigKey<Integer> key, Material material, String name, int min, int max, String... description) {
        int value = config().get(key);
        List<Component> lore = lore(description);
        lore.add(Icons.value("Value", String.valueOf(value)));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Left / right click: +1 / -1"));
        lore.add(Icons.line("<yellow>Shift + left click: +10"));
        lore.add(RESET_HINT);
        return Button.of(Icons.icon(material, name, lore), c -> {
            if (resetRequested(c, key)) return;
            int step = c.isShift() ? 10 : 1;
            int next = Math.clamp((long) config().get(key) + (c.isRight() ? -step : step), min, max);
            if (next == config().get(key)) {
                c.deny();
                return;
            }
            save(c, key, next);
        });
    }

    Button cooldown(ConfigKey<Duration> key, Material material, String name, String... description) {
        Duration value = config().get(key);
        List<Component> lore = lore(description);
        lore.add(Icons.value("Cooldown", formatDuration(value)));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to edit, e.g. 10m 5s"));
        lore.add(RESET_HINT);
        return Button.of(Icons.icon(material, name, lore), c -> {
            if (resetRequested(c, key)) return;
            prompt(c.player(), name, formatDuration(config().get(key)), typed -> {
                Duration parsed = parseDuration(typed);
                if (parsed == null) return INVALID_DURATION;
                return editor.set(key, parsed) ? null : SAVE_FAILED;
            });
        });
    }

    <E extends Enum<E>> Button choice(ConfigKey<E> key, Material material, String name, String... description) {
        E value = config().get(key);
        List<Component> lore = lore(description);
        for (E option : value.getDeclaringClass().getEnumConstants()) {
            lore.add(Icons.line(option == value ? "<green>▶ " + option.name() : "<dark_gray>  " + option.name()));
        }
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Left / right click: next / previous"));
        lore.add(RESET_HINT);
        return Button.of(Icons.icon(material, name, lore), c -> {
            if (resetRequested(c, key)) return;
            E[] options = config().get(key).getDeclaringClass().getEnumConstants();
            int index = config().get(key).ordinal() + (c.isRight() ? -1 : 1);
            save(c, key, options[Math.floorMod(index, options.length)]);
        });
    }

    // check returns an error to show, or null if the text is fine
    Button text(ConfigKey<String> key, Material material, String name, Function<String, String> check,
                String... description) {
        return text(key, material, name, check, null, description);
    }

    Button text(ConfigKey<String> key, Material material, String name, Function<String, String> check,
                TagResolver previewTags, String... description) {
        String value = config().get(key);
        List<Component> lore = lore(description);
        lore.add(Icons.value("Value", value));
        if (previewTags != null) {
            lore.add(Component.text("Preview: ").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                    .append(MM.deserialize(value, previewTags)));
        }
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Click to edit"));
        lore.add(RESET_HINT);
        return Button.of(Icons.icon(material, name, lore), c -> {
            if (resetRequested(c, key)) return;
            prompt(c.player(), name, config().get(key), typed -> {
                String error = check.apply(typed);
                if (error != null) return error;
                return editor.set(key, typed) ? null : SAVE_FAILED;
            });
        });
    }

    Button sound(ConfigKey<String> key, Material material, String name, String... description) {
        String value = config().get(key);
        List<Component> lore = lore(description);
        lore.add(Icons.value("Sound", value));
        lore.add(Component.empty());
        lore.add(Icons.line("<yellow>Left click to edit"));
        lore.add(Icons.line("<yellow>Right click to listen"));
        lore.add(RESET_HINT);
        return Button.of(Icons.icon(material, name, lore), c -> {
            if (resetRequested(c, key)) return;
            if (c.isRight()) {
                play(c, config().get(key));
                return;
            }
            prompt(c.player(), name, config().get(key), typed -> {
                String error = checkSound(typed);
                if (error != null) return error;
                return editor.set(key, typed.strip()) ? null : SAVE_FAILED;
            });
        });
    }

    // ---------------------------------------------------------------- shared

    static final String SAVE_FAILED = "Could not save the change. See the console.";
    static final String INVALID_DURATION = "Invalid time. Use units d, h, m, s, e.g. 10m 5s or 30s";

    // null if it cannot be read or is negative
    static Duration parseDuration(String typed) {
        try {
            Duration duration = DurationAdapter.INSTANCE.deserialize(typed, "menu");
            return duration.isNegative() ? null : duration;
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    // "1h30m5s" -> "1h 30m 5s"
    static String formatDuration(Duration duration) {
        return DurationAdapter.INSTANCE.serialize(duration).toString()
                .replaceAll("(?<=[a-z])(?=\\d)", " ");
    }

    // null when usable ("" means no sound), otherwise the error
    static String checkSound(String typed) {
        String text = typed.strip();
        if (text.isEmpty()) return null;
        return SoundSpec.parse(text).isPresent() ? null : "Unknown sound. Use key;volume;pitch, e.g. block.note_block.pling;1;1";
    }

    static void play(Click c, String spec) {
        c.silent();
        SoundSpec.parse(spec).ifPresentOrElse(sound -> c.player().playSound(sound, Sound.Emitter.self()),
                () -> c.deny("<gray>No sound is set."));
    }

    // submit returns an error to show, or null on success; Loom reopens the menu afterwards
    static void prompt(Player player, String title, String current, Function<String, String> submit) {
        Prompt.create()
                .title(title)
                .text(current)
                .open(player, typed -> {
                    String error = submit.apply(typed);
                    if (error != null) {
                        player.sendActionBar(Component.text(error, net.kyori.adventure.text.format.NamedTextColor.RED));
                        player.playSound(DENY, Sound.Emitter.self());
                    }
                });
    }

    <T> void save(Click c, ConfigKey<T> key, T value) {
        if (!editor.set(key, value)) {
            c.deny("<red>" + SAVE_FAILED);
        }
    }

    <T> boolean set(ConfigKey<T> key, T value) {
        return editor.set(key, value);
    }

    private <T> boolean resetRequested(Click c, ConfigKey<T> key) {
        if (!(c.isShift() && c.isRight())) return false;
        if (config().get(key).equals(key.defaultValue())) {
            c.deny("<gray>Already the default value.");
        } else {
            save(c, key, key.defaultValue());
        }
        return true;
    }

    private static List<Component> lore(String... description) {
        List<Component> lore = new ArrayList<>(Icons.description(description));
        if (description.length > 0) lore.add(Component.empty());
        return lore;
    }

    static Function<String, String> notBlank() {
        return typed -> typed.isBlank() ? "The value cannot be empty." : null;
    }

    static Function<String, String> mustContain(String placeholder) {
        return typed -> typed.contains(placeholder) ? null : "The value must contain " + placeholder + ".";
    }

    static Function<String, String> anything() {
        return typed -> null;
    }
}
