package com.craftserver.crateplugin.model;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single possible prize inside a crate. Either a normal material+amount stack
 * (optionally enchanted), or the special PRIME_PICKAXE type which is built by
 * PrimePickaxe.create().
 */
public class Reward {

    public enum Type { ITEM, PRIME_PICKAXE, EXCAVATOR_PICKAXE }

    private final String name;
    private final Type type;
    private final Material material;
    private final int amount;
    private final int weight;
    /** enchant key name (e.g. "sharpness") -> level */
    private final Map<String, Integer> enchants;

    public Reward(String name, Type type, Material material, int amount, int weight) {
        this(name, type, material, amount, weight, new LinkedHashMap<>());
    }

    public Reward(String name, Type type, Material material, int amount, int weight, Map<String, Integer> enchants) {
        this.name = name;
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.weight = weight;
        this.enchants = enchants;
    }

    public static Reward fromConfig(ConfigurationSection section) {
        String name = section.getString("name", "Reward");
        int weight = Math.max(1, section.getInt("weight", 1));
        String typeStr = section.getString("type", "ITEM");

        Map<String, Integer> enchants = new LinkedHashMap<>();
        ConfigurationSection enchSec = section.getConfigurationSection("enchants");
        if (enchSec != null) {
            for (String key : enchSec.getKeys(false)) {
                enchants.put(key.toLowerCase(), enchSec.getInt(key));
            }
        }

        if (typeStr.equalsIgnoreCase("PRIME_PICKAXE")) {
            return new Reward(name, Type.PRIME_PICKAXE, Material.DIAMOND_PICKAXE, 1, weight, enchants);
        }
        if (typeStr.equalsIgnoreCase("EXCAVATOR_PICKAXE")) {
            return new Reward(name, Type.EXCAVATOR_PICKAXE, Material.NETHERITE_PICKAXE, 1, weight, enchants);
        }

        Material mat = Material.matchMaterial(section.getString("material", "STONE"));
        if (mat == null) mat = Material.STONE;
        int amount = section.getInt("amount", 1);
        return new Reward(name, Type.ITEM, mat, amount, weight, enchants);
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("name", name);
        section.set("weight", weight);
        section.set("type", type.name());
        if (type == Type.ITEM) {
            section.set("material", material.name());
            section.set("amount", amount);
        }
        if (!enchants.isEmpty()) {
            for (Map.Entry<String, Integer> e : enchants.entrySet()) {
                section.set("enchants." + e.getKey(), e.getValue());
            }
        }
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public int getWeight() { return weight; }
    public Map<String, Integer> getEnchants() { return enchants; }

    /** Applies this reward's enchants (if any) to the given item, using unsafe levels so anything beyond vanilla max works. */
    public void applyEnchants(ItemStack item) {
        if (enchants.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();
        for (Map.Entry<String, Integer> e : enchants.entrySet()) {
            Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(e.getKey()));
            if (ench != null) {
                meta.addEnchant(ench, e.getValue(), true);
            }
        }
        item.setItemMeta(meta);
    }

    /** Icon used only for display in the spin GUI (not the actual granted item for PRIME_PICKAXE). */
    public ItemStack previewIcon() {
        ItemStack icon;
        if (type == Type.PRIME_PICKAXE) {
            icon = new ItemStack(Material.DIAMOND_PICKAXE);
        } else if (type == Type.EXCAVATOR_PICKAXE) {
            icon = new ItemStack(Material.NETHERITE_PICKAXE);
        } else {
            icon = new ItemStack(material, Math.max(1, Math.min(amount, 64)));
        }
        applyEnchants(icon);
        return icon;
    }
}
