package com.create.industry.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class MachinesConfig {
    private final Plugin plugin;
    private FileConfiguration cfg;

    public record MatterStage(Material from, Material to, int baseTimeSec, int energyPerConvert, int levelRequired) {}

    private final Map<Material, MatterStage> matterStages = new HashMap<>();
    private final Set<Material> mineWhitelist = EnumSet.noneOf(Material.class);

    public MachinesConfig(Plugin plugin) {
        this.plugin = plugin; reload();
    }
    public void reload() {
        File f = new File(plugin.getDataFolder(), "machines.yml");
        this.cfg = YamlConfiguration.loadConfiguration(f);
        loadMatter();
        loadWhitelist();
    }

    private void loadMatter() {
        matterStages.clear();
        var sec = cfg.getConfigurationSection("matter_converter.stages");
        if (sec == null) return;
        for (String k : sec.getKeys(false)) {
            var from = Material.matchMaterial(sec.getString(k + ".from", "STONE"));
            var to   = Material.matchMaterial(sec.getString(k + ".to", "STONE"));
            int time = parseSec(sec.getString(k + ".time", "60s"));
            int cost = sec.getInt(k + ".cost", 100);
            int lvl  = sec.getInt(k + ".level", 1);
            if (from != null && to != null)
                matterStages.put(from, new MatterStage(from, to, time, cost, lvl));
        }
    }
    private void loadWhitelist() {
        mineWhitelist.clear();
        for (String s : cfg.getStringList("protection.break_whitelist")) {
            var m = Material.matchMaterial(s);
            if (m != null) mineWhitelist.add(m);
        }
    }
    private int parseSec(String s) { return Integer.parseInt(s.replace("s", "")); }

    // Fuel
    public int fuelCapacity() { return cfg.getInt("generators.fuel.capacity", 20000); }
    public int fuelOutputPerTick() { return cfg.getInt("generators.fuel.output_per_tick", 120); }
    public int fuelFeValue(Material m) { return cfg.getInt("generators.fuel.fuels." + m.name() + ".fe", 0); }

    // Miner
    public boolean canMine(Material m) { return mineWhitelist.contains(m); }
    public int minerCostPerBlock() { return cfg.getInt("miner.base_cost_per_block", 80); }

    // Matter
    public Map<Material, MatterStage> matterStages() { return matterStages; }
    public double levelSpeedMultiplier(int level) {
        return switch (level) { case 3 -> 0.6; case 2 -> 0.8; default -> 1.0; };
    }

    // Solar/Wind/Hydro
    public int solarBaseOutput(boolean day, boolean storm) {
        int base = cfg.getInt("generators.solar.base_output", 40);
        if (!day) return (int)(base * cfg.getDouble("generators.solar.night_multiplier", 0.0));
        if (storm) return (int)(base * cfg.getDouble("generators.solar.rain_multiplier", 0.5));
        return base;
    }
    public int windOutput(int y, boolean storm) {
        int base = cfg.getInt("generators.wind.base_output", 20);
        double per = cfg.getDouble("generators.wind.per_height", 0.5);
        int minY = cfg.getInt("generators.wind.min_y", 64);
        int max = cfg.getInt("generators.wind.max_output", 120);
        double out = base + Math.max(0, y - minY) * per;
        if (storm) out *= cfg.getDouble("generators.wind.storm_bonus", 1.2);
        return (int)Math.min(max, Math.round(out));
    }
    public int hydroBaseOutput(){ return cfg.getInt("generators.hydro.base_output", 30);} 
    public int hydroMaxOutput(){ return cfg.getInt("generators.hydro.max_output", 180);} 
    public int hydroSampleFaces(){ return cfg.getInt("generators.hydro.sample_faces", 6);} 
}
