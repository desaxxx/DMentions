package org.nandayo.dmentions.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.ItemCreator;
import org.nandayo.dapi.guimanager.Button;
import org.nandayo.dapi.guimanager.LazyButton;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MentionTypeSettingsMenu extends Menu {

    /*
     * Insider variables in group section
     */
    private final List<String> insiderVar = Arrays.asList("sound","display","cooldown");

    private final @NotNull DMentions plugin;
    private final @NotNull Config config;
    private final @NotNull Player player;
    private final @NotNull MentionType mentionType;
    private final @Nullable String group;

    public MentionTypeSettingsMenu(@NotNull DMentions plugin, @NotNull Player player, @NotNull MentionType mentionType, @Nullable String group) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        this.mentionType = mentionType;
        this.group = group;
        open();
    }

    private void open() {
        LanguageManager LANGUAGE_MANAGER = plugin.getLanguageManager();
        ConfigurationSection menuSection = LANGUAGE_MANAGER.getSection("menu.mention_type_settings_menu");
        final String langMentionKey = mentionType == MentionType.GROUP ? "group" : "other";
        this.createInventory(54,
                LANGUAGE_MANAGER.getString(menuSection, "title." + langMentionKey)
                        .replace("{mentionType}", mentionType.name().toLowerCase(Locale.ENGLISH))
                        .replace("{group}", group == null ? "?" : group));

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
                return ItemCreator.of(mentionType.getIconMaterial())
                        .name(LANGUAGE_MANAGER.getString("menu.mention_button.display_name"))
                        .lore(LANGUAGE_MANAGER.getStringList("menu.mention_button.lore.go_back"))
                        .enchant(plugin.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
                        .hideFlag(ItemFlag.values())
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
         * Enabled
         */
        this.addButton(new Button(12) {
            final String configPath = getPath(mentionType, "enabled", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "enabled";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.LEVER)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name." + langMentionKey))
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
                new AnvilManager(plugin, player, configPath, LANGUAGE_MANAGER.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, Boolean.parseBoolean(text));
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Permission
         */
        this.addButton(new Button(14) {
            final String configPath = getPath(mentionType, "permission", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "permission";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name." + langMentionKey))
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
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Sound
         */
        this.addButton(new Button(16) {
            final String configPath = getPath(mentionType, "sound", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "sound";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NOTE_BLOCK)
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
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Keyword
         */
        if(mentionType != MentionType.PLAYER) {
            this.addButton(new Button(30) {
                final String configPath = getPath(mentionType, "keyword", group);
                final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "keyword";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.NAME_TAG)
                            .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name." + langMentionKey))
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
                                new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                            }));
                }
            });
        }

        /*
         * Display
         */
        this.addButton(new Button(32) {
            final String configPath = getPath(mentionType, "display", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "display";
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ITEM_FRAME)
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
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Cooldown
         */
        this.addButton(new Button(34) {
            final String configPath = getPath(mentionType, "cooldown", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "cooldown";
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
                            config.getUnsavedConfig().set(configPath, plugin.parseInt(text));
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Player Customized Display
         */
        if(mentionType == MentionType.PLAYER) {
            this.addButton(new Button(51) {
                final String configPath = getPath(mentionType, "customized_display", group);
                final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "customized_display";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(plugin.getMaterial(DMaterial.GLOW_ITEM_FRAME, DMaterial.PAINTING))
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
                                new MentionTypeSettingsMenu(plugin, player, mentionType, group);
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
                            .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                            .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, ClickType clickType) {
                    if(clickType == ClickType.LEFT) {
                        new SubMentionAddGroupMenu(plugin, player);
                    }else if(clickType == ClickType.RIGHT) {
                        config.getUnsavedConfig().set("group.list." + group, null);
                        new MentionSettingsMenu(plugin, player);
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
                            .name(LANGUAGE_MANAGER.getString(menuSection, langPathName + ".display_name"))
                            .lore(LANGUAGE_MANAGER.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, ClickType clickType) {
                    new SubDisabledGroupsMenu(plugin, player);
                }
            });
        }

        /*
         * Nearby Radius
         */
        else if(mentionType == MentionType.NEARBY) {
            this.addButton(new Button(51) {
                final String configPath = getPath(mentionType, "radius", group);
                final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "radius";
                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.ENDER_PEARL)
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
                                config.getUnsavedConfig().set(configPath, plugin.parseInt(text));
                                new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                            }));
                }
            });
        }

        /*
         * Close
         */
        this.runOnClose(inv -> plugin.setGuiConfigEditor(null));

        this.displayTo(player);
        plugin.setGuiConfigEditor(player);
    }

    private String getPath(MentionType mentionType, String path, String group) {
        if(mentionType == MentionType.GROUP && insiderVar.contains(path)) {
            return mentionType.toString().toLowerCase() + ".list." + group + "." + path;
        }
        return mentionType.toString().toLowerCase() + "." + path;
    }
}
