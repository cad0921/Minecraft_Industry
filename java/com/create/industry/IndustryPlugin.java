package com.create.industry;

import com.create.industry.listeners.*;
import com.create.industry.manager.*;
import com.create.industry.config.*;
import com.create.industry.commands.IndustryCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class IndustryPlugin extends JavaPlugin {
    private static IndustryPlugin INSTANCE;

    private MachinesConfig machinesConfig;
    private RecipesConfig recipesConfig;

    private MachineRegistry machineRegistry;
    private EnergyNetwork energyNetwork;
    private RecipeRegistry recipeRegistry;
    private PersistManager persistManager;

    public static IndustryPlugin inst() { return INSTANCE; }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // 初次釋出資源
        saveResource("machines.yml", false);
        saveResource("recipes.yml", false);
        saveResource("messages_zh_tw.yml", false);

        // 載入設定
        machinesConfig = new MachinesConfig(this);
        recipesConfig  = new RecipesConfig(this);

        // 建構核心服務
        recipeRegistry = new RecipeRegistry(recipesConfig);
        machineRegistry = new MachineRegistry(this, machinesConfig);
        energyNetwork = new EnergyNetwork(machinesConfig);
        persistManager = new PersistManager(this);

        // 註冊合成配方
        recipeRegistry.registerCraftingRecipes();

        // 指令 + Tab 補全：同一個實例
        IndustryCommand cmd = new IndustryCommand(this);
        PluginCommand pcmd = getCommand("industry");
        if (pcmd != null) {
            pcmd.setExecutor(cmd);
            pcmd.setTabCompleter(cmd);
        } else {
            getLogger().warning("Command 'industry' not found. Check plugin.yml.");
        }

        // 事件監聽
        getServer().getPluginManager().registerEvents(new PlaceBreakListener(machineRegistry), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);

        // 核心 tick 迴圈
        Bukkit.getScheduler().runTaskTimer(this, new TickRunner(machineRegistry, energyNetwork), 1L, 1L);

        getLogger().info("IndustryPlugin v0.4 enabled.");
    }

    @Override
    public void onDisable() {
        persistManager.flushAll(machineRegistry);
        getLogger().info("IndustryPlugin v0.4 disabled.");
    }

    // === 對外存取器 ===
    public MachineRegistry machines() { return machineRegistry; }
    public EnergyNetwork energy() { return energyNetwork; }
    public RecipeRegistry recipes() { return recipeRegistry; }
    public MachinesConfig machinesConfig() { return machinesConfig; }
    public RecipesConfig recipesConfig() { return recipesConfig; } // 新增：有時需要直接讀取 processing 配方
    
    
}
