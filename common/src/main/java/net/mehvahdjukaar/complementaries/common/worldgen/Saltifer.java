package net.mehvahdjukaar.complementaries.common.worldgen;


import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Aquifers are responsible for non-sea level fluids found in terrain generation, but also managing that different aquifers don't intersect with each other in ways that would create undesirable fluid placement.
 * The aquifer interface itself is a modifier on a per-block basis. It computes a block state to be placed for each position in the world.
 * <p>
 * Aquifers work by first partitioning a single chunk into a low resolution grid. They then generate, via various noise layers, an {@link net.minecraft.world.level.levelgen.Aquifer.NoiseBasedAquifer.AquiferStatus} at each grid point.
 * At each point, the grid cell containing that point is calculated, and then of the eight grid corners, the three closest aquifers are found, by square euclidean distance.
 * Borders between aquifers are created by comparing nearby aquifers to see if the given point is near-equidistant from them, indicating a border if so, or fluid/air depending on the aquifer height if not.
 */
public class Saltifer implements Aquifer {
    private static final int X_RANGE = 6;
    private static final int Y_RANGE = 1;
    private static final int Z_RANGE = 6;
    private static final int X_SEPARATION = 6;
    private static final int Y_SEPARATION = 3;
    private static final int Z_SEPARATION = 6;
    private static final int X_SPACING = 8;
    private static final int Y_SPACING = 1;
    private static final int Z_SPACING = 8;
    private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
    private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
    private final NoiseChunk noiseChunk;
    private final DensityFunction barrierNoise;
    private final DensityFunction fluidLevelFloodednessNoise;
    private final DensityFunction fluidLevelSpreadNoise;
    private final DensityFunction lavaNoise;
    private final PositionalRandomFactory positionalRandomFactory;
    private final Aquifer.FluidStatus[] aquiferCache;
    private final long[] aquiferLocationCache;
    private final Aquifer.FluidPicker globalFluidPicker;
    private final DensityFunction erosion;
    private final DensityFunction depth;
    private final NoiseRouter noiseRouter;
    private boolean shouldScheduleFluidUpdate;
    private final int minGridX;
    private final int minGridY;
    private final int minGridZ;
    private final int gridSizeX;
    private final int gridSizeZ;
    private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
            {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };
    private MultiNoiseBiomeSource biomeSource;
    private int MAX_AQUIFER_H_FROM_GROUND = 20;



