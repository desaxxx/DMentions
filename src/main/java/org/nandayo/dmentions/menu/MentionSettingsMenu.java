package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.util.DUtil;

import java.util.Set;

public class MentionSettingsMenu extends BaseMenu {

    public MentionSettingsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.mention_settings_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

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
                        .name(guiRegistry.getString("menu.general_button.display_name"))
                        .lore(guiRegistry.getStringList("menu.general_button.lore.not_viewing"))
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
                        .name(guiRegistry.getString("menu.mention_button.display_name"))
                        .lore(guiRegistry.getStringList("menu.mention_button.lore.viewing"))
                        .enchant(DUtil.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
                        .flags(ItemFlag.values())
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
                        .name(guiRegistry.getString("menu.reset_changes.display_name"))
                        .lore(guiRegistry.getStringList("menu.reset_changes.lore"))
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
                        .name(guiRegistry.getString("menu.save_changes.display_name"))
                        .lore(guiRegistry.getStringList("menu.save_changes.lore"))
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
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
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
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
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
                return ItemCreator.of(DUtil.getMaterial(DMaterial.SPYGLASS, DMaterial.TARGET))
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
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
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new SubChooseGroupMenu(plugin, player);
            }
        });


        this.displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
