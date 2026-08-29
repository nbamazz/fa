package com.primesmp.settings.listeners;

import com.primesmp.settings.PrimeSMPSettings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Cancels chat messages sent by a player who has chat disabled for
 * themselves. Uses the legacy AsyncPlayerChatEvent for compatibility across
 * Spigot and Paper; on newer Paper builds you may prefer
 * io.papermc.paper.event.player.AsyncChatEvent for Adventure Component
 * support instead.
 */
public class ChatListener implements Listener {

    private final PrimeSMPSettings plugin;

    public ChatListener(PrimeSMPSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataManager().isChatDisabled(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your chat is currently disabled. Use /primesettings status to check your settings.");
        }
    }
}
