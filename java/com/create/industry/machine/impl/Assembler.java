package com.create.industry.machine.impl;

import com.create.industry.machine.ProcessingMachine;
import org.bukkit.Location;

public class Assembler extends ProcessingMachine {
    public Assembler(Location loc, int level) { super(loc, "ASSEMBLER", level); }
    @Override protected String recipeKey() { return "ASSEMBLER"; }
    @Override protected String guiTitle() { return "自動合成機"; }
}
