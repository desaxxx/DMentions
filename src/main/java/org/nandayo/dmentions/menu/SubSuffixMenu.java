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
import org.nandayo.dmentions.integration.LP;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public class SubSuffixMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public SubSuffixMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;

        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.suffix_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection, "title"));

        /*
         * List groups that are within uConfigManager
         */
        int i = 0;
        ConfigurationSection section = config.getUnsavedConfig().getConfigurationSection("suffix_color.group");
        if (section != null) {
            for(String suffixGroup : section.getKeys(false)) {
                this.addButton(new Button(i++) {
                    final String configPath = "suffix_color.group." + suffixGroup;
                    final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                    final String langPathName = "existent_group";
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", suffixGroup)
                                )
                                .lore(() -> {
                                    List<String> lore = new ArrayList<>();
                                    for(String line : LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                        lore.add(config.getValueDisplayMessage(line, configPath));
                                    }
                                    return lore;
                                })
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                                    ((text) -> {
                                        config.getUnsavedConfig().set(configPath, text);
                                        new SubSuffixMenu(plugin, player);
                                    }));
                        }else if(clickType == ClickType.RIGHT) {
                            config.getUnsavedConfig().set(configPath, null);
                            new SubSuffixMenu(plugin, player);
                        }
                    }
                });
            }
        }

        /*
         * List groups that are not within uConfigManager
         */
        i = i - (i % 9) + 9;
        if(LP.isConnected() && section != null) {
            for(String group : LP.getGroups()) {
                if(section.contains(group)) continue;

                this.addButton(new Button(i++) {
                    final String langPathName = "nonexistent_group";
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.BLACK_BANNER)
                                .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, ClickType clickType) {
                        config.getUnsavedConfig().set("suffix_color.group." + group, "&f");
                        new SubSuffixMenu(plugin, player);
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
