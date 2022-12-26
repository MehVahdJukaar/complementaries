package net.mehvahdjukaar.complementaries.common.worldgen;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public interface BeardifierAccess {

    void addInner(NoiseChunk noiseChunk,
                  ChunkPos chunkPos,
                  NoiseRouter noiseRouter,
                  PositionalRandomFactory positionalRandomFactory);

    SaltBeardifier getInner();
}
