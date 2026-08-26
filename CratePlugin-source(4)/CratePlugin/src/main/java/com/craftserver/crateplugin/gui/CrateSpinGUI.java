package com.craftserver.crateplugin.gui;

import com.craftserver.crateplugin.CratePlugin;
import com.craftserver.crateplugin.model.Crate;
import com.craftserver.crateplugin.model.ExcavatorPickaxe;
import com.craftserver.crateplugin.model.PrimePickaxe;
import com.craftserver.crateplugin.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * A single-row 9-slot inventory that scrolls reward icons across the row and
 * slows to a stop on the center slot (slot 4) — a CS2 case-opening style reel.
 */
public class CrateSpinGUI {

    private static final int SIZE = 9;
    private static final int CENTER_SLOT = 4;
    private static final Random RANDOM = new Random();

    /** Tracks which players currently have a spin open so their clicks can be blocked. */
    private static final Set<UUID> SPINNING = new HashSet<>();

    public static boolean isSpinning(Player player) {
        return SPINNING.contains(player.getUniqueId());
    }

    public static void open(CratePlugin plugin, Player player, Crate crate, Reward result) {
        Inventory inv = Bukkit.createInventory(null, SIZE, "\u00a76\u00a7lOpening: " + crate.getId());
        List<Reward> pool = crate.getRewards();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, randomIcon(pool));
        }
        player.openInventory(inv);
        SPINNING.add(player.getUniqueId());

        int durationTicks = plugin.getConfig().getInt("spin.duration-ticks", 60);
        int startSpeed = plugin.getConfig().getInt("spin.start-speed", 2);
        int endSpeed = plugin.getConfig().getInt("spin.end-speed", 8);

        new BukkitRunnable() {
            int elapsed = 0;
            int nextTickAt = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.getOpenInventory().getTopInventory() != inv) {
                    SPINNING.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (elapsed >= durationTicks) {
                    // land on the real result
                    ItemStack finalIcon = result.previewIcon();
                    ItemMeta meta = finalIcon.getItemMeta();
                    meta.setDisplayName("\u00a7a\u00a7l" + result.getName());
                    finalIcon.setItemMeta(meta);
                    inv.setItem(CENTER_SLOT, finalIcon);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        SPINNING.remove(player.getUniqueId());
                        player.closeInventory();
                        grant(plugin, player, crate, result);
                    }, 25L);

                    cancel();
                    return;
                }

                if (elapsed >= nextTickAt) {
                    // shift items left, add new random on the right — creates a "scrolling reel" look
                    for (int i = 0; i < SIZE - 1; i++) {
                        inv.setItem(i, inv.getItem(i + 1));
                    }
                    inv.setItem(SIZE - 1, randomIcon(pool));
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.6f, 1.5f);

                    // ease from startSpeed to endSpeed as we approach the end (slows down like CS2 cases)
                    double progress = (double) elapsed / durationTicks;
                    int currentSpeed = (int) Math.round(startSpeed + (endSpeed - startSpeed) * progress);
                    nextTickAt = elapsed + Math.max(1, currentSpeed);
                }

                elapsed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static ItemStack randomIcon(List<Reward> pool) {
        if (pool.isEmpty()) return new ItemStack(org.bukkit.Material.BARRIER);
        Reward r = pool.get(RANDOM.nextInt(pool.size()));
        ItemStack icon = r.previewIcon();
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName("\u00a7f" + r.getName());
        icon.setItemMeta(meta);
        return icon;
    }

    private static void grant(CratePlugin plugin, Player player, Crate crate, Reward result) {
        ItemStack toGive;
        if (result.getType() == Reward.Type.PRIME_PICKAXE) {
            NamespacedKey tag = new NamespacedKey(plugin, PrimePickaxe.TAG_KEY);
            toGive = PrimePickaxe.create(tag);
        } else if (result.getType() == Reward.Type.EXCAVATOR_PICKAXE) {
            NamespacedKey tag = new NamespacedKey(plugin, ExcavatorPickaxe.TAG_KEY);
            toGive = ExcavatorPickaxe.create(tag);
        } else {
            toGive = new ItemStack(result.getMaterial(), Math.max(1, result.getAmount()));
            result.applyEnchants(toGive);
        }

        player.getInventory().addItem(toGive).values()
                .forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));

        String msg = plugin.msg("win-broadcast")
                .replace("%player%", player.getName())
                .replace("%crate%", crate.getId())
                .replace("%reward%", result.getName());
        Bukkit.broadcastMessage(msg);
    }
}
