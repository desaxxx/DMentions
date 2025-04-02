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
import org.nandayo.DMentions.integration.LP;
import org.nandayo.DMentions.service.GUIManager;
import org.nandayo.DMentions.service.LanguageManager;

import java.util.List;

@SuppressWarnings("unchecked")
public class SubDisabledGroupsMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;

    public SubDisabledGroupsMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.disabled_groups_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

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
                            .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, "disabled_group.display_name")
                                    .replace("{group}", group)
                                    .get()[0]
                            )
                            .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, "disabled_group.lore"))
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
                    final String langPathName = "non-disabled_group";
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                        .get()[0]
                                )
                                .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
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
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.back.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionSettingsMenu(plugin, player, manager);
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
