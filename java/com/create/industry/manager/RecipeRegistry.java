package com.create.industry.manager;

import com.create.industry.IndustryPlugin;

import com.create.industry.config.RecipesConfig;
import com.create.industry.data.ProcessingRecipe;
import com.create.industry.util.ItemUtil;
import com.create.industry.util.PdcUtil;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeRegistry {
    private final RecipesConfig cfg;
    private final PdcUtil pdc;
    private final java.util.List<NamespacedKey> registeredKeys = new java.util.ArrayList<>();

    public RecipeRegistry(RecipesConfig cfg) {
        this.cfg = cfg;
        this.pdc = new PdcUtil(IndustryPlugin.inst());
    }
    public void reload() {
        // 1) 先移除舊的自訂配方
        for (NamespacedKey key : registeredKeys) {
            Bukkit.removeRecipe(key);
        }
        registeredKeys.clear();

        // 2) 重新讀取 recipes.yml
        cfg.reload();

        // 3) 重新註冊所有合成/
        registerCraftingRecipes();
    }

    public void registerCraftingRecipes() {
        registerMachineRecipe("FUEL_GEN", Material.FURNACE);
        registerMachineRecipe("AUTO_MINER", Material.DROPPER);
        registerMachineRecipe("MAT_CONVERTER", Material.OBSIDIAN);
        registerMachineRecipe("MACERATOR", Material.STONECUTTER);
        registerMachineRecipe("COMPRESSOR", Material.ANVIL);
        registerMachineRecipe("ASSEMBLER", Material.CARTOGRAPHY_TABLE);
        registerMachineRecipe("SOLAR_GEN", Material.DAYLIGHT_DETECTOR);
        registerMachineRecipe("WIND_GEN", Material.GRINDSTONE);
        registerMachineRecipe("HYDRO_GEN", Material.CAULDRON);
    }
    public List<ProcessingRecipe> getProcessing(String machineKey) {
        return cfg.getProcessing(machineKey);
    }
    private void registerMachineRecipe(String id, Material display) {
        ItemStack result = ItemUtil.named(display, id);
        pdc.tagMachine(result, id, 1);
        NamespacedKey key = new NamespacedKey(IndustryPlugin.inst(), "mc_"+id.toLowerCase());

        ShapedRecipe r = new ShapedRecipe(key, result);
        r.shape("III", "IFI", "IRI");
        r.setIngredient('I', Material.IRON_INGOT);
        r.setIngredient('F', display);
        r.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(r);
        registeredKeys.add(key);

    }
}
