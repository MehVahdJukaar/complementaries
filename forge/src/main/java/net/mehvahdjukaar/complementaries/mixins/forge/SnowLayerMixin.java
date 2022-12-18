package net.mehvahdjukaar.complementaries.mixins.forge;

import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerMixin extends Block {

    protected SnowLayerMixin(Properties arg) {
        super(arg);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void saltPreventsSnow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo cir) {
        if (level.getChunk(pos) instanceof LevelChunk lc) {
            var c = ModCapabilities.get(lc, ModCapabilities.SALTED_CAPABILITY);
            if (c != null && c.isSalted(pos.below())){
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
                cir.cancel();
            }
        }
    }
}
