package net.mehvahdjukaar.complementaries.mixins.forge;

import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Inject(method = "shouldSnow", at = @At("RETURN"), cancellable = true)
    public void saltPreventsSnow(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            if (level.getChunk(pos) instanceof LevelChunk lc) {
                var c = ModCapabilities.get(lc, ModCapabilities.SALTED_CAPABILITY);
                if (c != null && c.isSalted(pos.below())) cir.setReturnValue(false);
            }
        }
    }
}
