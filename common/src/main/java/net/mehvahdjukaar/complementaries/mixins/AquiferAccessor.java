package net.mehvahdjukaar.complementaries.mixins;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Aquifer.NoiseBasedAquifer.class)
public interface AquiferAccessor {

    @Accessor("barrierNoise")
    DensityFunction getBarrierNoise();

    @Accessor("fluidLevelFloodednessNoise")
    DensityFunction getFluidLevelFloodednessNoise();

    @Accessor("fluidLevelSpreadNoise")
    DensityFunction getFluidLevelSpreadNoise();

    @Accessor("lavaNoise")
    DensityFunction getLavaNoise();


    @Accessor("erosion")
    DensityFunction getErosion();

    @Accessor("depth")
    DensityFunction getDepth();

    @Accessor("positionalRandomFactory")
    PositionalRandomFactory getPositionalRandomFactory();
    @Accessor("minGridX")
    int minGridX();
    @Accessor("gridSizeX")
    int gridSizeX();
    @Accessor("minGridZ")
    int minGridZ();
    @Accessor("gridSizeZ")
    int gridSizeZ();
}
