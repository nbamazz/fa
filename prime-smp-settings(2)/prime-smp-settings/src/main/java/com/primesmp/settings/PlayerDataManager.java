package com.primesmp.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stores which players have chat and/or mob spawning disabled for
 * themselves. Backed by a simple data.yml in the plugin's data folder so
 * settings survive restarts.
 */
public class PlayerDataManager {

    private final PrimeSMPSettings plugin;
    private final File dataFile;
    private final Map<UUID, Boolean> chatDisabled = new HashMap<>();
    private final Map<UUID, Boolean> mobSpawnDisabled = new HashMap<>();

    public PlayerDataManager(PrimeSMPSettings plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!dataFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (String key : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean chat = config.getBoolean("players." + key + ".chatDisabled", false);
                boolean mobs = config.getBoolean("players." + key + ".mobSpawnDisabled", false);
                if (chat) {
                    chatDisabled.put(uuid, true);
                }
                if (mobs) {
                    mobSpawnDisabled.put(uuid, true);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed entries rather than failing to load entirely.
            }
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();

        Set<UUID> allKnown = new HashSet<>();
        allKnown.addAll(chatDisabled.keySet());
        allKnown.addAll(mobSpawnDisabled.keySet());

        for (UUID uuid : allKnown) {
            boolean chat = chatDisabled.getOrDefault(uuid, false);
            boolean mobs = mobSpawnDisabled.getOrDefault(uuid, false);
            if (chat) {
                config.set("players." + uuid + ".chatDisabled", true);
            }
            if (mobs) {
                config.set("players." + uuid + ".mobSpawnDisabled", true);
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save PrimeSMPSettings player data", e);
        }
    }

    public boolean isChatDisabled(UUID uuid) {
        return chatDisabled.getOrDefault(uuid, false);
    }

    public boolean isMobSpawnDisabled(UUID uuid) {
        return mobSpawnDisabled.getOrDefault(uuid, false);
    }

    public void setChatDisabled(UUID uuid, boolean value) {
        if (value) {
            chatDisabled.put(uuid, true);
        } else {
            chatDisabled.remove(uuid);
        }
        save();
    }

    public void setMobSpawnDisabled(UUID uuid, boolean value) {
        if (value) {
            mobSpawnDisabled.put(uuid, true);
        } else {
            mobSpawnDisabled.remove(uuid);
        }
        save();
    }
}
