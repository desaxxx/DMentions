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

import java.util.Set;

public class SubLanguageMenu extends BaseMenu {

    public SubLanguageMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    void open() {
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.language_menu");
        createInventory(MenuType.CHEST_6_ROWS, LANGUAGE_MANAGER.getString(menuSection, "title"));

        int i = 0;
        for(String lang : LANGUAGE_MANAGER.REGISTERED_LANGUAGES) {
            int slot = i++;
            
            addButton(new Button() {
                final String langPathName = "language";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(slot);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BOOK)
                            .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                    .replace("{language}", lang)
                            )
                            .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    config.getUnsavedConfig().set("lang_file", lang);
                    new GeneralSettingsMenu(plugin, player);
                }
            });
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
                new GeneralSettingsMenu(plugin, player);
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
