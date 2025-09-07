package com.create.industry.listeners;

import com.create.industry.IndustryPlugin;
import com.create.industry.manager.MachineRegistry;
import com.create.industry.machine.Machine;
import com.create.industry.machine.impl.WireBlock;
import com.create.industry.util.WireTier;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlaceBreakListener implements Listener {
    private final MachineRegistry reg;

    public PlaceBreakListener(MachineRegistry reg) { this.reg = reg; }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack inHand = e.getItemInHand();
        if (inHand == null) return;
        ItemMeta meta = inHand.getItemMeta();
        if (meta == null) return;

        // 從 PDC 讀機器 ID / 等級
        var idKey   = new NamespacedKey("industry", "machine_id");
        var tierKey = new NamespacedKey("industry", "tier");
        String id = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        Integer tier = meta.getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        if (id == null || id.isEmpty()) return;    // 不是本插件機器
        if (tier == null || tier <= 0) tier = 1;

        // 生成並註冊機器——這裡呼叫 MachineRegistry 的工廠方法
        // ★★ 請把這行替換成你專案中「真正建立機器」的方法 ★★
        // 例如：reg.createAndRegister(id, e.getBlockPlaced().getLocation(), tier);
        Machine m = reg.createAndRegister(id, e.getBlockPlaced().getLocation(), tier);

        // 導線額外註冊到電網（如果這個機器是 WireBlock）
        if (m instanceof WireBlock wb) {
            IndustryPlugin.inst().energy().registerWire(e.getBlockPlaced().getLocation(), wb.wireTier());
        }

        // 無論何種機器，都把座標列為 energy node（電能可在此拉/推）
        IndustryPlugin.inst().energy().registerNode(e.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        var loc = e.getBlock().getLocation();
        Machine m = reg.get(loc);
        if (m == null) return;

        // 導線從電網移除
        if (m instanceof WireBlock) {
            IndustryPlugin.inst().energy().unregisterWire(loc);
        }

        // 從機器註冊表移除（並讓機器有機會做清理）
        m.onBreak(e.getPlayer());
        reg.remove(loc);
    }
}
