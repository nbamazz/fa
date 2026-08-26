package com.craftserver.crateplugin.listeners;

import com.craftserver.crateplugin.gui.CrateSpinGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** Prevents players from touching items while a crate spin animation is running. */
public class SpinGuiGuardListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (CrateSpinGUI.isSpinning(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (CrateSpinGUI.isSpinning(player)) {
            event.setCancelled(true);
        }
    }
}
