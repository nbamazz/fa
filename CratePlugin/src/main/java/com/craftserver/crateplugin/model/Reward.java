package com.craftserver.crateplugin.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * A single possible prize inside a crate. Either a normal material+amount stack,
 * or the special PRIME_PICKAXE type which is built by PrimePickaxe.create().
 */
public class Reward {

    public enum Type { ITEM, PRIME_PICKAXE }

    private final String name;
    private final Type type;
    private final Material material;
    private final int amount;
    private final int weight;

    public Reward(String name, Type type, Material material, int amount, int weight) {
        this.name = name;
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.weight = weight;
    }

    public static Reward fromConfig(ConfigurationSection section) {
        String name = section.getString("name", "Reward");
        int weight = Math.max(1, section.getInt("weight", 1));
        String typeStr = section.getString("type", "ITEM");

        if (typeStr.equalsIgnoreCase("PRIME_PICKAXE")) {
            return new Reward(name, Type.PRIME_PICKAXE, Material.DIAMOND_PICKAXE, 1, weight);
        }

        Material mat = Material.matchMaterial(section.getString("material", "STONE"));
        if (mat == null) mat = Material.STONE;
        int amount = section.getInt("amount", 1);
        return new Reward(name, Type.ITEM, mat, amount, weight);
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("name", name);
        section.set("weight", weight);
        section.set("type", type.name());
        if (type == Type.ITEM) {
            section.set("material", material.name());
            section.set("amount", amount);
        }
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public int getWeight() { return weight; }

    /** Icon used only for display in the spin GUI (not the actual granted item for PRIME_PICKAXE). */
    public ItemStack previewIcon() {
        if (type == Type.PRIME_PICKAXE) {
            return new ItemStack(Material.DIAMOND_PICKAXE);
        }
        return new ItemStack(material, Math.max(1, Math.min(amount, 64)));
    }
}
