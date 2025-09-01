package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.enumeration.MentionType;

import java.util.Set;

public class SubChooseGroupMenu extends BaseMenu {

    public SubChooseGroupMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.choose_group_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

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
                                .name(guiRegistry.getString(menuSection, "group.display_name")
                                        .replace("{group}", group)
                                )
                                .lore(guiRegistry.getStringList(menuSection, "group.lore"))
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
                        .name(guiRegistry.getString("menu.back.display_name"))
                        .lore(guiRegistry.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionSettingsMenu(plugin, player);
            }
        });


        this.displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
