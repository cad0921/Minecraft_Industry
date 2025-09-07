package com.create.industry.util;

import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;

public class BlockFaceUtil {
    public static org.bukkit.block.BlockFace getFacing(Block b) {
        if (b.getBlockData() instanceof Directional d) return d.getFacing();
        return org.bukkit.block.BlockFace.NORTH;
    }
}
