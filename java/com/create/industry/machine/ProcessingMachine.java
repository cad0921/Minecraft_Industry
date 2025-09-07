package com.create.industry.machine;

import com.create.industry.IndustryPlugin;
import com.create.industry.data.ProcessingRecipe;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class ProcessingMachine extends AbstractMachine {
    protected Inventory inv;
    protected int ticks;
    protected ProcessingRecipe cur;

    public ProcessingMachine(Location loc, String id, int level) {
        super(loc, id, level);
        this.inv = Bukkit.createInventory(null, 27, Component.text(guiTitle()));
    }

    protected abstract String recipeKey();
    protected abstract String guiTitle();

    protected List<ProcessingRecipe> recipes() {
        return IndustryPlugin.inst().recipes().getProcessing(recipeKey());
    }

    @Override public void onTick() {
        ItemStack in = inv.getItem(11);
        if (in == null || in.getType() == Material.AIR) { ticks = 0; cur = null; return; }
        if (cur == null || in.getType() != cur.in()) { cur = match(in.getType()); ticks = 0; }
        if (cur == null) { ticks = 0; return; }
        if (in.getAmount() < cur.inCount()) { ticks = 0; return; }

        int needFe = (ticks == 0 ? cur.fe() : 0);
        if (needFe > 0) {
            int pulled = IndustryPlugin.inst().energy().pullEnergy(loc, needFe);
            if (pulled < needFe) return;
        }

        ticks++;
        if (ticks >= cur.timeTicks()) {
            ItemStack outSlot = inv.getItem(15);
            if (!canInsert(outSlot, cur.out(), cur.outCount())) return;
            in.setAmount(in.getAmount() - cur.inCount());
            inv.setItem(11, in.getAmount()>0 ? in : null);
            inv.setItem(15, merge(outSlot, new ItemStack(cur.out(), cur.outCount())));
            ticks = 0; cur = null;
        }
    }

    private boolean canInsert(ItemStack slot, Material m, int count) {
        if (slot == null || slot.getType() == Material.AIR) return true;
        if (slot.getType() != m) return false;
        return slot.getAmount() + count <= slot.getMaxStackSize();
    }
    private ItemStack merge(ItemStack slot, ItemStack add) {
        if (slot == null || slot.getType() == Material.AIR) return add;
        slot.setAmount(slot.getAmount() + add.getAmount());
        return slot;
    }
    private ProcessingRecipe match(Material in) {
        return recipes().stream().filter(r -> r.in() == in).findFirst().orElse(null);
    }

    public Inventory inventory() { return inv; }
    public int progressPercent() { if (cur == null) return 0; return Math.min(100, (int)Math.floor(100.0 * ticks / cur.timeTicks())); }
}
