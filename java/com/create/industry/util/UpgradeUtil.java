package com.create.industry.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;

public final class UpgradeUtil {
    private UpgradeUtil(){}

    public static String idOf(ItemStack is) {
        if (is == null) return null;
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return null;
        try {
            PersistentDataContainer c = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("industry", "item_id");
            if (c.has(key, PersistentDataType.STRING)) {
                String v = c.get(key, PersistentDataType.STRING);
                if (v != null && !v.isEmpty()) return v.toUpperCase();
            }
        } catch (Throwable ignored){}
        if (meta.hasDisplayName()) {
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name != null) return name.trim().toUpperCase();
        }
        return null;
    }

    public static boolean isRouter(ItemStack is) {
        String id = idOf(is);
        return "UPG_MINER_ROUTER".equals(id) || "ROUTER".equals(id) || "UPGRADE_ROUTER".equals(id);
    }
    public static int speedLevel(ItemStack is) {
        String id = idOf(is);
        if (id == null) return 0;
        if (id.contains("UPG_MINER_SPEED_III") || id.endsWith("SPEED_3") || id.endsWith("SPEED III")) return 3;
        if (id.contains("UPG_MINER_SPEED_II")  || id.endsWith("SPEED_2") || id.endsWith("SPEED II"))  return 2;
        if (id.contains("UPG_MINER_SPEED_I")   || id.endsWith("SPEED_1") || id.endsWith("SPEED I"))   return 1;
        if (id.contains("UPG_MINER_SPEED")) return 1;
        return 0;
    }
    public static boolean isSilk(ItemStack is) {
        String id = idOf(is);
        return id != null && (id.contains("UPG_MINER_SILK") || id.equals("SILK"));
    }
    public static boolean isSmelt(ItemStack is) {
        String id = idOf(is);
        return id != null && (id.contains("UPG_MINER_SMELT") || id.equals("SMELT"));
    }
}