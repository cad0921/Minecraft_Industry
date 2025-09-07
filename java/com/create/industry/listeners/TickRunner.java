package com.create.industry.listeners;

import com.create.industry.manager.EnergyNetwork;
import com.create.industry.manager.MachineRegistry;

public class TickRunner implements Runnable {
    private final MachineRegistry reg;
    private final EnergyNetwork net;
    public TickRunner(MachineRegistry reg, EnergyNetwork net) { this.reg = reg; this.net = net; }

    @Override public void run() { reg.all().forEach(m -> m.onTick()); }
}
