package net.mehvahdjukaar.complementaries.mixins;

import net.mehvahdjukaar.complementaries.common.worldgen.NC;
import net.mehvahdjukaar.complementaries.common.worldgen.Saltifer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public abstract class AquiferMixin {

    @Shadow private boolean shouldScheduleFluidUpdate;

    @Shadow @Final private Aquifer.FluidPicker globalFluidPicker;

    @Shadow protected abstract int getIndex(int gridX, int gridY, int gridZ);

    @Shadow @Final private Aquifer.FluidStatus[] aquiferCache;

    @Shadow @Final private long[] aquiferLocationCache;

    @Shadow @Final private NoiseChunk noiseChunk;

    @Inject(method = "computeSubstance", at = @At("HEAD"), cancellable = true)
    public void aa(DensityFunction.FunctionContext context, double substance, CallbackInfoReturnable<BlockState> cir){

        cir.setReturnValue(((NC)  this.noiseChunk).getSaltifer().computeSubstance(context, substance));
    }

}
