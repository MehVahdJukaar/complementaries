package net.mehvahdjukaar.complementaries.common.worldgen;

import net.minecraft.world.level.levelgen.DensityFunctions;

import javax.annotation.Nullable;

public interface NC {
    @Nullable
    Saltifer getSaltifer();
    void setSaltifer(Saltifer saltifer);


    DensityFunctions.BeardifierOrMarker getBreadifier();

}
