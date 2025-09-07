package com.create.industry.listeners;

import com.create.industry.IndustryPlugin;
import com.create.industry.gui.GuiService;
import com.create.industry.machine.Machine;
import com.create.industry.machine.ProcessingMachine;
import com.create.industry.machine.impl.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

public class InteractListener implements Listener {
    private final GuiService gui = new GuiService();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        // 只吃主手、右鍵方塊
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        var b = e.getClickedBlock();
        Machine m = IndustryPlugin.inst().machines().get(b.getLocation());
        if (m == null) return; // 這裡如果是 null，通常代表放置時沒有註冊 —— 請檢查上面的 PlaceBreakListener

        e.setCancelled(true);

        if (m instanceof ProcessingMachine pm) { gui.openProcessing(e.getPlayer(), pm); return; }
        if (m instanceof MatterConverter mc)    { gui.openMatterConverter(e.getPlayer(), mc); return; }
        if (m instanceof BatteryBlock bb)      { gui.openBattery(e.getPlayer(), bb); return; }
        if (m instanceof SolarGenerator sg)    { gui.openSolar(e.getPlayer(), sg); return; }
        if (m instanceof WindGenerator wg)     { gui.openWind(e.getPlayer(), wg); return; }
        if (m instanceof HydroGenerator hg)    { gui.openHydro(e.getPlayer(), hg); return; }
        if (m instanceof AutoMiner am)         { gui.openMiner(e.getPlayer(), am); return; }
    }
}