    public Saltifer(
            NoiseChunk noiseChunk,
            ChunkPos chunkPos,
            NoiseRouter noiseRouter,
            PositionalRandomFactory positionalRandomFactory,
            int i,
            int j,
            Aquifer.FluidPicker fluidPicker
    ) {
        this.noiseChunk = noiseChunk;
        this.noiseRouter = noiseRouter;
        this.barrierNoise = noiseRouter.barrierNoise();
        this.fluidLevelFloodednessNoise = noiseRouter.fluidLevelFloodednessNoise();
        this.fluidLevelSpreadNoise = noiseRouter.fluidLevelSpreadNoise();
        this.lavaNoise = noiseRouter.lavaNoise();
        this.erosion = noiseRouter.erosion();
        this.depth = noiseRouter.depth();
        this.positionalRandomFactory = positionalRandomFactory;
        this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
        Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(60, Blocks.WATER.defaultBlockState());
        Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        this.globalFluidPicker = (a, k, c) -> (k > 50) ? fluidStatus2 : fluidStatus3;
        int k = this.gridX(chunkPos.getMaxBlockX()) + 1;
        this.gridSizeX = k - this.minGridX + 1;
        this.minGridY = this.gridY(i) - 1;
        int l = this.gridY(i + j) + 1;
        int m = l - this.minGridY + 1;
        this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
        int n = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
        this.gridSizeZ = n - this.minGridZ + 1;
        int o = this.gridSizeX * m * this.gridSizeZ;
        this.aquiferCache = new Aquifer.FluidStatus[o];
        this.aquiferLocationCache = new long[o];
        Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);

    }

    private int getAqIndex(int gridX, int gridY, int gridZ) {
        int i = gridX - this.minGridX;
        int j = gridY - this.minGridY;
        int k = gridZ - this.minGridZ;
        return (j * this.gridSizeZ + k) * this.gridSizeX + i;
    }

    @Nullable
    @Override
    public BlockState computeSubstance(DensityFunction.FunctionContext context, double substance) {
        int blockX = context.blockX();
        int blockY = context.blockY();
        int blockZ = context.blockZ();
        if (isTargetBiome(blockX, blockY, blockZ)) {
            if (blockY > 62 && 70 > noiseChunk.preliminarySurfaceLevel(blockX, blockZ)) {
                return Blocks.AIR.defaultBlockState();
            } else if (blockY > 50 && blockY < 63) return Blocks.CRIMSON_PLANKS.defaultBlockState();
        }
        if (substance > 0.0) {

            //greater than 0 means theres terrain there. we only use negative substance
            this.shouldScheduleFluidUpdate = false;
            return null;
        } else {
            if (true) return Blocks.AIR.defaultBlockState();
            int aqGridX = gridX(blockX - 5);
            int aqGridY = gridY(blockY + 1);
            int aqGridZ = gridZ(blockZ - 5);
            int firstDist = Integer.MAX_VALUE;
            int secondDist = Integer.MAX_VALUE;
            int thirdDist = Integer.MAX_VALUE;
            long firstSelected = 0L; // the one that contains me
            long secondSelected = 0L;
            long thirdSelected = 0L;

            for (int dx = 0; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = 0; dz <= 1; ++dz) {
                        int selectedAqGridX = aqGridX + dx;
                        int selectedAqGridY = aqGridY + dy;
                        int selectedAqGridZ = aqGridZ + dz;
                        int aqIndex = this.getAqIndex(selectedAqGridX, selectedAqGridY, selectedAqGridZ);
                        long aqCenterPacked = this.aquiferLocationCache[aqIndex];
                        if (aqCenterPacked == Long.MAX_VALUE) {
                            RandomSource randomSource = this.positionalRandomFactory.at(selectedAqGridX, selectedAqGridY, selectedAqGridZ);
                            aqCenterPacked = BlockPos.asLong(
                                    selectedAqGridX * X_SPACING + randomSource.nextInt(X_RANGE),
                                    selectedAqGridY * Y_SPACING + randomSource.nextInt(Y_RANGE),
                                    selectedAqGridZ * Z_SPACING + randomSource.nextInt(Z_RANGE));
                            this.aquiferLocationCache[aqIndex] = aqCenterPacked;
                        }

                        int distanceX = BlockPos.getX(aqCenterPacked) - blockX;
                        int distanceY = BlockPos.getY(aqCenterPacked) - blockY;
                        int distanceZ = BlockPos.getZ(aqCenterPacked) - blockZ;
                        int distanceSqr = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
                        if (firstDist >= distanceSqr) {
                            thirdSelected = secondSelected;
                            secondSelected = firstSelected;
                            firstSelected = aqCenterPacked;
                            thirdDist = secondDist;
                            secondDist = firstDist;
                            firstDist = distanceSqr;
                        } else if (secondDist >= distanceSqr) {
                            thirdSelected = secondSelected;
                            secondSelected = aqCenterPacked;
                            thirdDist = secondDist;
                            secondDist = distanceSqr;
                        } else if (thirdDist >= distanceSqr) {
                            thirdSelected = aqCenterPacked;
                            thirdDist = distanceSqr;
                        }
                    }
                }
            }

            Aquifer.FluidStatus firstStatus = this.getAquiferStatus(firstSelected, substance);
            double similarity = similarity(firstDist, secondDist, firstSelected, secondSelected);
            BlockState firstFluid = firstStatus.at(blockY);
            if (similarity <= 0.0) { //too far away
                this.shouldScheduleFluidUpdate = similarity >= FLOWING_UPDATE_SIMULARITY;
                return firstFluid;
            } else {
                MutableDouble mutableDouble = new MutableDouble(Double.NaN);
                Aquifer.FluidStatus secondStatus = this.getAquiferStatus(secondSelected, substance);
                double e = similarity * this.calculatePressure(context, mutableDouble, firstStatus, secondStatus);
                if (substance + e > 0.0) {
                    this.shouldScheduleFluidUpdate = false;
                    return Blocks.GLOWSTONE.defaultBlockState(); //makes a barrier block when 2 are distant enough
                } else {
                    Aquifer.FluidStatus thirdStatus = this.getAquiferStatus(thirdSelected, substance);
                    double thirdSimilarity = similarity(firstDist, thirdDist);
                    if (thirdSimilarity > 0.0) { //if similar
                        double g = similarity * thirdSimilarity * this.calculatePressure(context, mutableDouble, firstStatus, thirdStatus);
                        if (substance + g > 0.0) {
                            this.shouldScheduleFluidUpdate = false;
                            return Blocks.EMERALD_BLOCK.defaultBlockState();
                        }
                    }

                    double g = similarity(secondDist, thirdDist);
                    if (g > 0.0) {
                        double h = similarity * g * this.calculatePressure(context, mutableDouble, secondStatus, thirdStatus);
                        if (substance + h > 0.0) {
                            this.shouldScheduleFluidUpdate = false;
                            return Blocks.REDSTONE_BLOCK.defaultBlockState();
                        }
                    }

                    this.shouldScheduleFluidUpdate = true;
                    return firstFluid;
                }
            }
        }
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return this.shouldScheduleFluidUpdate;
    }

    /**
     * Compares two distances (between aquifers).
     *
     * @return {@code 1.0} if the distances are equal, and returns smaller values the more different in absolute value the two distances are.
     */
    private double similarity(int firstDistance, int secondDistance, long firstPos, long secondPos) {
        double old = similarity(firstDistance, secondDistance);
        if (old > 0) {
            boolean f = isTargetBiome(BlockPos.getX(firstPos), BlockPos.getY(firstPos), BlockPos.getZ(firstPos));
            boolean s = isTargetBiome(BlockPos.getX(secondPos), BlockPos.getY(secondPos), BlockPos.getZ(secondPos));
            if (s || f) return 1;

        }
        return old;
    }

    private static double similarity(int firstDistance, int secondDistance) {
        double d = 25.0;
        return 1.0 - (double) Math.abs(secondDistance - firstDistance) / 25.0;
    }

    private double calculatePressure(DensityFunction.FunctionContext context, MutableDouble substance, Aquifer.FluidStatus firstFluid, Aquifer.FluidStatus secondFluid) {
        int blockY = context.blockY();
        BlockState blockState = firstFluid.at(blockY);
        BlockState blockState2 = secondFluid.at(blockY);
        if (blockState2.is(blockState.getBlock())) {
            int deltaLevel = Math.abs(firstFluid.fluidLevel - secondFluid.fluidLevel);
            if (deltaLevel == 0) {
                return 0.0;
            } else {
                double d = 0.5 * (firstFluid.fluidLevel + secondFluid.fluidLevel);
                double e = blockY + 0.5 - d;
                double f = deltaLevel / 2.0;
                double g = 0.0;
                double h = 2.5;
                double k = 1.5;
                double l = 3.0;
                double m = 10.0;
                double n = 3.0;
                double o = f - Math.abs(e);
                double q;
                if (e > 0.0) {
                    double p = 0.0 + o;
                    if (p > 0.0) {
                        q = p / 1.5;
                    } else {
                        q = p / 2.5;
                    }
                } else {
                    double p = 3.0 + o;
                    if (p > 0.0) {
                        q = p / 3.0;
                    } else {
                        q = p / 10.0;
                    }
                }

                double p = 2.0;
                double r;
                if (!(q < -2.0) && !(q > 2.0)) {
                    double s = substance.getValue();
                    if (Double.isNaN(s)) {
                        double t = this.barrierNoise.compute(context);
                        substance.setValue(t);
                        r = t;
                    } else {
                        r = s;
                    }
                } else {
                    r = 0.0;
                }

                return 2.0 * (r + q);
            }
        } else {
            return 2.0;
        }
    }

    private int gridX(int x) {
        return Math.floorDiv(x, X_SPACING);
    }

    private int gridY(int y) {
        return Math.floorDiv(y, Y_SPACING);
    }

    private int gridZ(int z) {
        return Math.floorDiv(z, Z_SPACING);
    }

    //optimize
    private Aquifer.FluidStatus getAquiferStatus(long packedPos, double substance) {
        int blockX = BlockPos.getX(packedPos);
        int blockY = BlockPos.getY(packedPos);
        int blockZ = BlockPos.getZ(packedPos);
        int gridX = this.gridX(blockX);
        int gridY = this.gridY(blockY);
        int gridZ = this.gridZ(blockZ);
        int aqIndex = this.getAqIndex(gridX, gridY, gridZ);
        Aquifer.FluidStatus fluidStatus = this.aquiferCache[aqIndex];
        if (fluidStatus == null) {
            fluidStatus = this.computeFluid(blockX, blockY, blockZ, substance);
            this.aquiferCache[aqIndex] = fluidStatus;
        }
        return fluidStatus;
    }

    private Aquifer.FluidStatus computeFluid(int blockX, int blockY, int blockZ, double substance) {
        if (!isTargetBiome(blockX, blockY, blockZ)) return new FluidStatus(0, Blocks.AIR.defaultBlockState());

        Aquifer.FluidStatus globalFluid = this.globalFluidPicker.computeFluid(blockX, blockY, blockZ); //water
        int minSurfaceLevel = Integer.MAX_VALUE;
        int above = blockY + Y_SPACING;
        int below = blockY - Y_SPACING;
        boolean hasWaterAboveGround = false;

        for (int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
            int x = blockX + SectionPos.sectionToBlockCoord(is[0]);
            int z = blockZ + SectionPos.sectionToBlockCoord(is[1]);
            int surfaceLevel = this.noiseChunk.preliminarySurfaceLevel(x, z);

            int waterSurface = surfaceLevel + MAX_AQUIFER_H_FROM_GROUND;
            boolean isCenter = is[0] == 0 && is[1] == 0;
            if (isCenter && waterSurface < below) { //cant exist in mid air

                return new Aquifer.FluidStatus(globalFluid.fluidLevel, Blocks.LAPIS_BLOCK.defaultBlockState());
            }

            boolean aboveground = above > waterSurface;
            if (aboveground || isCenter) {
                Aquifer.FluidStatus aboveGroundWater = this.globalFluidPicker.computeFluid(x, waterSurface, z);
                if (!aboveGroundWater.at(waterSurface).isAir()) {
                    if (isCenter) {
                        hasWaterAboveGround = true;
                    }

                    if (aboveground) {
                        return aboveGroundWater;
                    }
                }
            }

            minSurfaceLevel = Math.min(minSurfaceLevel, surfaceLevel);
        }

        int surfaceLevel = this.computeSurfaceLevel(blockX, blockY, blockZ, globalFluid, minSurfaceLevel, hasWaterAboveGround);
        return new Aquifer.FluidStatus(surfaceLevel, this.computeFluidType(blockX, blockY, blockZ, globalFluid, surfaceLevel));
    }

    private int computeSurfaceLevel(int blockX, int blockY, int blockZ, Aquifer.FluidStatus globalFluidStatus,
                                    int minSurfaceLevel, boolean bl) {
        if (true) return globalFluidStatus.fluidLevel; //we always use same level
        DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockX, blockY, blockZ);
        double d;
        double e;
        if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion.compute(singlePointContext), this.depth.compute(singlePointContext))) {
            d = -1.0;
            e = -1.0;
        } else {
            int m = minSurfaceLevel + MAX_AQUIFER_H_FROM_GROUND - blockY;
            int n = 64;
            double f = bl ? Mth.clampedMap(m, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double g = Mth.clamp(this.fluidLevelFloodednessNoise.compute(singlePointContext), -1.0, 1.0);
            double h = Mth.map(f, 1.0, 0.0, -0.3, 0.8);
            double o = Mth.map(f, 1.0, 0.0, -0.8, 0.4);
            d = g - o;
            e = g - h;
        }

        int level;
        if (e > 0.0) {
            level = globalFluidStatus.fluidLevel;
        } else if (d > 0.0) {
            level = this.computeRandomizedFluidSurfaceLevel(blockX, blockY, blockZ, minSurfaceLevel);
        } else {
            level = DimensionType.WAY_BELOW_MIN_Y;
        }

        return level;
    }

    private int computeRandomizedFluidSurfaceLevel(int i, int j, int k, int l) {
        int m = 16;
        int n = 40;
        int o = Math.floorDiv(i, 16);
        int p = Math.floorDiv(j, 40);
        int q = Math.floorDiv(k, 16);
        int r = p * 40 + 20;
        int s = 10;
        double d = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(o, p, q)) * 10.0;
        int t = Mth.quantize(d, 3);
        int u = r + t;
        return Math.min(l, u);
    }

    private BlockState computeFluidType(int i, int j, int k, Aquifer.FluidStatus fluidStatus, int l) {
        BlockState blockState = fluidStatus.fluidType;
        if (l <= -10 && l != DimensionType.WAY_BELOW_MIN_Y && fluidStatus.fluidType != Blocks.LAVA.defaultBlockState()) {
            int m = 64;
            int n = 40;
            int o = Math.floorDiv(i, 64);
            int p = Math.floorDiv(j, 40);
            int q = Math.floorDiv(k, 64);
            double d = this.lavaNoise.compute(new DensityFunction.SinglePointContext(o, p, q));
            if (Math.abs(d) > 0.3) {
                blockState = Blocks.LAVA.defaultBlockState();
            }
        }

        return blockState;
    }

    public void setBiomeSource(MultiNoiseBiomeSource biomeSource) {
        this.biomeSource = biomeSource;
    }

    private boolean isTargetBiome(int x, int y, int z) {
        if (biomeSource != null) {
            DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(x, y, z);
            var t = Climate.target(
                    (float) noiseRouter.temperature().compute(singlePointContext),
                    (float) noiseRouter.vegetation().compute(singlePointContext),
                    (float) noiseRouter.continents().compute(singlePointContext),
                    (float) noiseRouter.erosion().compute(singlePointContext),
                    (float) noiseRouter.depth().compute(singlePointContext),
                    (float) noiseRouter.ridges().compute(singlePointContext));
            var b = biomeSource.getNoiseBiome(t);
            if (b.is(Biomes.SWAMP)) {
                return true;
            }
        }
        return false;
    }

}