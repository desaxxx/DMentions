package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LP;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.List;

public class SubMentionAddGroupMenu extends Menu {

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;

    public SubMentionAddGroupMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.add_group_menu");
        this.createInventory(54, LANGUAGE_MANAGER.getString(menuSection, "title"));

        /*
         * List groups that are not added in MentionInsideSettings.
         */
        ConfigurationSection section = config.getUnsavedConfig().getConfigurationSection("group.list");
        List<String> disabledGroups = config.getUnsavedConfig().getStringList("group.disabled_groups");
        if(LP.isConnected() && section != null) {
            int i = 0;
            for(String group : LP.getGroups()) {
                if(section.contains(group) || disabledGroups.contains(group)) continue;

                this.addButton(new Button(i++) {
                    final String langPathName = "not_added_group";
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.YELLOW_BANNER)
                                .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, ClickType clickType) {
                        config.getUnsavedConfig().set("group.list." + group + ".sound", "block.note_block.pling");
                        config.getUnsavedConfig().set("group.list." + group + ".display", "<#73c7dc>@{group}");
                        config.getUnsavedConfig().set("group.list." + group + ".cooldown", 5);
                        new MentionTypeSettingsMenu(plugin, player, MentionType.GROUP, group);
                    }
                });
            }
        }

        /*
         * Back
         */
        this.addButton(new Button(45) {
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ARROW)
                        .name(LANGUAGE_MANAGER.getString("menu.back.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, ClickType clickType) {
                new MentionSettingsMenu(plugin, player);
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
