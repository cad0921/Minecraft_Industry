package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import org.bukkit.Location;

public class WindGenerator extends AbstractGenerator {
    public WindGenerator(Location loc, int lvl) { super(loc, "WIND_GEN", lvl); }
    @Override public void onTick() {
        int y = loc.getBlockY();
        int out = IndustryPlugin.inst().machinesConfig().windOutput(y, loc.getWorld().hasStorm());
        IndustryPlugin.inst().energy().offerEnergy(loc, out);
        lastOutput = out;
    }
}
