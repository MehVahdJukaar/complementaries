package net.mehvahdjukaar.complementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

public class ComplementariesPlatformStuff {
    @ExpectPlatform
    public static void toggleSalted(LevelChunk levelChunk, BlockPos pos, boolean salted) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isSlated(LevelChunk levelChunk, BlockPos pos) {
        throw new AssertionError();
    }
}
