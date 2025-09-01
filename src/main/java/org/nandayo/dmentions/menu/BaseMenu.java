package org.nandayo.dmentions.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.menu.Menu;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.registry.GUIRegistry;

import java.util.function.Consumer;

public abstract class BaseMenu extends Menu {

    protected final @NotNull DMentions plugin;
    protected final @NotNull Config config;
    protected final @NotNull GUIRegistry guiRegistry;
    protected final @NotNull Player player;

    public BaseMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.guiRegistry = plugin.getGuiRegistry();
        this.player = player;
    }

    abstract protected void open();

    /**
     * Reset gui config editor player.
     * @return Inventory close consumer
     * @param <T> Inventory
     * @since 1.8.3
     */
    @Override
    public <T extends Inventory> Consumer<T> onClose() {
        return inv -> plugin.setGuiConfigEditor(null);
    }
}
