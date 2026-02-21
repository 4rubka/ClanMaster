package com.clanmaster.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Small helper for inventory items.
 */
public class ItemBuilder {

    private final ItemStack stack;

    public ItemBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    public ItemBuilder name(String name) {
        if (name == null) name = "";
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Text.color(name));
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(lore.stream().map(Text::color).toList());
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder hideFlags() {
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return stack;
    }
}
