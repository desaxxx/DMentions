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
import org.nandayo.DMentions.service.ConfigManager;
import org.nandayo.DMentions.service.GUIManager;
import org.nandayo.DMentions.service.LanguageManager;

import java.util.List;

@SuppressWarnings("unchecked")
public class SubSuffixMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;
    private final ConfigManager configManager;

    public SubSuffixMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.configManager = plugin.CONFIG_MANAGER;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.suffix_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

        /*
         * List groups that are within uConfigManager
         */
        int i = 0;
        ConfigurationSection section = manager.getUSection("suffix_color.group");
        for(String suffixGroup : section.getKeys(false)) {
            this.addButton(new Button(i++) {
                final String configPath = "suffix_color.group." + suffixGroup;
                final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "existent_group";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.GREEN_BANNER)
                            .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                    .replace("{group}", suffixGroup)
                                    .get()[0]
                            )
                            .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    if(clickType == ClickType.LEFT) {
                        new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                                ((text) -> {
                                    manager.setUValue(configPath, text);
                                    new SubSuffixMenu(plugin, player, manager);
                                }));
                    }else if(clickType == ClickType.RIGHT) {
                        manager.setUValue(configPath, null);
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
                    final String langPathName = "nonexistent_group";
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.BLACK_BANNER)
                                .name(LANGUAGE_MANAGER.getMessageReplaceable(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                        .get()[0]
                                )
                                .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
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
