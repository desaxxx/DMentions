package org.nandayo.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemCreator {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemCreator(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }
    public static ItemCreator of(ItemStack itemStack) {
        return new ItemCreator(itemStack);
    }
    public static ItemCreator of(Material material) {
        return new ItemCreator(new ItemStack(material));
    }
    //Get
    public ItemStack get() {
        if(meta != null) {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    //Modify
    public ItemCreator amount(int amount) {
        if(itemStack != null) {
            itemStack.setAmount(amount);
        }
        return this;
    }
    public ItemCreator name(String name) {
        if(meta != null && name != null) {
            meta.setDisplayName(HexUtil.color(name));
        }
        return this;
    }
    public ItemCreator lore(String... lore) {
        if(meta != null) {
            List<String> loreFix = new ArrayList<>();
            for(String line : lore) {
                loreFix.add(HexUtil.color(line));
            }
            meta.setLore(loreFix);
        }
        return this;
    }
    public ItemCreator lore(List<String> lore) {
        return lore(lore.toArray(new String[0]));
    }
    public ItemCreator enchant(Enchantment enchantment, int level) {
        if(meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }
    public ItemCreator hideFlag(ItemFlag... flags) {
        if(meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }
    public ItemCreator unbreakable(boolean unbreakable) {
        if(meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }
}
