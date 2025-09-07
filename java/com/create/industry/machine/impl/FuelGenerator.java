package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

public class FuelGenerator extends AbstractGenerator {
    private int bufferFE = 0;

    public FuelGenerator(org.bukkit.Location loc, int level) { super(loc, "FUEL_GEN", level); }

    @Override
    public void onTick() {
        if (!(block().getState() instanceof Furnace furnace)) return;
        FurnaceInventory inv = furnace.getInventory();
        int capacity = IndustryPlugin.inst().machinesConfig().fuelCapacity();
        int genPerTick = IndustryPlugin.inst().machinesConfig().fuelOutputPerTick();

        if (bufferFE + genPerTick <= capacity) {
            ItemStack fuel = inv.getFuel();
            if (fuel != null && fuel.getType() != Material.AIR) {
                int fePer = IndustryPlugin.inst().machinesConfig().fuelFeValue(fuel.getType());
                if (fePer > 0) {
                    bufferFE += Math.min(genPerTick, capacity - bufferFE);
                    if (furnace.getBurnTime() % 40 == 0) {
                        fuel.setAmount(fuel.getAmount() - 1);
                        inv.setFuel(fuel.getAmount() > 0 ? fuel : null);
                    }
                    furnace.setBurnTime((short) 200);
                }
            }
        }
        IndustryPlugin.inst().energy().offerEnergy(loc, bufferFE);
        lastOutput = bufferFE;
        bufferFE = 0;
    }
}
