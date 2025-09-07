package com.create.industry.manager;

import com.create.industry.IndustryPlugin;
import com.create.industry.config.MachinesConfig;
import com.create.industry.machine.Machine;
import com.create.industry.machine.impl.*;
import com.create.industry.util.WireTier;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class MachineRegistry {

    private final IndustryPlugin plugin;
    private final MachinesConfig cfg;

    // 位置 → 機器
    private final Map<Location, Machine> machines = new HashMap<>();
    // GUI Inventory → 機器（避免內存洩漏用 WeakHashMap 也可，這裡保持和你原始結構一致）
    private final Map<Inventory, Machine> invMap = new WeakHashMap<>();

    public MachineRegistry(IndustryPlugin plugin, MachinesConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    /* =========================================================
       工廠 + 註冊：Place 事件建機器時建議呼叫這個
       ========================================================= */
    /** 依 ID 建立實例並註冊，回傳建立好的 Machine（找不到 ID 則回傳 null）。 */
    public Machine createAndRegister(String id, Location loc, Integer tier) {
        if (id == null || loc == null) return null;
        int lvl = (tier == null || tier < 1) ? 1 : tier;
        String key = id.toUpperCase();

        Machine m = createById(key, loc, lvl);
        if (m == null) {
            IndustryPlugin.inst().getLogger().warning("[MachineRegistry] Unknown machine id: " + key + " @ " + loc);
            return null;
        }
        register(m);
        return m;
    }

    /** 內部：把 ID 映射到實際類別；需要更多機器就照樣加 case。 */
    private Machine createById(String id, Location loc, int tier) {
        return switch (id) {
            case "FUEL_GEN"       -> new FuelGenerator(loc, tier);
            case "AUTO_MINER"     -> new AutoMiner(loc, tier);
            case "MAT_CONVERTER"  -> new MatterConverter(loc, tier);
            case "BATTERY_BASIC"  -> new BatteryBlock(loc, "BATTERY_BASIC", 1);
            case "BATTERY_ADV", "BATTERY_ADVANCED"
                                 -> new BatteryBlock(loc, "BATTERY_ADV", 2);
            case "SOLAR_GEN"      -> new SolarGenerator(loc, tier);
            case "WIND_GEN"       -> new WindGenerator(loc, tier);
            case "HYDRO_GEN"      -> new HydroGenerator(loc, tier);
            case "MACERATOR"      -> new Macerator(loc, tier);
            case "COMPRESSOR"     -> new Compressor(loc, tier);
            case "ASSEMBLER"      -> new Assembler(loc, tier);

            // === 導線（三種等級） ===
            case "WIRE_LV"        -> new WireBlock(loc, "WIRE_LV", 1, WireTier.LV);
            case "WIRE_MV"        -> new WireBlock(loc, "WIRE_MV", 1, WireTier.MV);
            case "WIRE_HV"        -> new WireBlock(loc, "WIRE_HV", 1, WireTier.HV);
            default               -> null;
        };
    }

    /** 實際把機器塞進索引，並且把該座標登記為能量節點；導線額外註冊到電網 */
    public void register(Machine m) {
        if (m == null) return;
        Location key = m.location().toBlockLocation();
        machines.put(key, m);

        // 能源節點：讓 pull/offer 針對這格生效
        IndustryPlugin.inst().energy().registerNode(key);

        // 若是導線，順便註冊 wire
        if (m instanceof WireBlock wb) {
            IndustryPlugin.inst().energy().registerWire(key, wb.wireTier());
        }
    }

    /* =========================================================
       舊有 API 兼容（你之前的程式碼可以繼續用）
       ========================================================= */
    /** 舊 API：依 loc+id+tier 註冊（內部改呼叫 createAndRegister） */
    public void registerMachine(Location loc, String id, int tier) {
        createAndRegister(id, loc, tier);
    }

    /** 取得 / 移除 / 全部 */
    public Machine get(Location loc) { return machines.get(loc.toBlockLocation()); }
    public Collection<Machine> all() { return machines.values(); }

    /** 新版：remove；舊名：unregister（兩者都保留） */
    public void remove(Location loc) {
        Location key = loc.toBlockLocation();
        Machine m = machines.remove(key);
        if (m instanceof WireBlock) {
            // 導線從電網移除
            IndustryPlugin.inst().energy().unregisterWire(key);
        }
        // 其餘是否要從 energy.nodes 移除可視需求決定；通常保留 buffer 較安全
    }
    public void unregister(Location loc) { remove(loc); }

    /* =========================================================
       GUI Inventory 的綁定/查詢
       ========================================================= */
    public void bindInventory(Inventory inv, Machine m){
        if (inv != null && m != null) invMap.put(inv, m);
    }
    public Machine fromInventory(Inventory inv){ return invMap.get(inv); }
}
