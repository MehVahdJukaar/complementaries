package net.mehvahdjukaar.complementaries.mixins.forge;

import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.Type.class)
public abstract class SpawnPlacementsMixin {

    @Inject(method = "canSpawnAt", at = @At("RETURN"), cancellable = true, remap = false)
    public void checkSlated(LevelReader level, BlockPos pos, EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && type.getCategory() == MobCategory.MONSTER) {
            if (level.getChunk(pos) instanceof LevelChunk lc) {
                var c = ModCapabilities.get(lc,ModCapabilities.SALTED_CAPABILITY);
                if(c != null && c.isSalted(pos.below()))cir.setReturnValue(false);
            }
        }
    }
}
