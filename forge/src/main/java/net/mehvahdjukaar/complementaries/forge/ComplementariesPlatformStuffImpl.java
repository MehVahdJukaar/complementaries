package net.mehvahdjukaar.complementaries.forge;

import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class ComplementariesPlatformStuffImpl {
    public static void toggleSalted(LevelChunk levelChunk, BlockPos pos, boolean salted) {
        levelChunk.getCapability(ModCapabilities.SALTED_CAPABILITY).resolve().get().setSalted(pos, salted);
    }

    public static boolean isSlated(LevelChunk levelChunk, BlockPos pos) {
      return  levelChunk.getCapability(ModCapabilities.SALTED_CAPABILITY).resolve().get().isSalted(pos);
    }
}
