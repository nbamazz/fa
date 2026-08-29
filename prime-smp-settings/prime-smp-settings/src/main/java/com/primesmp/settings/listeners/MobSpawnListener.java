package com.primesmp.settings.listeners;

import com.primesmp.settings.PrimeSMPSettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Since a mob spawn event isn't inherently tied to a specific player, this
 * listener finds the nearest online player to the spawn location and, if
 * that player is within range and has mob spawning disabled, cancels the
 * spawn. Other players elsewhere on the server are unaffected.
 *
 * Only "environmental" spawn reasons are blocked so the toggle doesn't
 * interfere with spawner farms, spawn eggs, breeding, or plugin/command
 * spawned mobs.
 */
public class MobSpawnListener implements Listener {

    private static final Set<CreatureSpawnEvent.SpawnReason> BLOCKED_REASONS = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.JOCKEY,
            CreatureSpawnEvent.SpawnReason.MOUNT,
            CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
            CreatureSpawnEvent.SpawnReason.PATROL,
            CreatureSpawnEvent.SpawnReason.RAID,
            CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE,
            CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
            CreatureSpawnEvent.SpawnReason.DROWNED,
            CreatureSpawnEvent.SpawnReason.TRAP
    );

    // Matches vanilla's natural mob spawn radius around a player.
    private static final double RADIUS = 128.0;
    private static final double RADIUS_SQUARED = RADIUS * RADIUS;

    private final PrimeSMPSettings plugin;

    public MobSpawnListener(PrimeSMPSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!BLOCKED_REASONS.contains(event.getSpawnReason())) {
            return;
        }

        Location loc = event.getLocation();
        if (loc.getWorld() == null) {
            return;
        }

        Player nearest = null;
        double nearestDistSquared = Double.MAX_VALUE;

        for (Player player : loc.getWorld().getPlayers()) {
            double distSquared = player.getLocation().distanceSquared(loc);
            if (distSquared < nearestDistSquared) {
                nearestDistSquared = distSquared;
                nearest = player;
            }
        }

        if (nearest == null || nearestDistSquared > RADIUS_SQUARED) {
            return;
        }

        if (plugin.getDataManager().isMobSpawnDisabled(nearest.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
