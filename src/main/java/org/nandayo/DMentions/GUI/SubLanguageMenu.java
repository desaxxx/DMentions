package org.nandayo.DMentions.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.GUIManager.Button;
import org.nandayo.DAPI.GUIManager.Menu;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DMentions.Main;
import org.nandayo.DMentions.utils.GUIManager;

public class SubLanguageMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public SubLanguageMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.createInventory(54, "&8Language Chooser");

        int i = 0;
        for(String lang : plugin.langManager.getLanguages()) {
            this.addButton(new Button(i++) {
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BOOK)
                            .name("&3" + lang)
                            .lore("&eClick to choose!")
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
                        .name("&eBack")
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
        this.runOnClose(inv -> {
            plugin.guiConfigEditor = null;
        });

        this.displayTo(player);
        plugin.guiConfigEditor = player;
    }
}
