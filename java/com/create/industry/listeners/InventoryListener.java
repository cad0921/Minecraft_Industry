package com.create.industry.listeners;

import com.create.industry.IndustryPlugin;
import com.create.industry.gui.GuiService;
import com.create.industry.machine.Machine;
import com.create.industry.machine.ProcessingMachine;
import com.create.industry.machine.impl.*;
import com.create.industry.util.UpgradeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class InventoryListener implements Listener {
    private static final Set<Integer> MINER_STORAGE = new HashSet<>();
    static {
        for (int s : AutoMiner.STORAGE_SLOTS) MINER_STORAGE.add(s);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        Machine m = IndustryPlugin.inst().machines().fromInventory(inv);
        if (m == null) return;

        int slot = e.getRawSlot();
        if (slot >= inv.getSize()) return; // clicks in player inv ok

        // Buttons (all machines): right -> pause/resume
        if (slot == GuiService.SLOT_BTN_RIGHT) {
            e.setCancelled(true);
            if (m instanceof com.create.industry.machine.AbstractMachine am) {
                am.togglePaused();
                if (m instanceof ProcessingMachine pm) new GuiService().openProcessing((Player)e.getWhoClicked(), pm);
                else if (m instanceof MatterConverter mc) new GuiService().openMatterConverter((Player)e.getWhoClicked(), mc);
                else if (m instanceof BatteryBlock bb) new GuiService().openBattery((Player)e.getWhoClicked(), bb);
                else if (m instanceof SolarGenerator sg) new GuiService().openSolar((Player)e.getWhoClicked(), sg);
                else if (m instanceof WindGenerator wg) new GuiService().openWind((Player)e.getWhoClicked(), wg);
                else if (m instanceof HydroGenerator hg) new GuiService().openHydro((Player)e.getWhoClicked(), hg);
                else if (m instanceof AutoMiner amr) new GuiService().openMiner((Player)e.getWhoClicked(), amr);
            }
            return;
        }

        // Processing machines: allow input(11)/output(15)
        if (m instanceof ProcessingMachine) {
            if (slot == 11 || slot == 15) { /* allow */ }
            else e.setCancelled(true);
            return;
        }

        // Battery: left button cycles IO mode; otherwise read-only
        if (m instanceof BatteryBlock) {
            if (slot == GuiService.SLOT_BTN_LEFT) {
                e.setCancelled(true);
                ((BatteryBlock)m).cycleMode();
                new GuiService().openBattery((Player)e.getWhoClicked(), (BatteryBlock)m);
                return;
            }
            e.setCancelled(true);
            return;
        }

        // Generators / MatterConverter: read-only
        if (m instanceof AbstractGenerator || m instanceof MatterConverter) {
            e.setCancelled(true);
            return;
        }

        // AutoMiner custom
        if (m instanceof AutoMiner miner) {
            if (slot == GuiService.SLOT_BTN_LEFT) {
                e.setCancelled(true);
                // only cycle if router upgrade installed
                ItemStack upg = inv.getItem(AutoMiner.SLOT_UPG_ROUTER);
                if (UpgradeUtil.isRouter(upg)) miner.cycleRouterDir();
                new GuiService().openMiner((Player)e.getWhoClicked(), miner);
                return;
            }

            // Upgrade slots validation
            if (slot == AutoMiner.SLOT_UPG_SILK || slot == AutoMiner.SLOT_UPG_SMELT ||
                slot == AutoMiner.SLOT_UPG_ROUTER || slot == AutoMiner.SLOT_UPG_SPEED) {

                ItemStack cursor = e.getCursor();
                if (slot == AutoMiner.SLOT_UPG_ROUTER && cursor != null && !UpgradeUtil.isRouter(cursor)) { e.setCancelled(true); return; }
                if (slot == AutoMiner.SLOT_UPG_SPEED  && cursor != null && UpgradeUtil.speedLevel(cursor) == 0) { e.setCancelled(true); return; }
                if (slot == AutoMiner.SLOT_UPG_SILK   && cursor != null && !UpgradeUtil.isSilk(cursor)) { e.setCancelled(true); return; }
                if (slot == AutoMiner.SLOT_UPG_SMELT  && cursor != null && !UpgradeUtil.isSmelt(cursor)) { e.setCancelled(true); return; }
                // mutual exclusion silk vs smelt
                if (slot == AutoMiner.SLOT_UPG_SILK && inv.getItem(AutoMiner.SLOT_UPG_SMELT) != null &&
                        inv.getItem(AutoMiner.SLOT_UPG_SMELT).getType() != Material.AIR) { e.setCancelled(true); return; }
                if (slot == AutoMiner.SLOT_UPG_SMELT && inv.getItem(AutoMiner.SLOT_UPG_SILK) != null &&
                        inv.getItem(AutoMiner.SLOT_UPG_SILK).getType() != Material.AIR) { e.setCancelled(true); return; }
                return; // allow place/remove
            }

            // storage slots allowed
            if (MINER_STORAGE.contains(slot)) return;

            // others read-only
            e.setCancelled(true);
            return;
        }

        // default read-only
        e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) { /* no-op */ }
}