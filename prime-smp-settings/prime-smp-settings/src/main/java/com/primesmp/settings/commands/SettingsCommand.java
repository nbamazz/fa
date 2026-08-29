package com.primesmp.settings.commands;

import com.primesmp.settings.PrimeSMPSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /primesettings chat|mobs [on|off] [player]
 * /primesettings status [player]
 */
public class SettingsCommand implements CommandExecutor, TabCompleter {

    private final PrimeSMPSettings plugin;

    public SettingsCommand(PrimeSMPSettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "/primesettings chat|mobs [on|off] [player]");
            sender.sendMessage(ChatColor.GOLD + "/primesettings status [player]");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "chat":
                return handleToggle(sender, args, true);
            case "mobs":
                return handleToggle(sender, args, false);
            case "status":
                return handleStatus(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use chat, mobs, or status.");
                return true;
        }
    }

    private boolean handleToggle(CommandSender sender, String[] args, boolean isChat) {
        Boolean explicitValue = null;
        String targetName = null;

        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("on")) {
                explicitValue = true;
            } else if (args[1].equalsIgnoreCase("off")) {
                explicitValue = false;
            } else {
                targetName = args[1];
            }
        }
        if (args.length >= 3) {
            targetName = args[2];
        }

        Player target;
        boolean actingOnSelf = targetName == null;

        if (actingOnSelf) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name.");
                return true;
            }
            if (!sender.hasPermission("primesmp.settings.self")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("primesmp.settings.others")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to change other players' settings.");
                return true;
            }
            target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not online.");
                return true;
            }
        }

        boolean currentValue = isChat
                ? plugin.getDataManager().isChatDisabled(target.getUniqueId())
                : plugin.getDataManager().isMobSpawnDisabled(target.getUniqueId());

        boolean newValue = explicitValue != null ? explicitValue : !currentValue;

        if (isChat) {
            plugin.getDataManager().setChatDisabled(target.getUniqueId(), newValue);
        } else {
            plugin.getDataManager().setMobSpawnDisabled(target.getUniqueId(), newValue);
        }

        String feature = isChat ? "Chat" : "Mob spawning";
        String state = newValue ? "disabled" : "enabled";

        sender.sendMessage(ChatColor.GREEN + feature + " has been " + state + " for " + target.getName() + ".");
        if (!actingOnSelf) {
            target.sendMessage(ChatColor.YELLOW + "An admin has " + state + " your " + feature.toLowerCase() + ".");
        }

        return true;
    }

    private boolean handleStatus(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("primesmp.settings.others")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to check other players' settings.");
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' is not online.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name.");
                return true;
            }
            target = (Player) sender;
        }

        boolean chatDisabled = plugin.getDataManager().isChatDisabled(target.getUniqueId());
        boolean mobsDisabled = plugin.getDataManager().isMobSpawnDisabled(target.getUniqueId());

        sender.sendMessage(ChatColor.AQUA + "Settings for " + target.getName() + ":");
        sender.sendMessage(ChatColor.AQUA + " Chat disabled: " + (chatDisabled ? ChatColor.RED + "yes" : ChatColor.GREEN + "no"));
        sender.sendMessage(ChatColor.AQUA + " Mob spawning disabled: " + (mobsDisabled ? ChatColor.RED + "yes" : ChatColor.GREEN + "no"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("chat", "mobs", "status"), args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("status")) {
                return filter(onlinePlayerNames(), args[1]);
            }
            return filter(Arrays.asList("on", "off"), args[1]);
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("mobs"))) {
            return filter(onlinePlayerNames(), args[2]);
        }
        return new ArrayList<>();
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private List<String> filter(List<String> options, String prefix) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
