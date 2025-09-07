package com.create.industry.machine.impl;

import com.create.industry.machine.ProcessingMachine;
import org.bukkit.Location;

public class Macerator extends ProcessingMachine {
    public Macerator(Location loc, int level) { super(loc, "MACERATOR", level); }
    @Override protected String recipeKey() { return "MACERATOR"; }
    @Override protected String guiTitle() { return "粉碎機"; }
}
