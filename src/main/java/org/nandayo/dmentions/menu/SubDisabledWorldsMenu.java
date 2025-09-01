package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dmentions.DMentions;

import java.util.List;
import java.util.Set;

public class SubDisabledWorldsMenu extends BaseMenu {

    public SubDisabledWorldsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.disabled_worlds_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

        int i = 0;
        /*
         * List worlds that are disabled.
         */
        List<String> disabledList = config.getUnsavedConfig().getStringList("disabled_worlds");
        for(String world : disabledList) {
            int slot = i++;
            
            addButton(new Button() {
                final String langPathName = "disabled_world";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(slot);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.RED_BANNER)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                    .replace("{world}", world)
                            )
                            .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    disabledList.remove(world);
                    config.getUnsavedConfig().set("disabled_worlds", disabledList);
                    new SubDisabledWorldsMenu(plugin, player);
                }
            });
        }

        /*
         * List worlds that are not disabled.
         */
        i = i - (i % 9) + 9;
        for(World w : Bukkit.getWorlds()) {
            String world = w.getName();
            if(disabledList.contains(world)) continue;
            int slot = i++;

            addButton(new Button() {
                final String langPathName = "non-disabled_world";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(slot);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.GREEN_BANNER)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                    .replace("{world}", world)
                            )
                            .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    disabledList.add(world);
                    config.getUnsavedConfig().set("disabled_worlds", disabledList);
                    new SubDisabledWorldsMenu(plugin, player);
                }
            });
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


        this.displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
