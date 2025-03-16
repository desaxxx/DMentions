package org.nandayo.DMentions.GUI;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.GUIManager.Button;
import org.nandayo.DAPI.GUIManager.LazyButton;
import org.nandayo.DAPI.GUIManager.Menu;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DMentions.Main;
import org.nandayo.DMentions.mention.MentionType;
import org.nandayo.DMentions.utils.GUIManager;

import java.util.Arrays;
import java.util.Set;

public class MentionSettingsMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public MentionSettingsMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.createInventory(54, "&8Mention Settings");

        /*
         * Glass Fillers
         */
        this.addLazyButton(new LazyButton(Set.of(1,10,19,28,37,46)) {
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
        this.addButton(new Button(9) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.COMPASS)
                        .name("&3General Settings")
                        .lore("&eClick to view!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new GeneralSettingsMenu(plugin, player, manager);
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
         * Player Mention
         */
        this.addButton(new Button(12) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PLAYER_HEAD)
                        .name("&3Player Mentions")
                        .lore("&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionInsideSettingsMenu(plugin, player, manager, MentionType.PLAYER, null);
            }
        });

        /*
         * Everyone Mention
         */
        this.addButton(new Button(14) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BEACON)
                        .name("&3Everyone Mentions")
                        .lore("&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionInsideSettingsMenu(plugin, player, manager, MentionType.EVERYONE, null);
            }
        });

        /*
         * Nearby Mention
         */
        this.addButton(new Button(16) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.SPYGLASS)
                        .name("&3Nearby Mentions")
                        .lore("&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionInsideSettingsMenu(plugin, player, manager, MentionType.NEARBY, null);
            }
        });

        /*
         * Group Mention
         */
        this.addButton(new Button(30) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GREEN_BANNER)
                        .name("&3Group Mentions")
                        .lore("&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new SubChooseGroupMenu(plugin, player, manager);
            }
        });

        /*
         * Close
         */
        this.runOnClose(inv -> {
            plugin.guiConfigEditor = null;
        });

        this.displayTo(player);
        plugin.guiConfigEditor = player;
    }
}
