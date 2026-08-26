package com.craftserver.crateplugin;

import com.craftserver.crateplugin.commands.CrateCommand;
import com.craftserver.crateplugin.listeners.CrateInteractListener;
import com.craftserver.crateplugin.listeners.PrimePickaxeListener;
import com.craftserver.crateplugin.listeners.SpinGuiGuardListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class CratePlugin extends JavaPlugin {

    private CrateManager crateManager;
    private KeyManager keyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        this.keyManager = new KeyManager(this);
        this.crateManager = new CrateManager(this);

        CrateCommand crateCommand = new CrateCommand(this);
        getCommand("crate").setExecutor(crateCommand);
        getCommand("crate").setTabCompleter(crateCommand);

        getServer().getPluginManager().registerEvents(new CrateInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PrimePickaxeListener(this), this);
        getServer().getPluginManager().registerEvents(new SpinGuiGuardListener(), this);

        getLogger().info("CratePlugin enabled - " + crateManager.all().size() + " crate(s) loaded.");
    }

    @Override
    public void onDisable() {
        if (crateManager != null) crateManager.save();
    }

    public CrateManager getCrateManager() { return crateManager; }
    public KeyManager getKeyManager() { return keyManager; }

    /** Fetches a message from config.yml under messages.<key>, colorized, prefixed. */
    public String msg(String key) {
        String prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix", ""));
        String raw = getConfig().getString("messages." + key, key);
        return prefix + ChatColor.translateAlternateColorCodes('&', raw);
    }
}
