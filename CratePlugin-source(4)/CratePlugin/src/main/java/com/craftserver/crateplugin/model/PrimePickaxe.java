package com.craftserver.crateplugin.model;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/** The special "Prime Case" 3x3 mining pickaxe. */
public class PrimePickaxe {

    public static final String TAG_KEY = "prime_pickaxe";

    public static ItemStack create(NamespacedKey key) {
        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pick.getItemMeta();
        meta.setDisplayName("\u00a76\u00a7lPrime Pickaxe");
        meta.setLore(List.of(
                "\u00a77Mines a 3x3 area of blocks",
                "\u00a77in one hit.",
                "",
                "\u00a76From the Prime Case"
        ));
        meta.addEnchant(Enchantment.DIG_SPEED, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        pick.setItemMeta(meta);
        return pick;
    }

    public static boolean isPrimePickaxe(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return val != null && val == (byte) 1;
    }
}
