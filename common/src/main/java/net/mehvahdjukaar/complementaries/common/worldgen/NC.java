package net.mehvahdjukaar.complementaries.common.worldgen;

import net.mehvahdjukaar.complementaries.reg.ModRegistry;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import javax.annotation.Nullable;

public interface NC {
    @Nullable
    Saltifer getSaltifer();
    void setSaltifer(Saltifer saltifer);


    DensityFunctions.BeardifierOrMarker getBreadifier();
}
