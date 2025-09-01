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

import java.util.Set;

public class SubLanguageMenu extends BaseMenu {

    public SubLanguageMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.language_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

        int i = 0;
        for(String lang : plugin.getLanguageManager().REGISTERED_LANGUAGES) {
            int slot = i++;
            
            addButton(new Button() {
                final String langPathName = "language";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(slot);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BOOK)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                    .replace("{language}", lang)
                            )
                            .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    config.getUnsavedConfig().set("lang_file", lang);
                    new GeneralSettingsMenu(plugin, player);
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
