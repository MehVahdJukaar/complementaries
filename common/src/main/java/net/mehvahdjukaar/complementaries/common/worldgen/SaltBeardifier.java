//
// Source code recreated from a .class file by Quiltflower
//

package net.mehvahdjukaar.complementaries.common.worldgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.mehvahdjukaar.complementaries.mixins.MultiNoiseBiomeSourceAccessor;
import net.mehvahdjukaar.complementaries.reg.ModWorldgen;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions.BeardifierOrMarker;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import terrablender.core.TerraBlender;
import terrablender.worldgen.IExtendedParameterList;

import java.util.List;

public class SaltBeardifier implements BeardifierOrMarker {
    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = Util.make(
            new float[BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE], fs -> {
                for (int i = 0; i < BEARD_KERNEL_SIZE; ++i) {
                    for (int j = 0; j < BEARD_KERNEL_SIZE; ++j) {
                        for (int k = 0; k < BEARD_KERNEL_SIZE; ++k) {
                            fs[i * BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE + j * BEARD_KERNEL_SIZE + k] =
                                    (float) computeBeardContribution(j - BEARD_KERNEL_RADIUS, k - BEARD_KERNEL_RADIUS, i - BEARD_KERNEL_RADIUS);
                        }
                    }
                }
            });

    private ObjectListIterator<BoundingBox> pieceIterator;
    private final NoiseChunk noiseChunk;
    private final ChunkPos chunkPos;
    private final NoiseRouter noiseRouter;
    private final PositionalRandomFactory random;

    private boolean[][] biomeCache = null;
    private static final int START_Y = 64;
    private static final int END_Y = 87;
    private MultiNoiseBiomeSource biomeSource;


    public SaltBeardifier(NoiseChunk noiseChunk, ChunkPos chunkPos, NoiseRouter noiseRouter, PositionalRandomFactory randomFactory) {
        this.noiseChunk = noiseChunk;
        this.chunkPos = chunkPos;
        this.noiseRouter = noiseRouter;
        this.random = randomFactory;

        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();

        ObjectList<BoundingBox> objectList = new ObjectArrayList<>(10);
        objectList.add(new BoundingBox(x, 40, z, x + 4, 70, z + 4));

        this.pieceIterator = objectList.iterator();

    }


