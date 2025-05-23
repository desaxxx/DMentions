package org.nandayo.dmentions.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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

import java.util.List;

public class SubDisabledWorldsMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public SubDisabledWorldsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.disabled_worlds_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection, "title"));

        int i = 0;
        /*
         * List worlds that are disabled.
         */
        List<String> disabledList = config.getUnsavedConfig().getStringList("disabled_worlds");
        for(String world : disabledList) {
            this.addButton(new Button(i++) {
                final String langPathName = "disabled_world";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                    .replace("{world}", world)
                            )
                            .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, ClickType clickType) {
                    disabledList.remove(world);
                    config.getUnsavedConfig().set("disabled_worlds", disabledList);
                    new SubDisabledWorldsMenu(plugin, player);
                }
            });
        }

        /*
         * List worlds that are not disabled.
         */
        i = i - (i % 9) + 9;
        for(World w : Bukkit.getWorlds()) {
            String world = w.getName();
            if(disabledList.contains(world)) continue;

            this.addButton(new Button(i++) {
                final String langPathName = "non-disabled_world";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.GREEN_BANNER)
                            .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                    .replace("{world}", world)
                            )
                            .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, ClickType clickType) {
                    disabledList.add(world);
                    config.getUnsavedConfig().set("disabled_worlds", disabledList);
                    new SubDisabledWorldsMenu(plugin, player);
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
