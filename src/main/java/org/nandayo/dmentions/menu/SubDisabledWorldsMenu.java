package org.nandayo.DMentions.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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
public class SubDisabledWorldsMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;

    public SubDisabledWorldsMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.disabled_worlds_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

        int i = 0;
        /*
         * List worlds that are disabled.
         */
        List<String> disabledList = manager.getUStringList("disabled_worlds");
        for(String world : disabledList) {
            this.addButton(new Button(i++) {
                final String langPathName = "disabled_world";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                    .replace("{world}", world)
                                    .get()[0]
                            )
                            .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    disabledList.remove(world);
                    manager.setUValue("disabled_worlds", disabledList);
                    new SubDisabledWorldsMenu(plugin, player, manager);
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
                            .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                    .replace("{world}", world)
                                    .get()[0]
                            )
                            .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    disabledList.add(world);
                    manager.setUValue("disabled_worlds", disabledList);
                    new SubDisabledWorldsMenu(plugin, player, manager);
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
