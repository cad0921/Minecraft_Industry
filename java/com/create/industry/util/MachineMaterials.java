package com.create.industry.util;

import org.bukkit.Material;
import java.util.*;

/**
 * 機器 ID → 顯示用的原版方塊對照。
 * - /industry give 時用它決定物品外觀
 * - 破壞掉落時也用它決定掉落物外觀
 * - Tab 補全可用 allIds() 取得清單
 */
public final class MachineMaterials {
    private MachineMaterials() {}

    private static final Map<String, Material> MAP = new HashMap<>();
    private static final List<String> IDS = new ArrayList<>();

    static {
        // === 發電 ===
        put("FUEL_GEN",      Material.FURNACE);              // 燃料發電：熔爐
        put("SOLAR_GEN",     Material.DAYLIGHT_DETECTOR);    // 太陽能：日光感測器
        put("WIND_GEN",      Material.GRINDSTONE);           // 風力：磨石（或可改為 BANNER）
        put("HYDRO_GEN",     Material.CAULDRON);             // 水力：與你的配方一致（RecipeRegistry 用 CAULDRON）

        // === 儲能 ===
        put("BATTERY_BASIC", Material.ENDER_CHEST);
        put("BATTERY_ADV",   Material.SHULKER_BOX);
        put("BATTERY_ELITE", Material.NETHERITE_BLOCK);      // 若未用到可忽略

        // === 加工 ===
        put("MACERATOR",     Material.STONECUTTER);          // 粉碎機
        put("COMPRESSOR",    Material.ANVIL);                // 壓縮機
        put("ASSEMBLER",     Material.CARTOGRAPHY_TABLE);    // 自動合成機

        // === 物流/倉儲 ===
        put("CONVEYOR",      Material.RAIL);
        put("ITEM_PIPE",     Material.HOPPER);
        put("STORAGE_CRATE", Material.BARREL);

        // === 液體/環境 ===
        put("PUMP",          Material.BREWING_STAND);
        put("COOLING_TOWER", Material.IRON_BLOCK);

        // === 採礦/轉換 ===
        put("AUTO_MINER",    Material.PISTON);               // 或 DISPENSER
        put("MAT_CONVERTER", Material.OBSIDIAN);             // 或 COBBLESTONE_WALL
    }

    private static void put(String id, Material mat) {
        MAP.put(id, mat);
        IDS.add(id);
    }

    /** 依 ID 取得外觀方塊；未知 ID 回退 FURNACE。 */
    public static Material displayOf(String id) {
        if (id == null) return Material.FURNACE;
        return MAP.getOrDefault(id.toUpperCase(), Material.FURNACE);
    }

    /** 提供 Tab 補全使用的 ID 清單。 */
    public static List<String> allIds() {
        return Collections.unmodifiableList(IDS);
    }
}
