package org.nandayo.DMentions.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.GUIManager.Button;
import org.nandayo.DAPI.GUIManager.Menu;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DMentions.Main;
import org.nandayo.DMentions.utils.GUIManager;

import java.util.List;

public class SubDisabledWorldsMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public SubDisabledWorldsMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.createInventory(54, "&8Disabled Worlds");

        int i = 0;
        /*
         * List worlds that are disabled.
         */
        List<String> disabledList = manager.getUStringList("disabled_worlds");
        for(String world : disabledList) {
            this.addButton(new Button(i++) {
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name("&3" + world)
                            .lore("&eClick to remove from disabled worlds!")
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
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.GREEN_BANNER)
                            .name("&3" + world)
                            .lore("&eClick to add to disabled worlds!")
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