    public double compute(FunctionContext context) {
        if (this.biomeCache == null && biomeSource != null) lateInitialize(biomeSource);
        if (this.biomeCache == null) return 0;
        int blockX = context.blockX();
        int blockY = context.blockY();
        int blockZ = context.blockZ();
        //if(blockY<START_Y)return 10;


        if (blockY < START_Y - 10 || blockY > END_Y + 20) return 0;
        double density;
        double deltaDensity;
        for (density = 0.0; this.pieceIterator.hasNext(); density += deltaDensity) {
            BoundingBox boundingBox = this.pieceIterator.next();

            int dx = Math.max(0, Math.max(boundingBox.minX() - blockX, blockX - boundingBox.maxX()));
            int dz = Math.max(0, Math.max(boundingBox.minZ() - blockZ, blockZ - boundingBox.maxZ()));
            int minY = boundingBox.minY();
            int distanceFromMinY = blockY - minY;

            var terrain = TerrainAdjustment.BEARD_BOX;

            int q = switch (terrain) {
                case NONE -> 0;
                case BURY, BEARD_THIN -> distanceFromMinY;
                case BEARD_BOX -> Math.max(0, Math.max(minY - blockY, blockY - boundingBox.maxY()));
            };
            terrain = TerrainAdjustment.BEARD_THIN;

            deltaDensity = switch (terrain) {
                case NONE -> 0.0;
                case BURY -> getBuryContribution(dx, q, dz);
                case BEARD_THIN, BEARD_BOX -> getBeardContribution(dx, q, dz, distanceFromMinY) * 0.8;
            };
        }

        this.pieceIterator.back(Integer.MAX_VALUE);

        return density;
    }

    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(int x, int y, int z) {
        double d = Mth.length(x, y / 2.0, z);
        return Mth.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getBeardContribution(int x, int y, int z, int i) {
        int j = x + BEARD_KERNEL_RADIUS;
        int k = y + BEARD_KERNEL_RADIUS;
        int l = z + BEARD_KERNEL_RADIUS;
        if (isInKernelRange(j) && isInKernelRange(k) && isInKernelRange(l)) {
            double d = i + 0.5;
            double e = Mth.lengthSquared(x, d, z);
            double f = -d * Mth.fastInvSqrt(e / 2.0) / 2.0;
            return f * BEARD_KERNEL[l * BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE + j * BEARD_KERNEL_SIZE + k];
        } else {
            return 0.0;
        }
    }

    private static boolean isInKernelRange(int i) {
        return i >= 0 && i < BEARD_KERNEL_SIZE;
    }

    private static double computeBeardContribution(int x, int y, int z) {
        return computeBeardContribution(x, y + 0.5, z);
    }

    private static double computeBeardContribution(int i, double d, int j) {
        double e = Mth.lengthSquared(i, d, j);
        return Math.pow(Math.E, -e / 16.0);
    }


    private boolean isTargetBiome(MultiNoiseBiomeSource source, int x, int y, int z, Climate.Sampler sampler) {
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(x,y,z);
        var c = Climate.target((float)noiseRouter.temperature().compute(point),
                (float)noiseRouter.vegetation().compute(point),
                (float)noiseRouter.continents().compute(point),
                (float)noiseRouter.erosion().compute(point),
                (float)noiseRouter.depth().compute(point),
                (float)noiseRouter.ridges().compute(point));

        var b =((IExtendedParameterList<Holder<Biome>>)((MultiNoiseBiomeSourceAccessor)source).getParameters())
                .findValuePositional(c, QuartPos.fromBlock(x),QuartPos.fromBlock(y),QuartPos.fromBlock(z));
        return b.is(ModWorldgen.SALT_FLATS.getId());
    }

    private void computeBiomeCache(MultiNoiseBiomeSource source) {
        Climate.Sampler sampler = new Climate.Sampler(noiseRouter.temperature(),
                 noiseRouter.vegetation(),
                noiseRouter.continents(),
                noiseRouter.erosion(),
                noiseRouter.depth(),
                noiseRouter.ridges(), List.of());


        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        //offset to grab outer chunks too
        boolean[][] cache = new boolean[16 + 6][16 + 6];
        for (int x = 0; x < cache.length; x++) {
            for (int z = 0; z < cache[x].length; z++) {
                cache[x][z] = isTargetBiome(source,
                        indexToPos(x, chunkMinX) + 2,
                       64,
                        indexToPos(z, chunkMinZ) + 2,
                        sampler);
            }
        }
        this.biomeCache = cache;
    }

    public void initialize(MultiNoiseBiomeSource biomeSource) {
        this.biomeSource = biomeSource;
    }

    private void lateInitialize(MultiNoiseBiomeSource biomeSource) {
        if (biomeCache == null) {
            this.computeBiomeCache(biomeSource);
            int minBlockX = chunkPos.getMinBlockX();
            int minBlockZ = chunkPos.getMinBlockZ();
            ObjectList<BoundingBox> objectList = new ObjectArrayList<>(8);

            for (int i = 0; i < biomeCache.length; i++) {
                for (int j = 0; j < biomeCache[i].length; j++) {
                    if (biomeCache[i][j]) {
                        int px = indexToPos(i, minBlockX);
                        int pz = indexToPos(j, minBlockZ);
                        objectList.add(new BoundingBox(px, START_Y, pz,
                                px + 4, END_Y, pz + 4));

                    }
                }
            }
            this.pieceIterator = objectList.iterator();
        }
    }

    //use quartPos instead
    private static int indexToPos(int index, int chunkMin) {
        return chunkMin + ((index - 3) * 4);
    }


    private static int posToIndex(int blockPos) {
        return 3 + (blockPos % 16) / 4;
    }
}
