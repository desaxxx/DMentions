package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.guimanager.MenuType;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dapi.object.DEnchantment;
import org.nandayo.dapi.object.DMaterial;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.enumeration.MentionType;
import org.nandayo.dmentions.util.DUtil;

import java.util.*;

public class MentionTypeSettingsMenu extends BaseMenu {

    /*
     * Insider variables in group section
     */
    private final List<String> insiderVar = Arrays.asList("sound","display","cooldown");

    private final @NotNull MentionType mentionType;
    private final @Nullable String group;

    public MentionTypeSettingsMenu(@NotNull DMentions plugin, @NotNull Player player, @NotNull MentionType mentionType, @Nullable String group) {
        super(plugin, player);
        this.mentionType = mentionType;
        this.group = group;
        open();
    }

    @Override
    protected void open() {
        ConfigurationSection menuSection = guiRegistry.getSection("menu.mention_type_settings_menu");
        final String langMentionKey = mentionType == MentionType.GROUP ? "group" : "other";
        createInventory(MenuType.CHEST_6_ROWS,
                guiRegistry.getString(menuSection, "title." + langMentionKey)
                        .replace("{mentionType}", mentionType.name().toLowerCase(Locale.ENGLISH))
                        .replace("{group}", group == null ? "?" : group));

        /*
         * Glass Fillers
         */
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(1, 10, 19, 28, 37, 46);
            }

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
        addButton(new Button() {
            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(9);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.COMPASS)
                        .name(guiRegistry.getString("menu.general_button.display_name"))
                        .lore(guiRegistry.getStringList("menu.general_button.lore.not_viewing"))
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new GeneralSettingsMenu(plugin, player);
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
                return ItemCreator.of(mentionType.getIconMaterial())
                        .name(guiRegistry.getString("menu.mention_button.display_name"))
                        .lore(guiRegistry.getStringList("menu.mention_button.lore.go_back"))
                        .enchant(DUtil.getEnchantment(DEnchantment.UNBREAKING, DEnchantment.DURABILITY), 1)
                        .flags(ItemFlag.values())
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
                        .name(guiRegistry.getString("menu.reset_changes.display_name"))
                        .lore(guiRegistry.getStringList("menu.reset_changes.lore"))
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
                        .name(guiRegistry.getString("menu.save_changes.display_name"))
                        .lore(guiRegistry.getStringList("menu.save_changes.lore"))
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
         * Enabled
         */
        addButton(new Button() {
            final String configPath = getPath(mentionType, "enabled", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "enabled";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(12);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.LEVER)
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name." + langMentionKey))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for(String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, Boolean.parseBoolean(text));
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Permission
         */
        addButton(new Button() {
            final String configPath = getPath(mentionType, "permission", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "permission";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(14);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.PAPER)
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name." + langMentionKey))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, text);
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Sound
         */
        addButton(new Button() {
            final String configPath = getPath(mentionType, "sound", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "sound";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(16);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NOTE_BLOCK)
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
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
            addButton(new Button() {
                final String configPath = getPath(mentionType, "keyword", group);
                final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "keyword";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(30);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.NAME_TAG)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name." + langMentionKey))
                            .lore(() -> {
                                List<String> lore = new ArrayList<>();
                                for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                    lore.add(config.getValueDisplayMessage(line, configPath));
                                }
                                return lore;
                            })
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
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
        addButton(new Button() {
            final String configPath = getPath(mentionType, "display", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "display";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(32);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.ITEM_FRAME)
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, text);
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Cooldown
         */
        addButton(new Button() {
            final String configPath = getPath(mentionType, "cooldown", group);
            final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
            final String langPathName = "cooldown";

            @Override
            public @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(34);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.CLOCK)
                        .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                        .lore(() -> {
                            List<String> lore = new ArrayList<>();
                            for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                lore.add(config.getValueDisplayMessage(line, configPath));
                            }
                            return lore;
                        })
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
                        ((text) -> {
                            config.getUnsavedConfig().set(configPath, DUtil.parseInt(text,0));
                            new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                        }));
            }
        });

        /*
         * Player Customized Display
         */
        if(mentionType == MentionType.PLAYER) {
            addButton(new Button() {
                final String configPath = getPath(mentionType, "customized_display", group);
                final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "customized_display";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(51);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(DUtil.getMaterial(DMaterial.GLOW_ITEM_FRAME, DMaterial.PAINTING))
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                            .lore(() -> {
                                List<String> lore = new ArrayList<>();
                                for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                    lore.add(config.getValueDisplayMessage(line, configPath));
                                }
                                return lore;
                            })
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
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
            addButton(new Button() {
                final String langPathName = "add_delete_group";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(51);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.BLACK_BANNER)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                            .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
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
            addButton(new Button() {
                final String langPathName = "disabled_groups";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(49);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.REDSTONE)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                            .lore(guiRegistry.getStringList(menuSection, langPathName + ".lore"))
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    new SubDisabledGroupsMenu(plugin, player);
                }
            });
        }

        /*
         * Nearby Radius
         */
        else if(mentionType == MentionType.NEARBY) {
            addButton(new Button() {
                final String configPath = getPath(mentionType, "radius", group);
                final String changed = config.isValueChanged(configPath) ? "changed" : "unchanged";
                final String langPathName = "radius";

                @Override
                public @NotNull Set<Integer> getSlots() {
                    return Sets.newHashSet(51);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(Material.ENDER_PEARL)
                            .name(guiRegistry.getString(menuSection, langPathName + ".display_name"))
                            .lore(() -> {
                                List<String> lore = new ArrayList<>();
                                for (String line : guiRegistry.getStringList(menuSection, langPathName + ".lore." + changed)) {
                                    lore.add(config.getValueDisplayMessage(line, configPath));
                                }
                                return lore;
                            })
                            .get();
                }

                @Override
                public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                    new AnvilManager(plugin, player, configPath, guiRegistry.getString(menuSection, langPathName + ".edit_title"),
                            ((text) -> {
                                config.getUnsavedConfig().set(configPath, DUtil.parseInt(text,0));
                                new MentionTypeSettingsMenu(plugin, player, mentionType, group);
                            }));
                }
            });
        }


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
