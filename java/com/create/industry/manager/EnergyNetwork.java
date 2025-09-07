package com.create.industry.manager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.create.industry.util.WireTier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnergyNetwork {
    private final Map<Location, Integer> buffers = new HashMap<>();
    private final Map<Location, WireTier> wires = new HashMap<>(); // wire nodes with tier
    private final Set<Location> nodes = new HashSet<>(); // machine nodes (non-wire) to keep as targets

    public EnergyNetwork(Object cfgIgnored) {} // keep signature compatible; config not required here

    /** 將能量提供到該座標緩衝池，回傳實際接受量（本實作全收，含溢位保護） */
    public int offerEnergy(Location from, int amount) {
        if (amount <= 0) return 0;
        Location key = from.toBlockLocation();
        int before = buffers.getOrDefault(key, 0);
        long after = (long) before + amount;
        int accepted;
        if (after > Integer.MAX_VALUE) {
            accepted = Integer.MAX_VALUE - before;
            buffers.put(key, Integer.MAX_VALUE);
        } else {
            accepted = amount;
            buffers.put(key, (int) after);
        }
        nodes.add(key); // 確保是節點
        return Math.max(0, accepted);
    }

    /** 從近處座標拉取能量（只拉當前節點的緩衝） */
    public int pullEnergy(Location near, int want) {
        if (want <= 0) return 0;
        Location key = near.toBlockLocation();
        int have = buffers.getOrDefault(key, 0);
        int take = Math.min(have, want);
        if (take > 0) buffers.put(key, have - take);
        nodes.add(key); // 確保是節點
        return take;
    }

    /** 查詢該座標的可用能量（除錯用） */
    public int available(Location at) {
        return buffers.getOrDefault(at.toBlockLocation(), 0);
    }

    /** 註冊/移除「機器節點」（非導線也可成為節點，以便傳輸到這格） */
    public void registerNode(Location at) {
        nodes.add(at.toBlockLocation());
        buffers.putIfAbsent(at.toBlockLocation(), 0);
    }
    public void unregisterNode(Location at) {
        nodes.remove(at.toBlockLocation());
        // 不強制刪除 buffer，避免瞬斷丟失；如需清空可另外 clear()
    }

    /** 註冊/移除導線 */
    public void registerWire(Location at, WireTier tier) {
        Location key = at.toBlockLocation();
        wires.put(key, tier);
        buffers.putIfAbsent(key, 0);
    }
    public void unregisterWire(Location at) {
        wires.remove(at.toBlockLocation());
        // 保留 buffer（可選）
    }

    /** 每 tick 呼叫：在導線之間與其鄰接節點之間進行能量平衡（含損耗與每跳上限） */
    public void tick() {
        // 對每一條導線，嘗試與 6 個相鄰方塊做「由高往低」的平衡
        for (Map.Entry<Location, WireTier> e : wires.entrySet()) {
            Location wire = e.getKey();
            WireTier tier = e.getValue();

            for (BlockFace f : BlockFace.values()) {
                if (!isCardinal(f)) continue;
                Location n = wire.clone().add(f.getModX(), f.getModY(), f.getModZ());

                boolean neighborIsWire = wires.containsKey(n);
                boolean neighborIsNode = nodes.contains(n);
                if (!neighborIsWire && !neighborIsNode) continue; // 非節點也非導線，不處理

                WireTier edgeTier = neighborIsWire
                        ? minTier(tier, wires.get(n))
                        : tier;

                int src = buffers.getOrDefault(wire, 0);
                int dst = buffers.getOrDefault(n, 0);
                if (src <= dst) continue;

                int gap = src - dst;
                int desire = Math.max(1, gap / 2); // 嘗試平一半，避免震盪過大
                int move = Math.min(edgeTier.maxTransfer, desire);

                if (move <= 0) continue;
                // 應用損耗（向下取整）
                int arrive = (int) Math.floor(move * (1.0 - edgeTier.lossPerBlock));
                if (arrive <= 0) continue;

                buffers.put(wire, src - move);
                buffers.put(n, dst + arrive);

                // 確保這兩格都是已知節點，以便後續機器能 pull
                nodes.add(wire);
                nodes.add(n);
            }
        }
    }

    private static boolean isCardinal(BlockFace f) {
        return switch (f) {
            case NORTH, SOUTH, EAST, WEST, UP, DOWN -> true;
            default -> false;
        };
    }

    private static WireTier minTier(WireTier a, WireTier b) {
        // 以 maxTransfer 較小者為準（保守）
        return (a.maxTransfer <= b.maxTransfer) ? a : b;
    }

    /** 清空某座標緩衝 */
    public void clear(Location at) { buffers.remove(at.toBlockLocation()); }
}
