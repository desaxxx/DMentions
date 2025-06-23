package org.nandayo.dmentions.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nandayo.dapi.guimanager.Menu;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.Config;
import org.nandayo.dmentions.service.LanguageManager;

public abstract class BaseMenu extends Menu {

    protected final @NotNull DMentions plugin;
    protected final @NotNull Config config;
    protected final @NotNull LanguageManager LANGUAGE_MANAGER;
    protected final @NotNull Player player;

    public BaseMenu(@NotNull DMentions plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.LANGUAGE_MANAGER = plugin.getLanguageManager();
        this.player = player;
    }

    abstract void open();
}
