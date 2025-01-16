package org.nandayo.GUIManager;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Button {

    private final int slot;

    public Button(int slot) {
        this.slot = slot;
    }
    public final int getSlot() {
        return this.slot;
    }

    public abstract ItemStack getItem();

    public abstract void onClick(Player p, ClickType clickType);
}
