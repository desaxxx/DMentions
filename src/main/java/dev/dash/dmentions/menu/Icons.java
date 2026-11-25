package dev.dash.dmentions.menu;

import dev.dash.itemstack.builder.SimpleItemBuilder;
import dev.dash.loom.Loom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class Icons {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    static final ItemStack FILLER = Loom.filler(Material.BLACK_STAINED_GLASS_PANE);
    static final ItemStack BACK = Loom.item(Material.ARROW, "<yellow>Back");
    static final ItemStack CLOSE = Loom.item(Material.BARRIER, "<red>Close");
    static final ItemStack PREVIOUS_PAGE = Loom.item(Material.SPECTRAL_ARROW, "<yellow>Previous page");
    static final ItemStack NEXT_PAGE = Loom.item(Material.SPECTRAL_ARROW, "<yellow>Next page");

    private Icons() {}

    static ItemStack icon(Material material, String name, List<Component> lore) {
        return SimpleItemBuilder.of(material)
                .name(MM.deserialize(name))
                .lore(lore)
                .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES)
                .build();
    }

    static Component line(String miniMessage) {
        return MM.deserialize(miniMessage);
    }

    // the value is shown exactly as typed, its tags are not applied
    static Component value(String label, String raw) {
        return Component.text(label + ": ", NamedTextColor.GRAY)
                .append(Component.text(raw.isEmpty() ? "(empty)" : raw, NamedTextColor.WHITE));
    }

    static List<Component> description(String... lines) {
        List<Component> out = new ArrayList<>(lines.length);
        for (String text : lines) {
            out.add(Component.text(text, NamedTextColor.GRAY));
        }
        return out;
    }

    static String onOff(boolean value) {
        return value ? "<green>Enabled" : "<red>Disabled";
    }
}
