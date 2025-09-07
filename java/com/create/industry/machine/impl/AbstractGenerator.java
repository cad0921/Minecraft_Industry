package com.create.industry.machine.impl;

import com.create.industry.machine.AbstractMachine;
import org.bukkit.Location;

public abstract class AbstractGenerator extends AbstractMachine {
    protected int lastOutput = 0;
    protected AbstractGenerator(Location loc, String id, int level) { super(loc, id, level); }
    public int getLastOutput() { return lastOutput; }
}