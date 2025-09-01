package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.integration.LuckPermsHook;
import org.nandayo.dmentions.enumeration.MentionType;

import java.util.List;
import java.util.Set;

public class SubMentionAddGroupMenu extends BaseMenu {

    private final LuckPermsHook luckPermsHook;
    public SubMentionAddGroupMenu(@NotNull DMentions plugin, @NotNull Player player) {
        super(plugin, player);
        this.luckPermsHook = plugin.getLuckPermsHook();
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.add_group_menu");
        createInventory(MenuType.CHEST_6_ROWS, guiRegistry.getString(menuSection, "title"));

        /*
         * List groups that are not added in MentionInsideSettings.
         */
        ConfigurationSection section = config.getUnsavedConfig().getConfigurationSection("group.list");
        List<String> disabledGroups = config.getUnsavedConfig().getStringList("group.disabled_groups");
        if(!luckPermsHook.isMaskNull() && section != null) {
            int i = 0;
            for(String group : luckPermsHook.getGroups()) {
                if(section.contains(group) || disabledGroups.contains(group)) continue;
                int slot = i++;

                addButton(new Button() {
                    final String langPathName = "not_added_group";

                    @Override
                    public @NotNull Set<Integer> getSlots() {
                        return Sets.newHashSet(slot);
                    }

                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(Material.YELLOW_BANNER)
                                .name(guiRegistry.getString(menuSection, langPathName + ".display_name")
                                        .replace("{group}", group)
                                )
                                .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                                .get();
                    }

                    @Override
                    public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                        config.getUnsavedConfig().set("group.list." + group + ".sound", "block.note_block.pling");
                        config.getUnsavedConfig().set("group.list." + group + ".display", "&#73c7dc@{group}");
                        config.getUnsavedConfig().set("group.list." + group + ".cooldown", 5);
                        new MentionTypeSettingsMenu(plugin, player, MentionType.GROUP, group);
                    }
                });
            }
        }

        /*
         * Back
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(45);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ARROW)
                        .name(guiRegistry.getString("menu.back.display_name"))
                        .lore(guiRegistry.getStringList("menu.back.lore"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new MentionSettingsMenu(plugin, player);
            }
        });


        displayTo(player);
        plugin.setGuiConfigEditor(player);
    }
}
