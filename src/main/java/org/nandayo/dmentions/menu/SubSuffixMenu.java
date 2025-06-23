package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LP;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubSuffixMenu extends BaseMenu {

    public SubSuffixMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    void open() {
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.suffix_menu");
        createInventory(MenuType.CHEST_6_ROWS, LANGUAGE_MANAGER.getString(menuSection, "title"));

        /*
         * List groups that are within uConfigManager
         */
        int i = 0;
        ConfigurationSection section = config.getUnsavedConfig().getConfigurationSection("suffix_color.group");
        if (section != null) {
            for(String suffixGroup : section.getKeys(false)) {
                int slot = i++;
                
                addButton(new Button() {
                    final String configPath = "suffix_color.group." + suffixGroup;
                    final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                    final String langPathName = "existent_group";

                    @Override
                    public @NotNull Set<Integer> getSlots() {
                        return Sets.newHashSet(slot);
                    }

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
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
                int slot = i++;

                addButton(new Button() {
                    final String langPathName = "nonexistent_group";

                    @Override
                    public @NotNull Set<Integer> getSlots() {
                        return Sets.newHashSet(slot);
                    }

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
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                        config.getUnsavedConfig().set("suffix_color.group." + group, "&f");
                        new SubSuffixMenu(plugin, player);
                    }
                });
            }
        }

        /*
         * Back
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(45);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ARROW)
                        .name(LANGUAGE_MANAGER.getString("menu.back.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new GeneralSettingsMenu(plugin, player);
            }
        });

        /*
         * Close
         */
        runOnClose(inv -> plugin.setGuiConfigEditor(null));

        displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
