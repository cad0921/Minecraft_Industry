package com.create.industry.gui;

import com.create.industry.IndustryPlugin;
import com.create.industry.machine.ProcessingMachine;
import com.create.industry.machine.impl.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiService {

    public static final int SLOT_BTN_LEFT  = 20;
    public static final int SLOT_PROGRESS  = 13;
    public static final int SLOT_BTN_RIGHT = 22;

    private ItemStack pane(Material color, String name) {
        ItemStack i = new ItemStack(color);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.text(name));
        i.setItemMeta(m); return i;
    }
    private ItemStack gray(String name) { return pane(Material.GRAY_STAINED_GLASS_PANE, name); }
    private ItemStack button(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.text(name));
        i.setItemMeta(m); return i;
    }
    private ItemStack progressBar(String title, int percent) {
        int stage = Math.max(0, Math.min(10, percent/10));
        Material mat = switch (stage) { case 0,1,2,3 -> Material.RED_STAINED_GLASS_PANE; case 4,5,6,7 -> Material.ORANGE_STAINED_GLASS_PANE; default -> Material.LIME_STAINED_GLASS_PANE; };
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.text(title + " " + percent + "%"));
        i.setItemMeta(m); return i;
    }
    private ItemStack label(Material mat, String name) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.text(name));
        i.setItemMeta(m); return i;
    }

    // ======= Matter Converter =======
    public void openMatterConverter(Player p, MatterConverter mc) {
        Inventory inv = Bukkit.createInventory(p, 27, Component.text("物質轉換器"));
        for (int i=0;i<27;i++) if (i<9 || i>17 || i%9==0 || i%9==8) inv.setItem(i, gray(" "));
        inv.setItem(11, label(mc.currentMaterial(), "當前"));
        inv.setItem(15, label(mc.nextMaterial(), "下一步"));
        inv.setItem(SLOT_PROGRESS, progressBar("進度", mc.progressPercent()));
        inv.setItem(SLOT_BTN_RIGHT, button(mc.isPaused()?Material.LIME_DYE:Material.RED_DYE, mc.isPaused()? "恢復":"暫停"));
        p.openInventory(inv);
        IndustryPlugin.inst().machines().bindInventory(inv, mc);
    }

    // ======= Processing =======
    public void openProcessing(Player p, ProcessingMachine m) {
        var inv = m.inventory();
        for (int i=0;i<27;i++) if (i<9 || i>17 || i%9==0 || i%9==8) inv.setItem(i, gray(" "));
        inv.setItem(SLOT_PROGRESS, progressBar("進度", m.progressPercent()));
        inv.setItem(SLOT_BTN_RIGHT, button(m.isPaused()?Material.LIME_DYE:Material.RED_DYE, m.isPaused()? "恢復":"暫停"));
        p.openInventory(inv);
        IndustryPlugin.inst().machines().bindInventory(inv, m);
    }

    // ======= Battery =======
    public void openBattery(Player p, BatteryBlock b) {
        Inventory inv = Bukkit.createInventory(p, 27, Component.text("電池"));
        for (int i=0;i<27;i++) if (i<9 || i>17 || i%9==0 || i%9==8) inv.setItem(i, gray(" "));
        int cap = b.getCapacity();
        int stored = b.getStored();
        int percent = cap > 0 ? (int)Math.min(100, Math.round(100.0 * stored / cap)) : 0;

        String mode = switch (b.getMode()) {
            case BOTH -> "進/出";
            case INPUT_ONLY -> "僅進";
            case OUTPUT_ONLY -> "僅出";
        };

        inv.setItem(11, label(Material.REDSTONE, "容量: " + cap + " FE"));
        inv.setItem(SLOT_PROGRESS, progressBar("儲量", percent));
        inv.setItem(SLOT_BTN_LEFT, button(Material.REPEATER, "模式：" + mode + "（點我切換）"));
        inv.setItem(SLOT_BTN_RIGHT, button(b.isPaused()?Material.LIME_DYE:Material.RED_DYE, b.isPaused()? "恢復":"暫停"));

        p.openInventory(inv);
        IndustryPlugin.inst().machines().bindInventory(inv, b);
    }

    // ======= Generators =======
    private void openGenerator(Player p, AbstractGenerator g, String title, Material icon) {
        Inventory inv = Bukkit.createInventory(p, 27, Component.text(title));
        for (int i=0;i<27;i++) if (i<9 || i>17 || i%9==0 || i%9==8) inv.setItem(i, gray(" "));
        inv.setItem(11, label(icon, "類型"));
        inv.setItem(SLOT_PROGRESS, label(Material.REDSTONE_TORCH, "最近輸出: " + g.getLastOutput() + " FE/t"));
        inv.setItem(SLOT_BTN_RIGHT, button(g.isPaused()?Material.LIME_DYE:Material.RED_DYE, g.isPaused()? "恢復":"暫停"));
        p.openInventory(inv);
        IndustryPlugin.inst().machines().bindInventory(inv, g);
    }
    public void openSolar(Player p, SolarGenerator g) { openGenerator(p, g, "太陽能發電機", Material.DAYLIGHT_DETECTOR); }
    public void openWind(Player p, WindGenerator g)  { openGenerator(p, g, "風力發電機", Material.GRINDSTONE); }
    public void openHydro(Player p, HydroGenerator g){ openGenerator(p, g, "水力發電機", Material.CAULDRON); }

    // ======= Auto Miner =======
    public void openMiner(Player p, AutoMiner m) {
        Inventory inv = m.inventory();
        for (int i=0;i<27;i++) if (i<9 || i>17 || i%9==0 || i%9==8) inv.setItem(i, gray(" "));

        // 升級槽指示（不覆蓋玩家已放入的升級）
        if (inv.getItem(AutoMiner.SLOT_UPG_ROUTER) == null)
            inv.setItem(AutoMiner.SLOT_UPG_ROUTER, label(Material.COMPARATOR, "路由器(升級)"));
        if (inv.getItem(AutoMiner.SLOT_UPG_SPEED) == null)
            inv.setItem(AutoMiner.SLOT_UPG_SPEED,  label(Material.SUGAR, "加速 I/II/III"));
        if (inv.getItem(AutoMiner.SLOT_UPG_SILK) == null)
            inv.setItem(AutoMiner.SLOT_UPG_SILK,   label(Material.STRING, "絲綢"));
        if (inv.getItem(AutoMiner.SLOT_UPG_SMELT) == null)
            inv.setItem(AutoMiner.SLOT_UPG_SMELT,  label(Material.BLAZE_POWDER, "熔煉"));

        // 路由方向按鈕
        String dir = m.getRouterDir().name();
        inv.setItem(SLOT_BTN_LEFT, button(Material.COMPASS, "路由方向：" + dir + "（點我切換）"));

        // 目標方塊 / 暫停
        Block self = m.block();
        BlockFace face = m.getFacing();
        Block target = self.getRelative(face);
        Material tm = target.getType();
        inv.setItem(SLOT_PROGRESS, label(tm==Material.AIR?Material.BARRIER:tm, "目標: " + tm.name()));
        inv.setItem(SLOT_BTN_RIGHT, button(m.isPaused()?Material.LIME_DYE:Material.RED_DYE, m.isPaused()? "恢復":"暫停"));

        p.openInventory(inv);
        IndustryPlugin.inst().machines().bindInventory(inv, m);
    }
}