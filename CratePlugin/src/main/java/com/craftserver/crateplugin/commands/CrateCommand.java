package com.craftserver.crateplugin.commands;

import com.craftserver.crateplugin.CratePlugin;
import com.craftserver.crateplugin.model.Crate;
import com.craftserver.crateplugin.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final CratePlugin plugin;

    public CrateCommand(CratePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("\u00a76/crate create <id>\u00a77, setblock <id> <material>, addreward <id> <material|PRIME_PICKAXE> <amount> <weight> <name...>, key <player> <keyId> <amount>, keyall <keyId> <amount>, open <id>, list, reload");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(sender, args);
            case "setblock" -> handleSetBlock(sender, args);
            case "addreward" -> handleAddReward(sender, args);
            case "removecrate" -> handleRemove(sender, args);
            case "key" -> handleKey(sender, args);
            case "keyall" -> handleKeyAll(sender, args);
            case "open" -> handleOpen(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(plugin.msg("prefix") + "\u00a7cUnknown subcommand.");
        }
        return true;
    }

    // ---------- admin subcommands ----------

    private boolean requireAdmin(CommandSender sender) {
        if (!sender.hasPermission("crate.admin")) {
            sender.sendMessage(plugin.msg("no-permission"));
            return false;
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /crate create <id>"); return; }
        String id = args[1].toLowerCase();
        if (plugin.getCrateManager().exists(id)) {
            sender.sendMessage("\u00a7cA crate with that id already exists.");
            return;
        }
        plugin.getCrateManager().getOrCreate(id);
        plugin.getCrateManager().save();
        sender.sendMessage(plugin.msg("crate-created").replace("%crate%", id));
    }

    /** /crate setblock <id> <material> - binds the block the player is looking at (or places one) as this crate. */
    private void handleSetBlock(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cPlayers only."); return; }
        if (args.length < 3) { sender.sendMessage("\u00a7cUsage: /crate setblock <id> <material>"); return; }

        String id = args[1].toLowerCase();
        Material material = Material.matchMaterial(args[2].toUpperCase());
        if (material == null || !material.isBlock()) {
            sender.sendMessage("\u00a7cUnknown block material: " + args[2]);
            return;
        }

        RayTraceResult ray = player.rayTraceBlocks(6);
        if (ray == null || ray.getHitBlock() == null) {
            sender.sendMessage("\u00a7cLook at a block within 6 blocks to bind it as the crate.");
            return;
        }

        Block block = ray.getHitBlock();
        block.setType(material);

        Crate crate = plugin.getCrateManager().getOrCreate(id);
        crate.setBlockMaterial(material);
        crate.setLocation(block.getLocation());
        plugin.getCrateManager().save();

        sender.sendMessage(plugin.msg("crate-block-set").replace("%crate%", id).replace("%material%", material.name()));
    }

    private void handleAddReward(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (args.length < 6) {
            sender.sendMessage("\u00a7cUsage: /crate addreward <id> <material|PRIME_PICKAXE> <amount> <weight> <name...>");
            return;
        }
        String id = args[1].toLowerCase();
        if (!plugin.getCrateManager().exists(id)) {
            sender.sendMessage(plugin.msg("crate-not-found").replace("%crate%", id));
            return;
        }

        String typeArg = args[2].toUpperCase();
        int amount = parseIntOr(args[3], 1);
        int weight = parseIntOr(args[4], 1);
        String name = String.join(" ", List.of(args).subList(5, args.length));

        Reward reward;
        if (typeArg.equals("PRIME_PICKAXE")) {
            reward = new Reward(name, Reward.Type.PRIME_PICKAXE, Material.DIAMOND_PICKAXE, 1, weight);
        } else {
            Material mat = Material.matchMaterial(typeArg);
            if (mat == null) { sender.sendMessage("\u00a7cUnknown material: " + typeArg); return; }
            reward = new Reward(name, Reward.Type.ITEM, mat, amount, weight);
        }

        plugin.getCrateManager().get(id).get().addReward(reward);
        plugin.getCrateManager().save();
        sender.sendMessage(plugin.msg("reward-added").replace("%reward%", name).replace("%crate%", id).replace("%weight%", String.valueOf(weight)));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /crate removecrate <id>"); return; }
        String id = args[1].toLowerCase();
        if (!plugin.getCrateManager().exists(id)) {
            sender.sendMessage(plugin.msg("crate-not-found").replace("%crate%", id));
            return;
        }
        plugin.getCrateManager().remove(id);
        plugin.getCrateManager().save();
        sender.sendMessage("\u00a7aRemoved crate " + id + ".");
    }

    /** /crate key <player> <keyId> <amount> */
    private void handleKey(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (args.length < 4) { sender.sendMessage("\u00a7cUsage: /crate key <player> <keyId> <amount>"); return; }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("\u00a7cPlayer not found or offline: " + args[1]); return; }

        String keyId = args[2];
        int amount = parseIntOr(args[3], 1);

        target.getInventory().addItem(plugin.getKeyManager().createKey(keyId, amount));
        target.sendMessage(plugin.msg("key-received").replace("%amount%", String.valueOf(amount)).replace("%key%", keyId));
        sender.sendMessage(plugin.msg("key-given").replace("%amount%", String.valueOf(amount)).replace("%key%", keyId).replace("%player%", target.getName()));
    }

    /**
     * /crate keyall <keyId> <amount> — OP command: gives EVERY online player
     * <amount> keys of whichever key type the OP chose.
     */
    private void handleKeyAll(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (args.length < 3) { sender.sendMessage("\u00a7cUsage: /crate keyall <keyId> <amount>"); return; }

        String keyId = args[1];
        int amount = parseIntOr(args[2], 1);
        if (amount < 1) { sender.sendMessage("\u00a7cAmount must be at least 1."); return; }

        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.getInventory().addItem(plugin.getKeyManager().createKey(keyId, amount));
            online.sendMessage(plugin.msg("key-received").replace("%amount%", String.valueOf(amount)).replace("%key%", keyId));
            count++;
        }

        sender.sendMessage(plugin.msg("key-all-sent")
                .replace("%amount%", String.valueOf(amount))
                .replace("%key%", keyId)
                .replace("%count%", String.valueOf(count)));
    }

    private void handleOpen(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) return;
        if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cPlayers only."); return; }
        if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /crate open <id>  (admin test-open, no key required)"); return; }

        String id = args[1].toLowerCase();
        var crateOpt = plugin.getCrateManager().get(id);
        if (crateOpt.isEmpty() || crateOpt.get().getRewards().isEmpty()) {
            sender.sendMessage(plugin.msg("crate-not-found").replace("%crate%", id));
            return;
        }

        Crate crate = crateOpt.get();
        var result = crate.rollReward();
        com.craftserver.crateplugin.gui.CrateSpinGUI.open(plugin, player, crate, result);
    }

    private void handleList(CommandSender sender) {
        var crates = plugin.getCrateManager().all();
        if (crates.isEmpty()) { sender.sendMessage("\u00a7cNo crates configured yet."); return; }
        sender.sendMessage("\u00a76Crates: \u00a7f" + String.join(", ", crates.keySet()));
    }

    private void handleReload(CommandSender sender) {
        if (!requireAdmin(sender)) return;
        plugin.reloadConfig();
        plugin.getCrateManager().load();
        sender.sendMessage("\u00a7aCratePlugin reloaded.");
    }

    private int parseIntOr(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("create", "setblock", "addreward", "removecrate", "key", "keyall", "open", "list", "reload"));
        } else if (args.length == 2 && List.of("setblock", "addreward", "removecrate", "open").contains(args[0].toLowerCase())) {
            options.addAll(plugin.getCrateManager().all().keySet());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("key")) {
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
        }
        String partial = args[args.length - 1].toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(partial)).collect(Collectors.toList());
    }
}
