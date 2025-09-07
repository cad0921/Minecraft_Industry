package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import org.bukkit.Location;
import org.bukkit.World;

public class SolarGenerator extends AbstractGenerator {
    public SolarGenerator(Location loc, int lvl) { super(loc, "SOLAR_GEN", lvl); }
    @Override public void onTick() {
        World w = loc.getWorld();
        boolean day = w.isDayTime();
        boolean storm = w.hasStorm();
        int base = IndustryPlugin.inst().machinesConfig().solarBaseOutput(day, storm);
        IndustryPlugin.inst().energy().offerEnergy(loc, base);
        lastOutput = base;
    }
}
