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
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.*;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class GeneralSettingsMenu extends Menu {

    private final DMentions plugin;
    private final ConfigManager configManager;
    private final Player player;
    private final GUIManager manager;

    public GeneralSettingsMenu(DMentions plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.configManager = plugin.CONFIG_MANAGER;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.general_settings_menu");
        this.createInventory(54, (String) LANGUAGE_MANAGER.getMessage(menuSection,"title"));

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
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.general_button.lore.viewing"))
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
         * Mention Settings Icon
         */
        this.addButton(new Button(27) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BELL)
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.mention_button.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.mention_button.lore.not_viewing"))
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
         * Language
         */
        this.addButton(new Button(12) {
            final String configPath = "lang_file";
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "language";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BOOK)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
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
            final String configPath = "check_for_updates";
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CLOCK)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, Boolean.valueOf(text));
                            new GeneralSettingsMenu(plugin, player, manager);
                        }));
            }
        });

        /*
         * Prefix
         */
        this.addButton(new Button(16) {
            final String configPath = "prefix";
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NAME_TAG)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, text);
                            new GeneralSettingsMenu(plugin, player, manager);
                        }));
            }
        });

        /*
         * Mention Limit
         */
        this.addButton(new Button(30) {
            final String configPath = "mention_limit";
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, plugin.parseInt(text));
                            new GeneralSettingsMenu(plugin, player, manager);
                        }));
            }
        });

        /*
         * Suffix Color
         */
        this.addButton(new Button(32) {
            final String langPathName = "suffix_colors";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CYAN_DYE)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new SubSuffixMenu(plugin, player, manager);
            }
        });

        /*
         * Disabled Worlds
         */
        this.addButton(new Button(34) {
            final String langPathName = "disabled_worlds";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GRASS_BLOCK)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new SubDisabledWorldsMenu(plugin, player, manager);
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
