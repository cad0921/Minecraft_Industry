package com.create.industry.data;

import org.bukkit.Material;

public record ProcessingRecipe(Material in, int inCount, Material out, int outCount, int fe, int timeTicks) {}
