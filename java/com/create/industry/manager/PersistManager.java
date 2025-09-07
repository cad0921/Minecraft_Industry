package com.create.industry.manager;

import org.bukkit.plugin.Plugin;

public class PersistManager {
    private final Plugin plugin;
    public PersistManager(Plugin plugin){ this.plugin = plugin; }
    public void flushAll(MachineRegistry reg) {
        // TODO: serialize machines & inventories to disk
    }
}
