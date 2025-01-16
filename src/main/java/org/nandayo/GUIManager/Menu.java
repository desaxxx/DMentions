package org.nandayo.GUIManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.nandayo.Main;
import org.nandayo.utils.HexUtil;
import org.nandayo.utils.ItemCreator;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    final private List<Button> buttons = new ArrayList<>();

    private int size = 27;
    private String title = "Default";
    private List<Integer> fillers = new ArrayList<>();
    private ItemStack fillerMaterial = ItemCreator.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").lore("").get();

    public final List<Button> getButtons() {
        return this.buttons;
    }

    protected final void addButton(Button button) {
        this.buttons.add(button);
    }

    protected final void setSize(int size) {
        this.size = size;
    }

    protected final void setTitle(String title) {
        this.title = title;
    }

    protected final void setFillers(List<Integer> fillers) {
        this.fillers = fillers;
    }

    protected final void setFillerMaterial(ItemStack fillerMaterial) {
        this.fillerMaterial = fillerMaterial;
    }

    protected final void displayTo(Player p) {
        Inventory inv = Bukkit.createInventory(p, this.size, HexUtil.color(this.title));

        for(int i : fillers) {
            inv.setItem(i, fillerMaterial);
        }

        for(Button button : buttons) {
            inv.setItem(button.getSlot(), button.getItem());
        }

        p.openInventory(inv);
        p.setMetadata("openGui", new FixedMetadataValue(Main.inst(), this));
    }
}
