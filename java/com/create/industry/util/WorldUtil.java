package com.create.industry.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WorldUtil {
    private static final BlockFace[] FACES = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
    public static int countAdjacentWater(Block b, int maxFaces) {
        int c=0; for (int i=0;i<Math.min(FACES.length,maxFaces);i++) {
            Block n = b.getRelative(FACES[i]);
            if (n.getType() == Material.WATER) c++;
        } return c;
    }
}
