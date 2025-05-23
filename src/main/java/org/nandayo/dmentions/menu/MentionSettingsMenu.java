package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.LazyButton;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.mention.MentionType;
import org.nandayo.dmentions.service.GUIManager;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class MentionSettingsMenu extends Menu {

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;

    public MentionSettingsMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.mention_settings_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection, "title"));

        /*
         * Glass Fillers
         */
        this.addLazyButton(new LazyButton(Arrays.asList(1,10,19,28,37,46)) {
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
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.general_button.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.general_button.lore.not_viewing"))
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
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.mention_button.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.mention_button.lore.viewing"))
                        .enchant(plugin.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
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
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.reset_changes.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.reset_changes.lore"))
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
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.save_changes.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.save_changes.lore"))
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
            final String langPathName = "player_mentions";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PLAYER_HEAD)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, manager, MentionType.PLAYER, null);
            }
        });

        /*
         * Everyone Mention
         */
        this.addButton(new Button(14) {
            final String langPathName = "everyone_mentions";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BEACON)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, manager, MentionType.EVERYONE, null);
            }
        });

        /*
         * Nearby Mention
         */
        this.addButton(new Button(16) {
            final String langPathName = "nearby_mentions";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(plugin.getMaterial(DMaterial.SPYGLASS, DMaterial.TARGET))
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, manager, MentionType.NEARBY, null);
            }
        });

        /*
         * Group Mention
         */
        this.addButton(new Button(30) {
            final String langPathName = "group_mentions";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GREEN_BANNER)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
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
        this.runOnClose(inv -> plugin.GUI_CONFIG_EDITOR = null);

        this.displayTo(player);
        plugin.GUI_CONFIG_EDITOR = player;
    }
}
