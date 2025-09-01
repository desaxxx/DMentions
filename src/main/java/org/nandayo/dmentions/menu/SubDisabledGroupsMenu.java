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

import java.util.List;
import java.util.Set;

public class SubDisabledGroupsMenu extends BaseMenu {

    private final LuckPermsHook luckPermsHook;
    public SubDisabledGroupsMenu(DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        this.luckPermsHook = plugin.getLuckPermsHook();
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.disabled_groups_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

        int i = 0;
        /*
         * List groups that are disabled.
         */
        List<String> disabledList = config.getUnsavedConfig().getStringList("group.disabled_groups");
        for(String group : disabledList) {
            int slot = i++;
            
            addButton(new Button() {
                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(slot);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name(guiRegistry.getString(menuSection, "disabled_group.display_name")
                                    .replace("{group}", group)
                            )
                            .lore(guiRegistry.getStringList(menuSection, "disabled_group.lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    disabledList.remove(group);
                    config.getUnsavedConfig().set("group.disabled_groups", disabledList);
                    new SubDisabledGroupsMenu(plugin, player);
                }
            });
        }

        /*
         * List groups that are not disabled.
         */
        if(!luckPermsHook.isMaskNull()) {
            i = i - (i % 9) + 9;
            for(String group : luckPermsHook.getGroups()) {
                if(disabledList.contains(group)) continue;
                int slot = i++;

                addButton(new Button() {
                    final String langPathName = "non-disabled_group";

                    @Override
                    public @NotNull Set<Integer> getSlots() {
                        return Sets.newHashSet(slot);
                    }

                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
                new MentionSettingsMenu(plugin, player);
            }
        });


        displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
