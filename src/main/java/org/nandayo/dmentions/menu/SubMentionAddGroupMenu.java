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
import org.nandayo.dmentions.integration.LP;
import org.nandayo.dmentions.mention.MentionType;
import org.nandayo.dmentions.service.GUIManager;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.List;

@SuppressWarnings("unchecked")
public class SubMentionAddGroupMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;

    public SubMentionAddGroupMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.add_group_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

        /*
         * List groups that are not added in MentionInsideSettings.
         */
        ConfigurationSection section = manager.getUSection("group.list");
        List<String> disabledGroups = manager.getUStringList("group.disabled_groups");
        if(LP.isConnected()) {
            int i = 0;
            for(String group : LP.getGroups()) {
                if(section.contains(group) || disabledGroups.contains(group)) continue;

                this.addButton(new Button(i++) {
                    final String langPathName = "not_added_group";
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.YELLOW_BANNER)
                                .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                        .get()[0]
                                )
                                .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(Player p, ClickType clickType) {
                        manager.setUValue("group.list." + group + ".sound", "BLOCK_NOTE_BLOCK_PLING");
                        manager.setUValue("group.list." + group + ".display", "<#73c7dc>@{group}");
                        manager.setUValue("group.list." + group + ".cooldown", 5);
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
