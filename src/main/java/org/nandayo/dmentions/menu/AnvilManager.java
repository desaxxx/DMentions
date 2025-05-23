package org.nandayo.dmentions.menu;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nandayo.dmentions.DMentions;
import org.nandayo.dmentions.service.Config;

import java.util.Collections;
import java.util.function.Consumer;

public class AnvilManager {

    private final DMentions plugin;
    private final Config config;
    private final Player player;
    private final String path;
    private final String title;
    private final Consumer<String> onFinish;

    public AnvilManager(DMentions plugin, Player player, String path, String title, Consumer<String> onFinish) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.player = player;
        this.path = path;
        this.title = title;
        this.onFinish = onFinish;

        open();
    }

    public void open() {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .itemLeft(new ItemStack(Material.PAPER))
                .itemOutput(new ItemStack(Material.NAME_TAG))
                .interactableSlots(AnvilGUI.Slot.OUTPUT)
                .onClick((slot, stateSnapshot) -> Collections.singletonList(
                        AnvilGUI.ResponseAction.run(() -> onFinish.accept(stateSnapshot.getText()))))
                .title(title)
                .text(config.getUnsavedConfig().getString(path, ""))
                .open(player);
    }
}
