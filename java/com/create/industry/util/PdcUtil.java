package com.create.industry.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PdcUtil {
    private final NamespacedKey keyId;
    private final NamespacedKey keyTier;

    public PdcUtil(Plugin plugin) {
        keyId = new NamespacedKey(plugin, "machine_id");
        keyTier = new NamespacedKey(plugin, "machine_tier");
    }
    public void tagMachine(ItemStack item, String id, int tier) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyId, PersistentDataType.STRING, id);
        pdc.set(keyTier, PersistentDataType.INTEGER, tier);
        item.setItemMeta(meta);
    }
    public String readId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        var pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(keyId, PersistentDataType.STRING, null);
    }
    public int readTier(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        var pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(keyTier, PersistentDataType.INTEGER, 1);
    }
}
