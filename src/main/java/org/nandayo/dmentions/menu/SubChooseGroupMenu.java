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
import org.nandayo.dmentions.enumeration.MentionType;

import java.util.Set;

public class SubChooseGroupMenu extends BaseMenu {

    public SubChooseGroupMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    void open() {
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.choose_group_menu");
        createInventory(MenuType.CHEST_6_ROWS, LANGUAGE_MANAGER.getString(menuSection, "title"));

        /*
         * List groups that are within group.list
         */
        int i = 0;
        ConfigurationSection section = config.getUnsavedConfig().getConfigurationSection("group.list");
        if(section != null) {
            for(String group : section.getKeys(false)) {
                int slot = i++;
                
                addButton(new Button() {
                    @Override
                    public @NotNull Set<Integer> getSlots() {
                        return Sets.newHashSet(slot);
                    }

                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name(LANGUAGE_MANAGER.getString(menuSection, "group.display_name")
                                        .replace("{group}", group)
                                )
                                .lore(LANGUAGE_MANAGER.getStringList(menuSection, "group.lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        this.runOnClose(inv -> plugin.setGuiConfigEditor(null));

        this.displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
