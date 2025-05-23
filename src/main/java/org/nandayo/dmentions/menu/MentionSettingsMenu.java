package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.LazyButton;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

public class MentionSettingsMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public MentionSettingsMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.mention_settings_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection, "title"));

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
                        .lore(LANGUAGE_MANAGER.getStringList("menu.general_button.lore.not_viewing"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new GeneralSettingsMenu(plugin, player);
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
                        .lore(LANGUAGE_MANAGER.getStringList("menu.mention_button.lore.viewing"))
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
         * Player Mention
         */
        this.addButton(new Button(12) {
            final String langPathName = "player_mentions";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PLAYER_HEAD)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, MentionType.PLAYER, null);
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
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, MentionType.EVERYONE, null);
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
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new MentionTypeSettingsMenu(plugin, player, MentionType.NEARBY, null);
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
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
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
