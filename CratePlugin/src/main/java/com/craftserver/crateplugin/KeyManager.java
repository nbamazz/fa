package com.craftserver.crateplugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class KeyManager {

    private final NamespacedKey keyTag;

    public KeyManager(CratePlugin plugin) {
        this.keyTag = new NamespacedKey(plugin, "crate_key_id");
    }

    /** Builds a physical key item for the given key id (e.g. "prime_key"). */
    public ItemStack createKey(String keyId, int amount) {
        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK, amount);
        ItemMeta meta = item.getItemMeta();
        String pretty = prettify(keyId);
        meta.setDisplayName("\u00a7e\u00a7l" + pretty);
        meta.setLore(List.of("\u00a77Right-click the matching crate", "\u00a77to open it.", "", "\u00a78key:" + keyId));
        meta.getPersistentDataContainer().set(keyTag, PersistentDataType.STRING, keyId);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isKey(ItemStack item, String keyId) {
        if (item == null || !item.hasItemMeta()) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(keyTag, PersistentDataType.STRING);
        return id != null && id.equals(keyId);
    }

    /** True if the player holds at least one key of this type anywhere in their inventory. */
    public boolean hasKey(Player player, String keyId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKey(item, keyId)) return true;
        }
        return false;
    }

    /** Removes exactly one key of this type from the player's inventory. Returns true if removed. */
    public boolean consumeKey(Player player, String keyId) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isKey(item, keyId)) {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(i, item.getAmount() <= 0 ? null : item);
                return true;
            }
        }
        return false;
    }

    private String prettify(String keyId) {
        String base = keyId.endsWith("_key") ? keyId.substring(0, keyId.length() - 4) : keyId;
        String[] parts = base.split("[_ ]");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return sb.toString().trim() + " Key";
    }
}
