package org.nandayo.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.GUIManager.Button;
import org.nandayo.GUIManager.Menu;
import org.nandayo.Main;
import org.nandayo.integration.LP;
import org.nandayo.utils.GUIManager;
import org.nandayo.utils.ItemCreator;

import java.util.List;

public class SubDisabledGroupsMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public SubDisabledGroupsMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.setSize(54);
        this.setTitle("&8Disabled Groups");

        int i = 0;
        /*
         * List groups that are disabled.
         */
        List<String> disabledList = manager.getUStringList("group.disabled_groups");
        for(String group : disabledList) {
            this.addButton(new Button(i++) {
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name("&3" + group)
                            .lore("&eClick to remove from disabled groups!")
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    disabledList.remove(group);
                    manager.setUValue("group.disabled_groups", disabledList);
                    new SubDisabledGroupsMenu(plugin, player, manager);
                }
            });
        }

        /*
         * List groups that are not disabled.
         */
        i = i - (i % 9) + 9;
        if(LP.isConnected()) {
            for(String group : LP.getGroups()) {
                if(disabledList.contains(group)) continue;

                this.addButton(new Button(i++) {
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name("&3" + group)
                                .lore("&eClick to add to disabled groups!")
                                .get();
                    }

                    @Override
                    public void onClick(Player p, ClickType clickType) {
                        disabledList.add(group);
                        manager.setUValue("group.disabled_groups", disabledList);
                        new SubDisabledGroupsMenu(plugin, player, manager);
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
                new MentionSettingsMenu(plugin, player, manager);
            }
        });

        this.displayTo(player);
        plugin.guiConfigEditor = player;
    }
}
