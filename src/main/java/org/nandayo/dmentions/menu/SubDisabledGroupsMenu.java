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

import java.util.List;

public class SubDisabledGroupsMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public SubDisabledGroupsMenu(DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.disabled_groups_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection, "title"));

        int i = 0;
        /*
         * List groups that are disabled.
         */
        List<String> disabledList = config.getUnsavedConfig().getStringList("group.disabled_groups");
        for(String group : disabledList) {
            this.addButton(new Button(i++) {
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name(LANGUAGE_MANAGER.getString(menuSection, "disabled_group.display_name")
                                    .replace("{group}", group)
                            )
                            .lore(LANGUAGE_MANAGER.getStringList(menuSection, "disabled_group.lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, ClickType clickType) {
                    disabledList.remove(group);
                    config.getUnsavedConfig().set("group.disabled_groups", disabledList);
                    new SubDisabledGroupsMenu(plugin, player);
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
                                .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, ClickType clickType) {
                        disabledList.add(group);
                        config.getUnsavedConfig().set("group.disabled_groups", disabledList);
                        new SubDisabledGroupsMenu(plugin, player);
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
                new MentionSettingsMenu(plugin, player);
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
