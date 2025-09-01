package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LuckPermsHook;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubSuffixMenu extends BaseMenu {

    private final LuckPermsHook luckPermsHook;
    public SubSuffixMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        this.luckPermsHook = plugin.getLuckPermsHook();
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.suffix_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

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
                                .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", suffixGroup)
                                )
                                .lore(() -> {
                                    List<String> lore = new ArrayList<>();
                                    for(String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                        lore.add(config.getValueDisplayMessage(line, configPath));
                                    }
                                    return lore;
                                })
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                        if(clickType == ClickType.LEFT) {
                            new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
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
        if(!luckPermsHook.isMaskNull() && section != null) {
            for(String group : luckPermsHook.getGroups()) {
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
                                .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
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
                        .name(guiRegistry.getString("menu.back.display_name"))
                        .lore(guiRegistry.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new GeneralSettingsMenu(plugin, player);
            }
        });


        displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
