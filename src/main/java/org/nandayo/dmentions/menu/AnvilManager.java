package org.nandayo.dmentions.menu;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nandayo.dapi.guimanager.button.Button;
import org.nandayo.dapi.guimanager.menu.AnvilMenu;
import org.nandayo.dapi.util.ItemCreator;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.Config;

import java.util.Set;
import java.util.function.Consumer;

public class AnvilManager extends AnvilMenu {

    private final Config config;
    private final Player player;
    private final String path;
    private final String title;
    private final Consumer<String> onFinish;

    public AnvilManager(DMentions plugin, Player player, String path, String title, Consumer<String> onFinish) {
        this.config = plugin.getConfiguration();
        this.player = player;
        this.path = path;
        this.title = title;
        this.onFinish = onFinish;

        open();
    }

    private void open() {
        createInventory(player, title);

        addButton(new Button() {
            @Override
            protected @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(0);
            }

            @Override
            public @Nullable ItemStack getItem() {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    meta.setDisplayName(config.getUnsavedConfig().getString(path, ""));
                    item.setItemMeta(meta);
                }
                return item;
            }
        });

        addButton(new Button() {
            @Override
            protected @NotNull Set<Integer> getSlots() {
                return Sets.newHashSet(2);
            }

            @Override
            public @Nullable ItemStack getItem() {
                return ItemCreator.of(Material.NAME_TAG)
                        .get();
            }

            @Override
            public void onClick(@NotNull Player p, @NotNull ClickType clickType) {
                String text = getText();
                onFinish.accept(text == null ? "" : text);
            }
        });

        displayTo(player);
    }
}
