package net.mehvahdjukaar.complementaries.mixins;

import net.mehvahdjukaar.complementaries.common.worldgen.BeardifierWithSaltProcessor;
import net.mehvahdjukaar.complementaries.common.worldgen.NC;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public abstract class NoiseAquiferMixin {

    @Shadow private boolean shouldScheduleFluidUpdate;

    @Shadow @Final private Aquifer.FluidPicker globalFluidPicker;

    @Shadow protected abstract int getIndex(int gridX, int gridY, int gridZ);

    @Shadow @Final private Aquifer.FluidStatus[] aquiferCache;

    @Shadow @Final private long[] aquiferLocationCache;

    @Shadow @Final private NoiseChunk noiseChunk;

    @Inject(method = "computeSubstance", at = @At("HEAD"), cancellable = true)
    public void aa(DensityFunction.FunctionContext context, double substance, CallbackInfoReturnable<BlockState> cir){
        var s = ((NC)  this.noiseChunk).getSaltifer();
        var saltBeardifier = ((NC) this.noiseChunk).getBreadifier();
        if(saltBeardifier instanceof BeardifierWithSaltProcessor sb){
            var override = sb.getSaltPostProcessor().shouldOverride(context,substance);
            if(override.getResult() == InteractionResult.SUCCESS) {
                cir.setReturnValue(override.getObject());
            }
        }
       // if(s != null) cir.setReturnValue(s.computeSubstance(context, substance));
    }


}
