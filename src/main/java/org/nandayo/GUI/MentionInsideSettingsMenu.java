package org.nandayo.GUI;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.nandayo.GUIManager.Button;
import org.nandayo.GUIManager.Menu;
import org.nandayo.Main;
import org.nandayo.mention.MentionType;
import org.nandayo.utils.GUIManager;
import org.nandayo.utils.ItemCreator;

import java.util.Arrays;
import java.util.List;

public class MentionInsideSettingsMenu extends Menu {

    /*
     * Insider variables in group section
     */
    private final List<String> insiderVar = Arrays.asList("sound","display","cooldown");

    private final Main plugin;
    private final Player player;
    private final GUIManager manager;
    private final MentionType mentionType;
    private final String group;

    public MentionInsideSettingsMenu(Main plugin, Player player, GUIManager manager, MentionType mentionType, String group) {
        this.plugin = plugin;
        this.player = player;
        this.manager = manager;
        this.mentionType = mentionType;
        this.group = group;

        open();
    }

    public void open() {
        this.setSize(54);
        String mt = mentionType == MentionType.GROUP ? "=" + group : "";
        this.setTitle("&8Mention Settings (" + mentionType.toString() + mt + ")");

        /*
         * Glass Fillers
         */
        this.setFillers(Arrays.asList(1,10,19,28,37,46));

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
                Material mat;
                switch (mentionType) {
                    case PLAYER -> mat = Material.PLAYER_HEAD;
                    case EVERYONE -> mat = Material.BEACON;
                    case NEARBY -> mat = Material.SPYGLASS;
                    case GROUP -> mat = Material.GREEN_BANNER;
                    default -> mat = Material.BELL;
                }
                return ItemCreator.of(mat)
                        .name("&3Mention Settings")
                        .lore("&eClick to go back!")
                        .enchant(Enchantment.DURABILITY, 1)
                        .hideFlag(ItemFlag.values())
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

        /*
         * All group warn for groups
         */
        String all = mentionType == MentionType.GROUP ? " (All Groups)" : "";

        // Modify Config
        /*
         * Enabled
         */
        this.addButton(new Button(12) {
            final String path = getPath(mentionType, "enabled", group);
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.LEVER)
                        .name("&3Enabled" + all)
                        .lore("&eState: &f" + manager.getValueDisplay(path),
                                "&eClick to toggle!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit State",
                        ((text) -> {
                            manager.setUValue(path, Boolean.valueOf(text));
                            new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Permission
         */
        this.addButton(new Button(14) {
            final String path = getPath(mentionType, "permission", group);
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name("&3Permission" + all)
                        .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                "&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Permission",
                        ((text) -> {
                            manager.setUValue(path, text);
                            new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Sound
         */
        this.addButton(new Button(16) {
            final String path = getPath(mentionType, "sound", group);
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NOTE_BLOCK)
                        .name("&3Sound")
                        .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                "&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Sound",
                        ((text) -> {
                            manager.setUValue(path, text);
                            new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Keyword
         */
        if(mentionType != MentionType.PLAYER) {
            this.addButton(new Button(30) {
                final String path = getPath(mentionType, "keyword", group);
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.NAME_TAG)
                            .name("&3Keyword" + all)
                            .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                    "&eClick to edit!")
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    new AnvilManager(plugin, manager, player, path, "Edit Keyword",
                            ((text) -> {
                                manager.setUValue(path, text);
                                new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                            }));
                }
            });
        }

        /*
         * Display
         */
        this.addButton(new Button(32) {
            final String path = getPath(mentionType, "display", group);
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.GLOW_ITEM_FRAME)
                        .name("&3Display")
                        .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                "&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Display",
                        ((text) -> {
                            manager.setUValue(path, text);
                            new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Cooldown
         */
        this.addButton(new Button(34) {
            final String path = getPath(mentionType, "cooldown", group);
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CLOCK)
                        .name("&3Cooldown (seconds)")
                        .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                "&eClick to edit!")
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, path, "Edit Cooldown",
                        ((text) -> {
                            manager.setUValue(path, plugin.parseInt(text));
                            new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        if(mentionType == MentionType.GROUP) {
            /*
             * Add|Delete Group Menu
             */
            this.addButton(new Button(51) {
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BLACK_BANNER)
                            .name("&3Add | Delete Group")
                            .lore("&eLeft click to choose group!",
                                    "&cRight click to remove this group from the list!")
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    if(clickType == ClickType.LEFT) {
                        new SubMentionAddGroupMenu(plugin, player, manager);
                    }else if(clickType == ClickType.RIGHT) {
                        manager.setUValue("group.list." + group, null);
                        new MentionSettingsMenu(plugin, player, manager);
                    }
                }
            });

            /*
             * Add to disabled-groups
             */
            this.addButton(new Button(49) {
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.REDSTONE)
                            .name("&3Disabled Groups")
                            .lore("&eClick to edit!")
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    new SubDisabledGroupsMenu(plugin, player, manager);
                }
            });
        }

        /*
         * Nearby Radius
         */
        else if(mentionType == MentionType.NEARBY) {
            this.addButton(new Button(51) {
                final String path = getPath(mentionType, "radius", group);
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.ENDER_PEARL)
                            .name("&3Radius")
                            .lore("&eCurrent: &f" + manager.getValueDisplay(path),
                                    "&eClick to edit!")
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    new AnvilManager(plugin, manager, player, path, "Edit Radius",
                            ((text) -> {
                                manager.setUValue(path, plugin.parseInt(text));
                                new MentionInsideSettingsMenu(plugin, player, manager, mentionType, group);
                            }));
                }
            });
        }

        this.displayTo(player);
        plugin.guiConfigEditor = player;
    }

    private String getPath(MentionType mentionType, String path, String group) {
        if(mentionType == MentionType.GROUP && insiderVar.contains(path)) {
            return mentionType.toString().toLowerCase() + ".list." + group + "." + path;
        }
        return mentionType.toString().toLowerCase() + "." + path;
    }
}
