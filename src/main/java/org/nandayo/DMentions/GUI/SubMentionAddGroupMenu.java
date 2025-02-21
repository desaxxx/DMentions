package org.nandayo.DMentions.GUI;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.GUIManager.Button;
import org.nandayo.DAPI.GUIManager.Menu;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DMentions.Main;
import org.nandayo.DMentions.integration.LP;
import org.nandayo.DMentions.mention.MentionType;
import org.nandayo.DMentions.utils.GUIManager;

import java.util.List;

public class SubMentionAddGroupMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public SubMentionAddGroupMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.createInventory(54, "&8Redirecting | Choose Group");

        /*
         * List groups that are not added in MentionInsideSettings.
         */
        ConfigurationSection section = manager.getUSection("group.list");
        List<String> disabledGroups = manager.getUStringList("group.disabled_groups");
        if(LP.isConnected()) {
            int i = 0;
            for(String group : LP.getGroups()) {
                if(section.contains(group) || disabledGroups.contains(group)) continue;

                this.addButton(new Button(i++) {
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.YELLOW_BANNER)
                                .name("&3" + group)
                                .lore("&eClick to add!")
                                .get();
                    }

                    @Override
                    public void onClick(Player p, ClickType clickType) {
                        manager.setUValue("group.list." + group + ".sound", "BLOCK_NOTE_BLOCK_PLING");
                        manager.setUValue("group.list." + group + ".display", "<#73c7dc>@{group}");
                        manager.setUValue("group.list." + group + ".cooldown", 5);
                        new MentionInsideSettingsMenu(plugin, player, manager, MentionType.GROUP, group);
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
                        .name("&eBack")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new MentionSettingsMenu(plugin, player, manager);
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
