package com.craftserver.crateplugin.model;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Crate {

    private final String id;
    private Location location; // may be null until bound with /crate setblock
    private Material blockMaterial = Material.CHEST;
    private String keyId;
    private final List<Reward> rewards = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public Crate(String id) {
        this.id = id;
        this.keyId = id + "_key";
    }

    public String getId() { return id; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public Material getBlockMaterial() { return blockMaterial; }
    public void setBlockMaterial(Material blockMaterial) { this.blockMaterial = blockMaterial; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public List<Reward> getRewards() { return rewards; }

    public void addReward(Reward reward) { rewards.add(reward); }

    /** Weighted random pick from this crate's reward pool. */
    public Reward rollReward() {
        if (rewards.isEmpty()) return null;
        int totalWeight = rewards.stream().mapToInt(Reward::getWeight).sum();
        if (totalWeight <= 0) return rewards.get(RANDOM.nextInt(rewards.size()));

        int roll = RANDOM.nextInt(totalWeight);
        int cumulative = 0;
        for (Reward r : rewards) {
            cumulative += r.getWeight();
            if (roll < cumulative) return r;
        }
        return rewards.get(rewards.size() - 1);
    }
}
