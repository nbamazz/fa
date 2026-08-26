package com.craftserver.crateplugin.listeners;

import com.craftserver.crateplugin.CratePlugin;
import com.craftserver.crateplugin.model.ExcavatorPickaxe;
import com.craftserver.crateplugin.model.PrimePickaxe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * When a player breaks a block with the Prime Pickaxe OR the Excavator Pickaxe,
 * also breaks the 3x3 area of blocks around that block on the same face plane,
 * dropping items naturally. Both custom pickaxes share this same mining behavior.
 */
public class ThreeByThreePickaxeListener implements Listener {

    private final NamespacedKey primeTag;
    private final NamespacedKey excavatorTag;

    public ThreeByThreePickaxeListener(CratePlugin plugin) {
        this.primeTag = new NamespacedKey(plugin, PrimePickaxe.TAG_KEY);
        this.excavatorTag = new NamespacedKey(plugin, ExcavatorPickaxe.TAG_KEY);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        boolean isPrime = PrimePickaxe.isPrimePickaxe(tool, primeTag);
        boolean isExcavator = ExcavatorPickaxe.isExcavatorPickaxe(tool, excavatorTag);
        if (!isPrime && !isExcavator) return;

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
                if (target.getType() == Material.BEDROCK) continue;

                target.breakNaturally(tool);
            }
        }
    }
}
