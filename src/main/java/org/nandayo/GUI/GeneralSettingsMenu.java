package org.nandayo.GUI;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.nandayo.GUIManager.Button;
import org.nandayo.GUIManager.Menu;
import org.nandayo.Main;
import org.nandayo.utils.GUIManager;
import org.nandayo.utils.ItemCreator;

import java.util.Arrays;

public class GeneralSettingsMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public GeneralSettingsMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.setSize(54);
        this.setTitle("&8General Settings");

        /*
         * Glass Fillers
         */
        this.setFillers(Arrays.asList(1,10,19,28,37,46));

        /*
         * General Settings Icon
         */
        this.addButton(new Button(9) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.COMPASS)
                        .name("&3General Settings")
                        .lore("&eYou are viewing this setting.")
                        .enchant(Enchantment.DURABILITY, 1)
                        .hideFlag(ItemFlag.values())
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                // NONE
            }
        });

        /*
         * Mention Settings Icon
         */
        this.addButton(new Button(27) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BELL)
                        .name("&3Mention Settings")
                        .lore("&eClick to view!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionSettingsMenu(plugin, player, manager);
            }
        });

        /*
         * Reset Changes Icon
         */
        this.addButton(new Button(47) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BARRIER)
                        .name("&cReset changes")
                        .lore("&eClick to apply!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                manager.resetChanges();
                player.closeInventory();
            }
        });

        /*
         * Save Changes Icon
         */
        this.addButton(new Button(53) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.WRITABLE_BOOK)
                        .name("&aSave Changes")
                        .lore("&eClick to apply!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                manager.saveChanges();
                player.closeInventory();
            }
        });

        // Modify Config
        /*
         * Language
         */
        this.addButton(new Button(12) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BOOK)
                        .name("&3Language")
                        .lore("&eCurrent: &f" + manager.getValueDisplay("lang_file"),
                                "&eClick to choose another language!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new SubLanguageMenu(plugin, player, manager);
            }
        });

        /*
         * Check for Updates
         */
        this.addButton(new Button(14) {
            final String path = "check_for_updates";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CLOCK)
                        .name("&3Check for Updates")
                        .lore("&eState: &f" + manager.getValueDisplay(path),
                                "&eClick to toggle!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Update Notification",
                        ((text) -> {
                            manager.setUValue(path, Boolean.valueOf(text));
                            new GeneralSettingsMenu(plugin, player, manager);
                        }));
            }
        });

        /*
         * Prefix
         */
        this.addButton(new Button(16) {
            final String path = "prefix";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NAME_TAG)
                        .name("&3Prefix")
                        .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                "&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Prefix",
                        ((text) -> {
                            manager.setUValue(path, text);
                            new GeneralSettingsMenu(plugin, player, manager);
                        }));
            }
        });

        /*
         * Mention Limit
         */
        this.addButton(new Button(30) {
            final String path = "mention_limit";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name("&3Mention Limit")
                        .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                "&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Mention Limit",
                        ((text) -> {
                            manager.setUValue(path, plugin.parseInt(text));
                            new GeneralSettingsMenu(plugin, player, manager);
                        }));
            }
        });

        /*
         * Suffix Color
         */
        this.addButton(new Button(32) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CYAN_DYE)
                        .name("&3Suffix Colors")
                        .lore("&eClick to edit colors!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new SubSuffixMenu(plugin, player, manager);
            }
        });

        this.displayTo(player);
        plugin.guiConfigEditor = player;
    }
}
