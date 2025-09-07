package com.create.industry.util;

public enum WireTier {
    LV(256, 0.02),
    MV(1024, 0.01),
    HV(4096, 0.002);

    public final int maxTransfer;
    public final double lossPerBlock;

    WireTier(int maxTransfer, double lossPerBlock) {
        this.maxTransfer = maxTransfer;
        this.lossPerBlock = lossPerBlock;
    }

    public static WireTier of(String id) {
        if (id == null) return LV;
        try {
            return WireTier.valueOf(id.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return LV;
        }
    }
}
