package net.mehvahdjukaar.complementaries.mixins;

import net.mehvahdjukaar.complementaries.common.worldgen.BeardifierWithSaltProcessor;
import net.mehvahdjukaar.complementaries.common.worldgen.NC;
import net.mehvahdjukaar.complementaries.common.worldgen.SaltBeardifier;
import net.mehvahdjukaar.complementaries.common.worldgen.Saltifer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin implements NC {


    @Shadow
    @Final
    int firstNoiseX;
    @Shadow
    @Final
    int firstNoiseZ;
    @Shadow
    @Final
    private Aquifer aquifer;
    @Shadow
    @Final
    private NoiseSettings noiseSettings;

    @Shadow
    protected abstract Climate.Sampler cachedClimateSampler(NoiseRouter noiseRouter, List<Climate.ParameterPoint> points);

    @Shadow
    @Final
    private DensityFunctions.BeardifierOrMarker beardifier;

    @Unique
    @Nullable
    private Saltifer saltifer;

    public void setSaltifer(Saltifer saltifer) {
        this.saltifer = saltifer;
    }

    @Override
    public DensityFunctions.BeardifierOrMarker getBreadifier() {
        return beardifier;
    }

    @Nullable
    @Override
    public Saltifer getSaltifer() {

        return saltifer;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Aquifer;create(Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/NoiseRouter;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;IILnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)Lnet/minecraft/world/level/levelgen/Aquifer;"))
    private NoiseChunk createSaltifer(NoiseChunk chunk, ChunkPos chunkPos, NoiseRouter noiseRouter, PositionalRandomFactory positionalRandomFactory, int minY, int height, Aquifer.FluidPicker globalFluidPicker) {
        //Saltifer saltifer = new Saltifer(chunk, chunkPos, noiseRouter, positionalRandomFactory, minY, height, globalFluidPicker);
        // this.setSaltifer(saltifer);
        if (this.beardifier instanceof BeardifierWithSaltProcessor ba) {
            ba.addSaltPostProcessor(new SaltBeardifier(chunk, chunkPos, noiseRouter, positionalRandomFactory));
        }
        return chunk;
    }
}
