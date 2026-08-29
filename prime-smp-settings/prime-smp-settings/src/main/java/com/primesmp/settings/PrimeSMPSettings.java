package com.primesmp.settings;

import com.primesmp.settings.commands.SettingsCommand;
import com.primesmp.settings.listeners.ChatListener;
import com.primesmp.settings.listeners.MobSpawnListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrimeSMPSettings extends JavaPlugin {

    private PlayerDataManager dataManager;

    @Override
    public void onEnable() {
        this.dataManager = new PlayerDataManager(this);
        dataManager.load();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);

        SettingsCommand settingsCommand = new SettingsCommand(this);
        getCommand("primesettings").setExecutor(settingsCommand);
        getCommand("primesettings").setTabCompleter(settingsCommand);

        getLogger().info("PrimeSMPSettings enabled - per-player chat & mob spawn controls active.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }
}
