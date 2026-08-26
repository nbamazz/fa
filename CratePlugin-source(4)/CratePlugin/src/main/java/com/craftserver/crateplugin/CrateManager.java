package com.craftserver.crateplugin;

import com.craftserver.crateplugin.model.Crate;
import com.craftserver.crateplugin.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CrateManager {

    private final CratePlugin plugin;
    private final File file;
    private final Map<String, Crate> crates = new LinkedHashMap<>();

    public CrateManager(CratePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
        load();
    }

    public Crate getOrCreate(String id) {
        return crates.computeIfAbsent(id.toLowerCase(), Crate::new);
    }

    public Optional<Crate> get(String id) {
        return Optional.ofNullable(crates.get(id.toLowerCase()));
    }

    public boolean exists(String id) {
        return crates.containsKey(id.toLowerCase());
    }

    public void remove(String id) {
        crates.remove(id.toLowerCase());
    }

    public Map<String, Crate> all() {
        return crates;
    }

    /** Finds the crate bound to a physical block location, if any. */
    public Optional<Crate> byLocation(Location loc) {
        for (Crate c : crates.values()) {
            if (c.getLocation() != null && sameBlock(c.getLocation(), loc)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    private boolean sameBlock(Location a, Location b) {
        return a.getWorld() != null && a.getWorld().equals(b.getWorld())
                && a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ();
    }

    public void load() {
        crates.clear();
        if (!file.exists()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection cratesSec = yml.getConfigurationSection("crates");
        if (cratesSec == null) return;

        for (String id : cratesSec.getKeys(false)) {
            ConfigurationSection sec = cratesSec.getConfigurationSection(id);
            if (sec == null) continue;
            Crate crate = new Crate(id);

            Material mat = Material.matchMaterial(sec.getString("material", "CHEST"));
            crate.setBlockMaterial(mat != null ? mat : Material.CHEST);
            crate.setKeyId(sec.getString("key", id + "_key"));

            if (sec.contains("world")) {
                World world = Bukkit.getWorld(sec.getString("world"));
                if (world != null) {
                    Location loc = new Location(world, sec.getInt("x"), sec.getInt("y"), sec.getInt("z"));
                    crate.setLocation(loc);
                }
            }

            ConfigurationSection rewardsSec = sec.getConfigurationSection("rewards");
            if (rewardsSec != null) {
                for (String key : rewardsSec.getKeys(false)) {
                    ConfigurationSection rSec = rewardsSec.getConfigurationSection(key);
                    if (rSec != null) crate.addReward(Reward.fromConfig(rSec));
                }
            }

            crates.put(id, crate);
        }
    }

    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        for (Crate crate : crates.values()) {
            String path = "crates." + crate.getId();
            yml.set(path + ".material", crate.getBlockMaterial().name());
            yml.set(path + ".key", crate.getKeyId());

            Location loc = crate.getLocation();
            if (loc != null && loc.getWorld() != null) {
                yml.set(path + ".world", loc.getWorld().getName());
                yml.set(path + ".x", loc.getBlockX());
                yml.set(path + ".y", loc.getBlockY());
                yml.set(path + ".z", loc.getBlockZ());
            }

            int i = 0;
            for (Reward r : crate.getRewards()) {
                r.saveToConfig(yml.createSection(path + ".rewards.reward" + (i++)));
            }
        }

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save crates.yml: " + e.getMessage());
        }
    }
}
