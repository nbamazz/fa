package com.craftserver.crateplugin.model;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * A second, distinct 3x3-mining pickaxe - separate item from the Prime Pickaxe,
 * netherite-based instead of diamond-based, so it can be its own reward.
 */
public class ExcavatorPickaxe {

    public static final String TAG_KEY = "excavator_pickaxe";

    public static ItemStack create(NamespacedKey key) {
        ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = pick.getItemMeta();
        meta.setDisplayName("\u00a7b\u00a7lExcavator Pickaxe");
        meta.setLore(List.of(
                "\u00a77Mines a 3x3 area of blocks",
                "\u00a77in one hit.",
                "",
                "\u00a7bExcavator Pickaxe"
        ));
        meta.addEnchant(Enchantment.DIG_SPEED, 4, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        pick.setItemMeta(meta);
        return pick;
    }

    public static boolean isExcavatorPickaxe(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte val = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return val != null && val == (byte) 1;
    }
}
