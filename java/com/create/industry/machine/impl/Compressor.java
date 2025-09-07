package com.create.industry.machine.impl;

import com.create.industry.machine.ProcessingMachine;
import org.bukkit.Location;

public class Compressor extends ProcessingMachine {
    public Compressor(Location loc, int level) { super(loc, "COMPRESSOR", level); }
    @Override protected String recipeKey() { return "COMPRESSOR"; }
    @Override protected String guiTitle() { return "壓縮機"; }
}
