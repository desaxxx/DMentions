package org.nandayo.GUI;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.nandayo.GUIManager.Button;
import org.nandayo.GUIManager.Menu;
import org.nandayo.Main;
import org.nandayo.mention.MentionType;
import org.nandayo.utils.GUIManager;
import org.nandayo.utils.ItemCreator;

public class SubChooseGroupMenu extends Menu {

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;

    public SubChooseGroupMenu(Main plugin, Player player, GUIManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;

        open();
    }

    public void open() {
        this.setSize(54);
        this.setTitle("&8Redirecting | Choose Group");

        /*
         * List groups that are within group.list
         */
        int i = 0;
        ConfigurationSection section = manager.getUSection("group.list");
        if(section != null) {
            for(String group : section.getKeys(false)) {
                this.addButton(new Button(i++) {
                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.GREEN_BANNER)
                                .name("&3" + group)
                                .lore("&eClick to edit!")
                                .get();
                    }

                    @Override
                    public void onClick(Player p, ClickType clickType) {
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

        this.displayTo(player);
        plugin.guiConfigEditor = player;
    }
}
