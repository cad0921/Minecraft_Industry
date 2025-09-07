package com.create.industry.config;

import com.create.industry.data.ProcessingRecipe;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class RecipesConfig {
    private final Plugin plugin;
    private YamlConfiguration cfg;
    private final Map<String, List<ProcessingRecipe>> processing = new HashMap<>();

    public RecipesConfig(Plugin plugin) { this.plugin = plugin; reload(); }
    public void reload() {
        File f = new File(plugin.getDataFolder(), "recipes.yml");
        this.cfg = YamlConfiguration.loadConfiguration(f);
        loadProcessing();
    }

    private void loadProcessing() {
        processing.clear();
        var root = cfg.getConfigurationSection("processing");
        if (root == null) return;
        for (String mach : root.getKeys(false)) {
            List<ProcessingRecipe> list = new ArrayList<>();
            var arr = root.getList(mach);
            if (arr == null) continue;
            for (Object o : arr) {
                if (!(o instanceof Map<?,?> m)) continue;
                String in = (String) m.get("in");
                String out = (String) m.get("out");
                int fe = parseInt(m.get("fe"), 100);
                int time = parseInt(m.get("time"), 100);
                if (in == null || out == null) continue;
                var inPair = parseItemSpec(in);
                var outPair = parseItemSpec(out);
                if (inPair == null || outPair == null) continue;
                list.add(new ProcessingRecipe(inPair.mat, inPair.cnt, outPair.mat, outPair.cnt, fe, time));
            }
            processing.put(mach.toUpperCase(), list);
        }
    }

    private static class Spec { Material mat; int cnt; }
    private Spec parseItemSpec(String s) {
        String[] parts = s.trim().split("\s+x");
        Spec sp = new Spec();
        sp.mat = Material.matchMaterial(parts[0].trim());
        sp.cnt = (parts.length>1) ? Integer.parseInt(parts[1].trim()) : 1;
        if (sp.mat == null) return null; return sp;
    }
    private int parseInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return def; }
    }

    public List<ProcessingRecipe> getProcessing(String key) {
        return processing.getOrDefault(key.toUpperCase(), Collections.emptyList());
    }
}
