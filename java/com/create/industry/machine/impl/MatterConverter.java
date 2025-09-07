package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import com.create.industry.machine.AbstractMachine;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class MatterConverter extends AbstractMachine {
    private int ticks;
    private int lastCycleTicks;

    public MatterConverter(org.bukkit.Location loc, int tier) { super(loc, "MAT_CONVERTER", tier); }

    @Override
    public void onTick() {
        Block base = block().getRelative(0, -1, 0);
        var stage = IndustryPlugin.inst().machinesConfig().matterStages().get(base.getType());
        if (stage == null || tier() < stage.levelRequired()) { ticks = 0; lastCycleTicks = 0; return; }

        if (lastCycleTicks == 0) {
            lastCycleTicks = (int) (stage.baseTimeSec()*20 * IndustryPlugin.inst().machinesConfig().levelSpeedMultiplier(tier()));
        }
        ticks++;

        if (ticks >= lastCycleTicks) {
            int pulled = IndustryPlugin.inst().energy().pullEnergy(loc, stage.energyPerConvert());
            if (pulled >= stage.energyPerConvert()) {
                base.setType(stage.to());
            }
            ticks = 0; lastCycleTicks = 0;
        }
    }

    public int progressPercent() { if (lastCycleTicks <= 0) return 0; return Math.min(100, (int)Math.floor(100.0 * ticks / lastCycleTicks)); }
    public Material currentMaterial() { return block().getRelative(0, -1, 0).getType(); }
    public Material nextMaterial() {
        var st = IndustryPlugin.inst().machinesConfig().matterStages().get(currentMaterial());
        return st == null ? Material.AIR : st.to();
    }
}
