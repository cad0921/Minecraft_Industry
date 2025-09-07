package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import com.create.industry.machine.AbstractMachine;
import com.create.industry.util.WireTier;
import org.bukkit.Location;

public class WireBlock extends AbstractMachine {
    private final WireTier wireTier; // ← 重新命名，避免和 int tier() 混淆

    public WireBlock(Location loc, String id, int level, WireTier tier) {
        super(loc, id, level);
        this.wireTier = tier;
        IndustryPlugin.inst().energy().registerWire(loc, wireTier);
        IndustryPlugin.inst().energy().registerNode(loc); // 讓此格成為節點，用於中繼
    }

    @Override
    public void onPlace(org.bukkit.entity.Player placer) {
        IndustryPlugin.inst().energy().registerWire(loc, wireTier);
        IndustryPlugin.inst().energy().registerNode(loc);
    }

    @Override
    public void onBreak(org.bukkit.entity.Player breaker) {
        IndustryPlugin.inst().energy().unregisterWire(loc);
        // 保留 node/buffer 由 EnergyNetwork 自行處理
    }

    @Override
    public void onTick() {
        // 導線不做事，傳輸在 EnergyNetwork.tick() 統一處理
    }

    /** 回傳導線等級（LV/MV/HV），不要覆寫 AbstractMachine.tier() */
    public WireTier wireTier() { return wireTier; }
}
