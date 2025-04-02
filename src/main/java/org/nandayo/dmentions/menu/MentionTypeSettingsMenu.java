package org.nandayo.DMentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DAPI.ItemCreator;
import org.nandayo.DAPI.guimanager.Button;
import org.nandayo.DAPI.guimanager.LazyButton;
import org.nandayo.DAPI.guimanager.Menu;
import org.nandayo.DAPI.object.DEnchantment;
import org.nandayo.DAPI.object.DMaterial;
import org.nandayo.DMentions.DMentions;
import org.nandayo.DMentions.mention.MentionType;
import org.nandayo.DMentions.service.ConfigManager;
import org.nandayo.DMentions.service.GUIManager;
import org.nandayo.DMentions.service.LanguageManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class MentionTypeSettingsMenu extends Menu {

    /*
     * Insider variables in group section
     */
    private final List<String> insiderVar = Arrays.asList("sound","display","cooldown");

    private final DMentions plugin;
    private final Player player;
    private final GUIManager manager;
    private final ConfigManager configManager;
    private final MentionType mentionType;
    private final String group;

    public MentionTypeSettingsMenu(DMentions plugin, Player player, GUIManager manager, MentionType mentionType, String group) {
        this.plugin = plugin;
        this.configManager = plugin.CONFIG_MANAGER;
        this.player = player;
        this.manager = manager;
        this.mentionType = mentionType;
        this.group = group;

        open();
    }

    public void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.LANGUAGE_MANAGER;
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.mention_type_settings_menu");
        final String langMentionKey = mentionType == MentionType.GROUP ? "group" : "other";
        this.createInventory(54,
                LANGUAGE_MANAGER.getMessageReplaceable(menuSection, "title." + langMentionKey)
                        .replace("{mentionType}", mentionType.toString().toUpperCase(Locale.ENGLISH))
                        .replace("{group}", group == null ? "?" : group)
                        .get()[0]);

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
                Material mat;
                switch (mentionType) {
                    case PLAYER: mat = Material.PLAYER_HEAD;break;
                    case EVERYONE: mat = Material.BEACON;break;
                    case NEARBY: mat = plugin.getMaterial(DMaterial.SPYGLASS, DMaterial.TARGET);break;
                    case GROUP: mat = Material.GREEN_BANNER;break;
                    default: mat = Material.BELL;break;
                }
                return ItemCreator.of(mat)
                        .name((String) LANGUAGE_MANAGER.getMessage("menu.mention_button.display_name"))
                        .lore((List<String>) LANGUAGE_MANAGER.getMessage("menu.mention_button.lore.go_back"))
                        .enchant(plugin.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
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
         * Enabled
         */
        this.addButton(new Button(12) {
            final String configPath = getPath(mentionType, "enabled", group);
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "enabled";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.LEVER)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name." + langMentionKey))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, Boolean.valueOf(text));
                            new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Permission
         */
        this.addButton(new Button(14) {
            final String configPath = getPath(mentionType, "permission", group);
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "permission";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name." + langMentionKey))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, text);
                            new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Sound
         */
        this.addButton(new Button(16) {
            final String configPath = getPath(mentionType, "sound", group);
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "sound";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NOTE_BLOCK)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, text);
                            new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Keyword
         */
        if(mentionType != MentionType.PLAYER) {
            this.addButton(new Button(30) {
                final String configPath = getPath(mentionType, "keyword", group);
                final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "keyword";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.NAME_TAG)
                            .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name." + langMentionKey))
                            .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                            ((text) -> {
                                manager.setUValue(configPath, text);
                                new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                            }));
                }
            });
        }

        /*
         * Display
         */
        this.addButton(new Button(32) {
            final String configPath = getPath(mentionType, "display", group);
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "display";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ITEM_FRAME)
                        .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                        .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                        .get();
            }

            @Override
            public void onClick(Player p, ClickType clickType) {
                new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            manager.setUValue(configPath, text);
                            new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Cooldown
         */
        this.addButton(new Button(34) {
            final String configPath = getPath(mentionType, "cooldown", group);
            final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "cooldown";
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
                            manager.setUValue(configPath, plugin.parseInt(text));
                            new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                        }));
            }
        });

        /*
         * Player Customized Display
         */
        if(mentionType == MentionType.PLAYER) {
            this.addButton(new Button(51) {
                final String configPath = getPath(mentionType, "customized_display", group);
                final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "customized_display";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(plugin.getMaterial(DMaterial.GLOW_ITEM_FRAME, DMaterial.PAINTING))
                            .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                            .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                            ((text) -> {
                                manager.setUValue(configPath, text);
                                new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                            }));
                }
            });
        }

        else if(mentionType == MentionType.GROUP) {
            /*
             * Add|Delete Group Menu
             */
            this.addButton(new Button(51) {
                final String langPathName = "add_delete_group";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BLACK_BANNER)
                            .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                            .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
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
                final String langPathName = "disabled_groups";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.REDSTONE)
                            .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                            .lore((List<String>) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".lore"))
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
                final String configPath = getPath(mentionType, "radius", group);
                final String changed = manager.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "radius";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.ENDER_PEARL)
                            .name((String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".display_name"))
                            .lore(LANGUAGE_MANAGER.getValueDisplayMessage(menuSection, langPathName + ".lore." + changed, configPath, configManager))
                            .get();
                }

                @Override
                public void onClick(Player p, ClickType clickType) {
                    new AnvilManager(plugin, manager, player, configPath, (String) LANGUAGE_MANAGER.getMessage(menuSection, langPathName + ".edit_title"),
                            ((text) -> {
                                manager.setUValue(configPath, plugin.parseInt(text));
                                new MentionTypeSettingsMenu(plugin, player, manager, mentionType, group);
                            }));
                }
            });
        }

        /*
         * Close
         */
        this.runOnClose(inv -> plugin.GUI_CONFIG_EDITOR = null);

        this.displayTo(player);
        plugin.GUI_CONFIG_EDITOR = player;
    }

    private String getPath(MentionType mentionType, String path, String group) {
        if(mentionType == MentionType.GROUP && insiderVar.contains(path)) {
            return mentionType.toString().toLowerCase() + ".list." + group + "." + path;
        }
        return mentionType.toString().toLowerCase() + "." + path;
    }
}
