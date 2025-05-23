package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

public class SubLanguageMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public SubLanguageMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.language_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection, "title"));

        int i = 0;
        for(String lang : LANGUAGE_MANAGER.REGISTERED_LANGUAGES) {
            this.addButton(new Button(i++) {
                final String langPathName = "language";
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
                public void onClick(@NotNull Player p, ClickType clickType) {
                    config.getUnsavedConfig().set("lang_file", lang);
                    new GeneralSettingsMenu(plugin, player);
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
                        .name(LANGUAGE_MANAGER.getString("menu.back.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
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
