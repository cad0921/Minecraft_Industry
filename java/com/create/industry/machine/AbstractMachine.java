package com.create.industry.machine;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

public abstract class AbstractMachine implements Machine {
    protected final Location loc;
    protected final String id;
    protected int level;
    protected boolean paused = false;

    protected AbstractMachine(Location loc, String id, int level) {
        this.loc = loc.toBlockLocation();
        this.id = id;
        this.level = level;
    }

    public boolean isPaused() { return paused; }
    public void setPaused(boolean p) { this.paused = p; }
    public void togglePaused() { this.paused = !this.paused; }

    @Override public Location location() { return loc; }
    @Override public String id() { return id; }
    @Override public int tier() { return level; }

    public Block block() { return loc.getBlock(); }

    @Override public void onPlace(Player placer) {}
    @Override public void onBreak(Player breaker) {}
}