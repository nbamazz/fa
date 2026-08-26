package com.craftserver.crateplugin.listeners;

import com.craftserver.crateplugin.CratePlugin;
import com.craftserver.crateplugin.model.PrimePickaxe;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * When a player breaks a block with the Prime Pickaxe, also breaks the 3x3 area
 * of blocks around that block on the same face plane, dropping items naturally.
 */
public class PrimePickaxeListener implements org.bukkit.event.Listener {

    private final CratePlugin plugin;
    private final NamespacedKey tag;

    public PrimePickaxeListener(CratePlugin plugin) {
        this.plugin = plugin;
        this.tag = new NamespacedKey(plugin, PrimePickaxe.TAG_KEY);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!PrimePickaxe.isPrimePickaxe(tool, tag)) return;

        Block origin = event.getBlock();

        // Figure out which plane to expand on based on the player's look direction (simple 3x3 on the flattest axis)
        double pitch = Math.abs(player.getLocation().getPitch());
        boolean lookingVertical = pitch > 60; // mining straight up/down -> expand on X/Z plane

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue; // origin block already handled by the event itself
                Block target;
                if (lookingVertical) {
                    target = origin.getRelative(a, 0, b);
                } else {
                    // expand based on dominant horizontal facing so the 3x3 is "facing" the player, not floor-flat
                    float yaw = player.getLocation().getYaw();
                    boolean facingXAxis = Math.abs(Math.cos(Math.toRadians(yaw))) > Math.abs(Math.sin(Math.toRadians(yaw)));
                    target = facingXAxis
                            ? origin.getRelative(0, a, b)
                            : origin.getRelative(b, a, 0);
                }

                if (target.getType().isAir() || !target.getType().isSolid()) continue;
                if (target.getType() == org.bukkit.Material.BEDROCK) continue;

                Location loc = target.getLocation();
                target.breakNaturally(tool);
            }
        }
    }
}
