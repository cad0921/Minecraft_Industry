package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import com.create.industry.machine.AbstractMachine;

public class BatteryBlock extends AbstractMachine {
    public enum IoMode { BOTH, INPUT_ONLY, OUTPUT_ONLY }

    private int stored;
    private IoMode mode = IoMode.BOTH;

    public BatteryBlock(org.bukkit.Location loc, String id, int tier) {
        super(loc, id, tier);
        stored = 0;
    }

    @Override
    public void onTick() {
        if (paused) return;  // respect pause
        int cap = getCapacity();
        int io = tier()==1 ? 400 : 1200;

        // input
        if (mode == IoMode.BOTH || mode == IoMode.INPUT_ONLY) {
            int pulled = IndustryPlugin.inst().energy().pullEnergy(loc, io);
            stored = Math.min(cap, stored + pulled);
        }

        // output (simple: only push overflow; adjust if you want active output)
        if (mode == IoMode.BOTH || mode == IoMode.OUTPUT_ONLY) {
            int out = Math.min(io, stored);
            if (out > 0) {
                int unused = IndustryPlugin.inst().energy().offerEnergy(loc, out);
                int accepted = out - unused;
                stored = Math.max(0, stored - accepted);
            }
        }
    }

    public int getStored() { return stored; }
    public int getCapacity() {
        return tier()==1 ? 100000 : (tier()==2 ? 500000 : 2000000);
    }

    public IoMode getMode() { return mode; }
    public void cycleMode() {
        switch (mode) {
            case BOTH -> mode = IoMode.INPUT_ONLY;
            case INPUT_ONLY -> mode = IoMode.OUTPUT_ONLY;
            case OUTPUT_ONLY -> mode = IoMode.BOTH;
        }
    }
}