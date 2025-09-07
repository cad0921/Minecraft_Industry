package com.create.industry.machine.impl;

import com.create.industry.IndustryPlugin;
import com.create.industry.machine.AbstractMachine;
import com.create.industry.util.UpgradeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class AutoMiner extends AbstractMachine {

    // GUI layout
    public static final int SLOT_BTN_LEFT  = 20; // router dir cycle
    public static final int SLOT_PROGRESS  = 13; // center label
    public static final int SLOT_BTN_RIGHT = 22; // pause/resume
    public static final int SLOT_UPG_ROUTER = 2;
    public static final int SLOT_UPG_SPEED  = 4;
    public static final int SLOT_UPG_SILK   = 6;
    public static final int SLOT_UPG_SMELT  = 7;

    // 9 storage slots (avoid 13/20/22)
    public static final int[] STORAGE_SLOTS = new int[]{10,11,12, 14,15,16, 19,21,24};

    private final Inventory inv;
    private int workTicks = 0;
    private int routerPushCooldown = 0;
    private BlockFace routerDir = BlockFace.SOUTH; // default

    public AutoMiner(org.bukkit.Location loc, int level) {
        super(loc, "AUTO_MINER", level);
        this.inv = Bukkit.createInventory(null, 27, Component.text("自動挖礦機"));
    }

    public Inventory inventory() { return inv; }

    @Override
    public void onTick() {
        if (paused) return;
        Block self = block();
        if (!(self.getBlockData() instanceof Directional dir)) return;

        // push items via router if installed
        if (hasRouter()) {
            if (routerPushCooldown-- <= 0) {
                pushOneStack(self.getRelative(routerDir));
                routerPushCooldown = 10; // every 10 ticks
            }
        }

        // mine cycle
        int interval = intervalTicks();
        if (workTicks++ < interval) return;
        workTicks = 0;

        Block target = self.getRelative(dir.getFacing());
        if (!IndustryPlugin.inst().machinesConfig().canMine(target.getType())) return;

        ItemStack drop = getDropFor(target.getType());
        if (drop == null || drop.getType() == Material.AIR) return;

        // Check storage capacity; if full stop
        if (!canInsertStorage(drop)) return;

        int cost = IndustryPlugin.inst().machinesConfig().minerCostPerBlock();
        int pulled = IndustryPlugin.inst().energy().pullEnergy(loc, cost);
        if (pulled < cost) return;

        // perform break
        target.setType(Material.AIR);
        insertIntoStorage(drop);
    }

    private int intervalTicks() {
        int base = 40; // base 2s per block
        int lvl = speedLevel();
        double mult = switch (lvl) { case 1 -> 0.7; case 2 -> 0.5; case 3 -> 0.3; default -> 1.0; };
        return (int)Math.max(1, Math.round(base * mult));
    }

    private boolean hasRouter() {
        return UpgradeUtil.isRouter(inv.getItem(SLOT_UPG_ROUTER));
    }
    private int speedLevel() {
        return UpgradeUtil.speedLevel(inv.getItem(SLOT_UPG_SPEED));
    }
    private boolean silkEnabled() {
        return UpgradeUtil.isSilk(inv.getItem(SLOT_UPG_SILK));
    }
    private boolean smeltEnabled() {
        return UpgradeUtil.isSmelt(inv.getItem(SLOT_UPG_SMELT));
    }

    public BlockFace getFacing() {
        if (block().getBlockData() instanceof Directional dir) return dir.getFacing();
        return BlockFace.NORTH;
    }
    public BlockFace getRouterDir() { return routerDir; }
    public void cycleRouterDir() {
        List<BlockFace> order = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN);
        int idx = order.indexOf(routerDir);
        routerDir = order.get((idx+1) % order.size());
    }

    private ItemStack getDropFor(Material m) {
        if (silkEnabled()) {
            return new ItemStack(m);
        }
        if (smeltEnabled()) {
            Material out = smeltResult(m);
            if (out != null) return new ItemStack(out);
        }
        // default: simplified to the block itself; you can extend vanilla table
        return new ItemStack(m);
    }

    private Material smeltResult(Material m) {
        return switch (m) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case SAND -> Material.GLASS;
            case COBBLESTONE -> Material.STONE;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }

    private boolean canInsertStorage(ItemStack stack) {
        int remaining = stack.getAmount();
        // try merge
        for (int s : STORAGE_SLOTS) {
            ItemStack cur = inv.getItem(s);
            if (cur != null && cur.isSimilar(stack)) {
                int can = Math.min(remaining, cur.getMaxStackSize() - cur.getAmount());
                remaining -= can;
                if (remaining <= 0) return true;
            }
        }
        // try empty slots
        for (int s : STORAGE_SLOTS) {
            ItemStack cur = inv.getItem(s);
            if (cur == null || cur.getType() == Material.AIR) return true;
        }
        return false;
    }

    private void insertIntoStorage(ItemStack stack) {
        // merge
        for (int s : STORAGE_SLOTS) {
            ItemStack cur = inv.getItem(s);
            if (cur != null && cur.isSimilar(stack)) {
                int can = Math.min(stack.getAmount(), cur.getMaxStackSize() - cur.getAmount());
                if (can > 0) {
                    cur.setAmount(cur.getAmount() + can);
                    stack.setAmount(stack.getAmount() - can);
                    inv.setItem(s, cur);
                    if (stack.getAmount() <= 0) return;
                }
            }
        }
        // empty
        for (int s : STORAGE_SLOTS) {
            ItemStack cur = inv.getItem(s);
            if (cur == null || cur.getType() == Material.AIR) {
                inv.setItem(s, stack.clone());
                stack.setAmount(0);
                return;
            }
        }
        // no space: drop (failsafe)
        if (stack.getAmount() > 0) {
            block().getWorld().dropItemNaturally(loc, stack);
        }
    }

    private void pushOneStack(Block targetContainer) {
        if (!(targetContainer.getState() instanceof InventoryHolder holder)) return;
        Inventory out = holder.getInventory();
        // find first non-empty storage slot
        for (int s : STORAGE_SLOTS) {
            ItemStack cur = inv.getItem(s);
            if (cur == null || cur.getType() == Material.AIR) continue;
            ItemStack moving = cur.clone();
            int moved = 0;
            // try merge into output
            for (int i=0;i<out.getSize() && moving.getAmount() > 0;i++) {
                ItemStack oi = out.getItem(i);
                if (oi != null && oi.isSimilar(moving)) {
                    int can = Math.min(moving.getAmount(), oi.getMaxStackSize() - oi.getAmount());
                    if (can > 0) {
                        oi.setAmount(oi.getAmount() + can);
                        moving.setAmount(moving.getAmount() - can);
                        out.setItem(i, oi);
                        moved += can;
                    }
                }
            }
            // try empty slots
            for (int i=0;i<out.getSize() && moving.getAmount() > 0;i++) {
                ItemStack oi = out.getItem(i);
                if (oi == null || oi.getType() == Material.AIR) {
                    out.setItem(i, moving.clone());
                    moved += moving.getAmount();
                    moving.setAmount(0);
                    break;
                }
            }
            if (moved > 0) {
                // update storage
                int remain = cur.getAmount() - moved;
                if (remain <= 0) inv.setItem(s, null);
                else { cur.setAmount(remain); inv.setItem(s, cur); }
                return;
            }
        }
    }
}