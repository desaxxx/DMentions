package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LP;
import org.nandayo.dmentions.enumeration.MentionType;

import java.util.List;
import java.util.Set;

public class SubMentionAddGroupMenu extends BaseMenu {

    public SubMentionAddGroupMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    void open() {
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.add_group_menu");
        createInventory(MenuType.CHEST_6_ROWS, LANGUAGE_MANAGER.getString(menuSection, "title"));

        /*
         * List groups that are not added in MentionInsideSettings.
         */
        ConfigurationSection section = config.getUnsavedConfig().getConfigurationSection("group.list");
        List<String> disabledGroups = config.getUnsavedConfig().getStringList("group.disabled_groups");
        if(LP.isConnected() && section != null) {
            int i = 0;
            for(String group : LP.getGroups()) {
                if(section.contains(group) || disabledGroups.contains(group)) continue;
                int slot = i++;

                addButton(new Button() {
                    final String langPathName = "not_added_group";

                    @Override
                    public @NotNull Set<Integer> getSlots() {
                        return Sets.newHashSet(slot);
                    }

                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.YELLOW_BANNER)
                                .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                        config.getUnsavedConfig().set("group.list." + group + ".sound", "block.note_block.pling");
                        config.getUnsavedConfig().set("group.list." + group + ".display", "<#73c7dc>@{group}");
                        config.getUnsavedConfig().set("group.list." + group + ".cooldown", 5);
                        new MentionTypeSettingsMenu(plugin, player, MentionType.GROUP, group);
                    }
                });
            }
        }

        /*
         * Back
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(45);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ARROW)
                        .name(LANGUAGE_MANAGER.getString("menu.back.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionSettingsMenu(plugin, player);
            }
        });

        /*
         * Close
         */
        runOnClose(inv -> plugin.setGuiConfigEditor(null));

        displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
