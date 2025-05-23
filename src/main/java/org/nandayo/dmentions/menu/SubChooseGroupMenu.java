package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.mention.MentionType;
import org.nandayo.dmentions.service.GUIManager;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.List;

@SuppressWarnings("unchecked")
public class SubChooseGroupMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;

    public SubChooseGroupMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.choose_group_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

        /*
         * List groups that are within group.list
         */
        int i = 0;
        ConfigurationSection section = manager.getUSection("group.list");
        if(section != null) {
            for(String group : section.getKeys(false)) {
                this.addButton(new Button(i++) {
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, "group.display_name")
                                        .replace("{group}", group)
                                        .get()[0]
                                )
                                .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, "group.lore"))
                                .get();
                    }

                    @Override
                    public void onClick(Player p, ClickType clickType) {
                        new MentionTypeSettingsMenu(plugin, player, manager, MentionType.GROUP, group);
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
