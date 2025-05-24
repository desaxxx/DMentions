package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.LazyButton;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.*;

import java.util.ArrayList;
import java.util.List;

public class GeneralSettingsMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public GeneralSettingsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.general_settings_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection,"title"));

        /*
         * Glass Fillers
         */
        this.addButton(new LazyButton(1,10,19,28,37,46) {
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
                        .name(LANGUAGE_MANAGER.getString("menu.general_button.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.general_button.lore.viewing"))
                        .enchant(plugin.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
                        .hideFlag(ItemFlag.values())
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
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
                        .name(LANGUAGE_MANAGER.getString("menu.mention_button.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.mention_button.lore.not_viewing"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new MentionSettingsMenu(plugin, player);
            }
        });

        /*
         * Reset Changes Icon
         */
        this.addButton(new Button(47) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BARRIER)
                        .name(LANGUAGE_MANAGER.getString("menu.reset_changes.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.reset_changes.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                config.resetUnsavedConfig(p);
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
                        .name(LANGUAGE_MANAGER.getString("menu.save_changes.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.save_changes.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                config.saveUnsavedConfig(p);
                player.closeInventory();
            }
        });

        // Modify Config
        /*
         * Language
         */
        this.addButton(new Button(12) {
            final String configPath = "lang_file";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "language";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.BOOK)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for(String line : LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new SubLanguageMenu(plugin, player);
            }
        });

        /*
         * Check for Updates
         */
        this.addButton(new Button(13) {
            final String configPath = "check_for_updates";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CLOCK)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, Boolean.parseBoolean(text));
                            new GeneralSettingsMenu(plugin, player);
                        }));
            }
        });

        /*
         * Prefix
         */
        this.addButton(new Button(14) {
            final String configPath = "prefix";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NAME_TAG)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, text);
                            new GeneralSettingsMenu(plugin, player);
                        }));
            }
        });

        /*
         * Mention Limit
         */
        this.addButton(new Button(15) {
            final String configPath = "mention_limit";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : LANGUAGE_MANAGER.getStringList(menuSection,langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, plugin.parseInt(text));
                            new GeneralSettingsMenu(plugin, player);
                        }));
            }
        });

        /*
         * Vanish Respect
         */
        this.addButton(new Button(16) {
            final String configPath = "vanish_respect";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.POTION)
                        .potion(PotionType.INVISIBILITY)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : LANGUAGE_MANAGER.getStringList(menuSection,langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .hideFlag(ItemFlag.values())
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, Boolean.parseBoolean(text));
                            new GeneralSettingsMenu(plugin, player);
                        }));
            }
        });

        /*
         * AFK Respect
         */
        this.addButton(new Button(30) {
            final String configPath = "afk_respect";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PACKED_ICE)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : LANGUAGE_MANAGER.getStringList(menuSection,langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, Boolean.parseBoolean(text));
                            new GeneralSettingsMenu(plugin, player);
                        }));
            }
        });

        /*
         * Ignore Respect
         */
        this.addButton(new Button(31) {
            final String configPath = "ignore_respect";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ENDER_EYE)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : LANGUAGE_MANAGER.getStringList(menuSection,langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, Boolean.parseBoolean(text));
                            new GeneralSettingsMenu(plugin, player);
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
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new SubSuffixMenu(plugin, player);
            }
        });

        /*
         * Disabled Worlds
         */
        this.addButton(new Button(33) {
            final String langPathName = "disabled_worlds";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GRASS_BLOCK)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new SubDisabledWorldsMenu(plugin, player);
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
