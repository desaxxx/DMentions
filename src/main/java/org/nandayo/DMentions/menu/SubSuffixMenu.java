package org.nandayo.DMentions.GUI;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.GUIManager.Button;
import org.nandayo.DAPI.GUIManager.Menu;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DMentions.Main;
import org.nandayo.DMentions.integration.LP;
import org.nandayo.DMentions.utils.GUIManager;

public class SubSuffixMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public SubSuffixMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.createInventory(54, "&8Color Manager");

        /*
         * List groups that are within uConfigManager
         */
        int i = 0;
        ConfigurationSection section = manager.getUSection("suffix_color.group");
        for(String suffixGroup : section.getKeys(false)) {
            this.addButton(new Button(i++) {
                final String path = "suffix_color.group." + suffixGroup;
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.GREEN_BANNER)
                            .name("&3" + suffixGroup)
                            .lore("&eCurrent: &f" + manager.getValueDisplay(path, "color"),
                                    "&eLeft click to edit!",
                                    "&cRight click to remove from list!")
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    if(clickType == ClickType.LEFT) {
                        new AnvilManager(plugin, manager, player, path, "Edit Suffix Color",
                                ((text) -> {
                                    manager.setUValue(path, text);
                                    new SubSuffixMenu(plugin, player, manager);
                                }));
                    }else if(clickType == ClickType.RIGHT) {
                        manager.setUValue(path, null);
                        new SubSuffixMenu(plugin, player, manager);
                    }
                }
            });
        }

        /*
         * List groups that are not within uConfigManager
         */
        i = i - (i % 9) + 9;
        if(LP.isConnected()) {
            for(String group : LP.getGroups()) {
                if(section.contains(group)) continue;

                this.addButton(new Button(i++) {
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.BLACK_BANNER)
                                .name("&3" + group)
                                .lore("&eClick to add!")
                                .get();
                    }

                    @Override
                    public void onClick(Player p, ClickType clickType) {
                        manager.setUValue("suffix_color.group." + group, "&f");
                        new SubSuffixMenu(plugin, player, manager);
                    }
                });
            }
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
