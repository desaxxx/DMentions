package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.enumeration.MentionType;

import java.util.Set;

public class MentionSettingsMenu extends BaseMenu {

    public MentionSettingsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    void open() {
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.mention_settings_menu");
        createInventory(MenuType.CHEST_6_ROWS, LANGUAGE_MANAGER.getString(menuSection, "title"));

        /*
         * Glass Fillers
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(1,10,19,28,37,46);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GRAY_STAINED_GLASS_PANE)
                        .name(" ")
                        .get();
            }
        });

        /*
         * General Settings Icon
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(9);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.COMPASS)
                        .name(LANGUAGE_MANAGER.getString("menu.general_button.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.general_button.lore.not_viewing"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new GeneralSettingsMenu(plugin, player);
            }
        });

        /*
         * Mention Settings Icon
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(27);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BELL)
                        .name(LANGUAGE_MANAGER.getString("menu.mention_button.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.mention_button.lore.viewing"))
                        .enchant(plugin.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
                        .hideFlag(ItemFlag.values())
                        .get();
            }
        });

        /*
         * Reset Changes Icon
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(47);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BARRIER)
                        .name(LANGUAGE_MANAGER.getString("menu.reset_changes.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.reset_changes.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                config.resetUnsavedConfig(p);
                player.closeInventory();
            }
        });

        /*
         * Save Changes Icon
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(53);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.WRITABLE_BOOK)
                        .name(LANGUAGE_MANAGER.getString("menu.save_changes.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.save_changes.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                config.saveUnsavedConfig(p);
                player.closeInventory();
            }
        });

        // Modify Config
        /*
         * Player Mention
         */
        addButton(new Button() {
            final String langPathName = "player_mentions";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(12);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PLAYER_HEAD)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, MentionType.PLAYER, null);
            }
        });

        /*
         * Everyone Mention
         */
        addButton(new Button() {
            final String langPathName = "everyone_mentions";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(14);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BEACON)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, MentionType.EVERYONE, null);
            }
        });

        /*
         * Nearby Mention
         */
        addButton(new Button() {
            final String langPathName = "nearby_mentions";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(16);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(plugin.getMaterial(DMaterial.SPYGLASS, DMaterial.TARGET))
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, MentionType.NEARBY, null);
            }
        });

        /*
         * Group Mention
         */
        addButton(new Button() {
            final String langPathName = "group_mentions";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(30);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GREEN_BANNER)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new SubChooseGroupMenu(plugin, player);
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
