package com.craftserver.crateplugin.listeners;

import com.craftserver.crateplugin.CratePlugin;
import com.craftserver.crateplugin.gui.CrateSpinGUI;
import com.craftserver.crateplugin.model.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class CrateInteractListener implements Listener {

    private final CratePlugin plugin;

    public CrateInteractListener(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Optional<Crate> crateOpt = plugin.getCrateManager().byLocation(event.getClickedBlock().getLocation());
        if (crateOpt.isEmpty()) return;

        event.setCancelled(true);
        Crate crate = crateOpt.get();

        if (CrateSpinGUI.isSpinning(player)) return;

        if (!plugin.getKeyManager().hasKey(player, crate.getKeyId())) {
            player.sendMessage(plugin.msg("need-key").replace("%key%", crate.getKeyId()));
            return;
        }

        if (crate.getRewards().isEmpty()) {
            player.sendMessage(plugin.msg("crate-not-found").replace("%crate%", crate.getId() + " (no rewards configured)"));
            return;
        }

        plugin.getKeyManager().consumeKey(player, crate.getKeyId());
        player.sendMessage(plugin.msg("key-consumed")
                .replace("%key%", crate.getKeyId())
                .replace("%crate%", crate.getId()));

        var result = crate.rollReward();
        CrateSpinGUI.open(plugin, player, crate, result);
    }
}
