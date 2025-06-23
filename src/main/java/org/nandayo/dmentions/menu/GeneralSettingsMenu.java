package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dmentions.DMentions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GeneralSettingsMenu extends BaseMenu {

    public GeneralSettingsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        open();
    }

    @Override
    void open() {
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.general_settings_menu");
        createInventory(MenuType.CHEST_6_ROWS, LANGUAGE_MANAGER.getString(menuSection,"title"));

        /*
         * Glass Fillers
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(1,10,19,28,37,46);
            }

            @Override
            public @Nullable ItemStack getItem() {
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
            public @Nullable ItemStack getItem() {
                return ItemCreator.of(Material.COMPASS)
                        .name(LANGUAGE_MANAGER.getString("menu.general_button.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.general_button.lore.viewing"))
                        .enchant(plugin.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
                        .hideFlag(ItemFlag.values())
                        .get();
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
                        .lore(LANGUAGE_MANAGER.getStringList("menu.mention_button.lore.not_viewing"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionSettingsMenu(plugin, player);
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
         * Language
         */
        addButton(new Button() {
            final String configPath = "lang_file";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "language";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(12);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new SubLanguageMenu(plugin, player);
            }
        });

        /*
         * Check for Updates
         */
        addButton(new Button() {
            final String configPath = "check_for_updates";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(13);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        addButton(new Button() {
            final String configPath = "prefix";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(14);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        addButton(new Button() {
            final String configPath = "mention_limit";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(15);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        addButton(new Button() {
            final String configPath = "vanish_respect";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(16);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        addButton(new Button() {
            final String configPath = "afk_respect";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(30);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        addButton(new Button() {
            final String configPath = "ignore_respect";
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = configPath;

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(31);
            }

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
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
        addButton(new Button() {
            final String langPathName = "suffix_colors";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(32);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CYAN_DYE)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new SubSuffixMenu(plugin, player);
            }
        });

        /*
         * Disabled Worlds
         */
        addButton(new Button() {
            final String langPathName = "disabled_worlds";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(33);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GRASS_BLOCK)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
