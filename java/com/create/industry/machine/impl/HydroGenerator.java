package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import com.create.industry.util.WorldUtil;
import org.bukkit.Location;

public class HydroGenerator extends AbstractGenerator {
    public HydroGenerator(Location loc, int lvl) { super(loc, "HYDRO_GEN", lvl); }
    @Override public void onTick() {
        int faces = IndustryPlugin.inst().machinesConfig().hydroSampleFaces();
        int flow = WorldUtil.countAdjacentWater(loc.getBlock(), faces);
        int out = Math.min(IndustryPlugin.inst().machinesConfig().hydroMaxOutput(),
                IndustryPlugin.inst().machinesConfig().hydroBaseOutput() + flow*10);
        IndustryPlugin.inst().energy().offerEnergy(loc, out);
        lastOutput = out;
    }
}
