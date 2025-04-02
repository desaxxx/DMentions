package org.nandayo.DMentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.guimanager.Button;
import org.nandayo.DAPI.guimanager.Menu;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DMentions.DMentions;
import org.nandayo.DMentions.service.GUIManager;
import org.nandayo.DMentions.service.LanguageManager;

import java.util.List;

@SuppressWarnings("unchecked")
public class SubLanguageMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;

    public SubLanguageMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.language_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

        int i = 0;
        for(String lang : plugin.LANGUAGE_MANAGER.DEFAULT_LANGUAGE_LIST) {
            this.addButton(new Button(i++) {
                final String langPathName = "language";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BOOK)
                            .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                    .replace("{language}", lang)
                                    .get()[0]
                            )
                            .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    manager.setUValue("lang_file", lang);
                    new GeneralSettingsMenu(plugin, player, manager);
                }
            });
        }

        /*
         * Back
         */
        this.addButton(new Button(45) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ARROW)
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.back.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new GeneralSettingsMenu(plugin, player, manager);
            }
        });

        /*
         * Close
         */
        this.runOnClose(inv -> plugin.GUI_CONFIG_EDITOR = null);

        this.displayTo(player);
        plugin.GUI_CONFIG_EDITOR = player;
    }
}
