package com.create.industry.machine;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Machine {
    Location location();
    String id();
    int tier();

    void onTick();
    void onPlace(Player placer);
    void onBreak(Player breaker);

    default Block block() { return location().getBlock(); }
}
