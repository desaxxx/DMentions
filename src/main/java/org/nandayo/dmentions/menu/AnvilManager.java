package org.nandayo.DMentions.menu;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nandayo.DMentions.DMentions;
import org.nandayo.DMentions.service.GUIManager;

import java.util.Arrays;
import java.util.function.Consumer;

public class AnvilManager {

    private final DMentions plugin;
    private final GUIManager manager;
    private final Player player;
    private final String path;
    private final String title;
    private final Consumer<String> onFinish;

    public AnvilManager(DMentions plugin, GUIManager guiConfigManager, Player player, String path, String title, Consumer<String> onFinish) {
        this.plugin = plugin;
        this.manager = guiConfigManager;
        this.player = player;
        this.path = path;
        this.title = title;
        this.onFinish = onFinish;

        open();
    }

    public void open() {
        Object defaultValue = manager.getUValue(path, "");
        new AnvilGUI.Builder()
                .plugin(plugin)
                .itemLeft(new ItemStack(Material.PAPER))
                .itemOutput(new ItemStack(Material.NAME_TAG))
                .interactableSlots(AnvilGUI.Slot.OUTPUT)
                .onClick((slot, stateSnapshot) -> Arrays.asList(
                        AnvilGUI.ResponseAction.run(() -> onFinish.accept(stateSnapshot.getText()))))
                .title(title)
                .text(String.valueOf(manager.getUValue(path, defaultValue)))
                .open(player);
    }
}
