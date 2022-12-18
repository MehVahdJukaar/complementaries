package net.mehvahdjukaar.complementaries.common.worldgen;


import net.mehvahdjukaar.complementaries.mixins.AquiferAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
public class Saltifer {
    private boolean shouldScheduleFluidUpdate;

    /**
     * Creates a standard noise based aquifer. This aquifer will place liquid (both water and lava), air, and stone as described above.
     */
    public static Saltifer create(NoiseChunk chunk, AquiferAccessor noiseRouter,
                                  int minY, int height) {
        return new Saltifer(chunk, noiseRouter, minY, height);
    }

    private static final int X_RANGE = 10;
    private static final int Y_RANGE = 9;
    private static final int Z_RANGE = 10;
    private static final int X_SEPARATION = 6;
    private static final int Y_SEPARATION = 3;
    private static final int Z_SEPARATION = 6;
    private static final int X_SPACING = 16;
    private static final int Y_SPACING = 12;
    private static final int Z_SPACING = 16;
    private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
    private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
    private final NoiseChunk noiseChunk;
    private final DensityFunction barrierNoise;
    private final DensityFunction fluidLevelFloodednessNoise;
    private final DensityFunction fluidLevelSpreadNoise;
    private final DensityFunction lavaNoise;
    private final PositionalRandomFactory positionalRandomFactory;
    private final FluidStatus[] aquiferCache;
    private final long[] aquiferLocationCache;
    private final DensityFunction erosion;
    private final DensityFunction depth;
    private final int minGridX;
    private final int minGridY;
    private final int minGridZ;
    private final int gridSizeX;
    private final int gridSizeZ;
    private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};
    FluidStatus gfs = new FluidStatus(-50, Blocks.LAVA.defaultBlockState());

    Saltifer(NoiseChunk noiseChunk, AquiferAccessor aquifer,
             int minY, int height) {
        this.noiseChunk = noiseChunk;
        this.barrierNoise = aquifer.getBarrierNoise();
        this.fluidLevelFloodednessNoise = aquifer.getFluidLevelFloodednessNoise();
        this.fluidLevelSpreadNoise = aquifer.getFluidLevelSpreadNoise();
        this.lavaNoise = aquifer.getLavaNoise();
        this.erosion = aquifer.getErosion();
        this.depth = aquifer.getDepth();
        this.positionalRandomFactory = aquifer.getPositionalRandomFactory();
        this.minGridX = aquifer.minGridX();

        this.gridSizeX = aquifer.gridSizeX();
        this.minGridY = this.gridY(minY) - 1;
        int l = this.gridY(minY + height) + 1;
        int m = l - this.minGridY + 1;
        this.minGridZ = aquifer.minGridZ();
        this.gridSizeZ = aquifer.gridSizeZ();
        int o = this.gridSizeX * m * this.gridSizeZ;
        this.aquiferCache = new FluidStatus[o];
        this.aquiferLocationCache = new long[o];
        Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
    }

    /**
     * @return A cache index based on grid positions.
     */
    private int getIndex(int gridX, int gridY, int gridZ) {
        int i = gridX - this.minGridX;
        int j = gridY - this.minGridY;
        int k = gridZ - this.minGridZ;
        return (j * this.gridSizeZ + k) * this.gridSizeX + i;
    }

    @Nullable
    public BlockState computeSubstance(DensityFunction.FunctionContext context, double substance) {
        Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(64, Blocks.WATER.defaultBlockState());
        FluidPicker fp = (j, k, l) -> k < Math.min(-54, 64) ? fluidStatus : fluidStatus2;
        int blockX = context.blockX();
        int blockY = context.blockY();
        int blockZ = context.blockZ();

        if (substance > 0.0) return null;
        int fX = Math.floorDiv(blockX - 5, 16);
        int fY = Math.floorDiv(blockY + 1, 12);
        int fZ = Math.floorDiv(blockZ - 5, 16);
        int o = Integer.MAX_VALUE;
        int p = Integer.MAX_VALUE;
        int q = Integer.MAX_VALUE;
        long r = 0L;
        long s = 0L;
        long t = 0L;

        for (int u = 0; u <= 1; ++u) {
            for (int v = -1; v <= 1; ++v) {
                for (int w = 0; w <= 1; ++w) {
                    int x = fX + u;
                    int y = fY + v;
                    int z = fZ + w;
                    int aa = this.getIndex(x, y, z);
                    long ab = this.aquiferLocationCache[aa];
                    long ac;
                    if (ab != Long.MAX_VALUE) {
                        ac = ab;
                    } else {
                        RandomSource randomSource = this.positionalRandomFactory.at(x, y, z);
                        ac = BlockPos.asLong(x * 16 + randomSource.nextInt(10), y * 12 + randomSource.nextInt(9), z * 16 + randomSource.nextInt(10));
                        this.aquiferLocationCache[aa] = ac;
                    }

                    int ad = BlockPos.getX(ac) - blockX;
                    int ae = BlockPos.getY(ac) - blockY;
                    int af = BlockPos.getZ(ac) - blockZ;
                    int ag = ad * ad + ae * ae + af * af;
                    if (o >= ag) {
                        t = s;
                        s = r;
                        r = ac;
                        q = p;
                        p = o;
                        o = ag;
                    } else if (p >= ag) {
                        t = s;
                        s = ac;
                        q = p;
                        p = ag;
                    } else if (q >= ag) {
                        t = ac;
                        q = ag;
                    }
                }
            }
        }

        FluidStatus fluidStatus2 = this.getAquiferStatus(r);
        double d = similarity(o, p);
        BlockState blockState = fluidStatus2.at(blockY);
        if (d <= 0.0) {
            this.shouldScheduleFluidUpdate = d >= FLOWING_UPDATE_SIMULARITY;
            return blockState;
        } else {
            MutableDouble mutableDouble = new MutableDouble(Double.NaN);
            FluidStatus fluidStatus3 = this.getAquiferStatus(s);
            double e = d * this.calculatePressure(context, mutableDouble, fluidStatus2, fluidStatus3);
            if (substance + e > 0.0) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            } else {
                FluidStatus fluidStatus4 = this.getAquiferStatus(t);
                double f = similarity(o, q);
                double g;
                if (f > 0.0) {
                    g = d * f * this.calculatePressure(context, mutableDouble, fluidStatus2, fluidStatus4);
                    if (substance + g > 0.0) {
                        this.shouldScheduleFluidUpdate = false;
                        return null;
                    }
                }

                g = similarity(p, q);
                if (g > 0.0) {
                    double h = d * g * this.calculatePressure(context, mutableDouble, fluidStatus3, fluidStatus4);
                    if (substance + h > 0.0) {
                        this.shouldScheduleFluidUpdate = false;
                        return null;
                    }
                }

                this.shouldScheduleFluidUpdate = true;
                return blockState;
            }
        }
    }

    /**
     * Compares two distances (between aquifers).
     *
     * @return {@code 1.0} if the distances are equal, and returns smaller values the more different in absolute value the two distances are.
     */
    private static double similarity(int firstDistance, int secondDistance) {
        double d = 25.0;
        return 1.0 - (double) Math.abs(secondDistance - firstDistance) / 25.0;
    }

    private double calculatePressure(DensityFunction.FunctionContext context, MutableDouble substance, FluidStatus firstFluid, FluidStatus secondFluid) {
        int i = context.blockY();
        BlockState blockState = firstFluid.at(i);
        BlockState blockState2 = secondFluid.at(i);
        if ((!blockState.is(Blocks.LAVA) || !blockState2.is(Blocks.WATER)) && (!blockState.is(Blocks.WATER) || !blockState2.is(Blocks.LAVA))) {
            int j = Math.abs(firstFluid.fluidLevel - secondFluid.fluidLevel);
            if (j == 0) {
                return 0.0;
            } else {
                double d = 0.5 * (double) (firstFluid.fluidLevel + secondFluid.fluidLevel);
                double e = (double) i + 0.5 - d;
                double f = (double) j / 2.0;
                double g = 0.0;
                double h = 2.5;
                double k = 1.5;
                double l = 3.0;
                double m = 10.0;
                double n = 3.0;
                double o = f - Math.abs(e);
                double q;
                double p;
                if (e > 0.0) {
                    p = 0.0 + o;
                    if (p > 0.0) {
                        q = p / 1.5;
                    } else {
                        q = p / 2.5;
                    }
                } else {
                    p = 3.0 + o;
                    if (p > 0.0) {
                        q = p / 3.0;
                    } else {
                        q = p / 10.0;
                    }
                }

                p = 2.0;
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
        return Math.floorDiv(x, 16);
    }

    private int gridY(int y) {
        return Math.floorDiv(y, 12);
    }

    private int gridZ(int z) {
        return Math.floorDiv(z, 16);
    }

    /**
     * Calculates the aquifer at a given location. Internally references a cache using the grid positions as an index. If the cache is not populated, computes a new aquifer at that grid location using {@link #computeFluid.
     *
     * @param packedPos The aquifer block position, packed into a {@code long}.
     */
    private FluidStatus getAquiferStatus(long packedPos) {
        int i = BlockPos.getX(packedPos);
        int j = BlockPos.getY(packedPos);
        int k = BlockPos.getZ(packedPos);
        int l = this.gridX(i);
        int m = this.gridY(j);
        int n = this.gridZ(k);
        int o = this.getIndex(l, m, n);
        FluidStatus fluidStatus = this.aquiferCache[o];
        if (fluidStatus != null) {
            return fluidStatus;
        } else {
            FluidStatus fluidStatus2 = this.computeFluid(i, j, k);
            this.aquiferCache[o] = fluidStatus2;
            return fluidStatus2;
        }
    }

    private FluidStatus computeFluid(int x, int y, int z) {
        FluidStatus fluidStatus = gfs;
        int i = Integer.MAX_VALUE;
        int j = y + 12;
        int k = y - 12;
        boolean bl = false;
        int[][] var9 = SURFACE_SAMPLING_OFFSETS_IN_CHUNKS;
        int var10 = var9.length;

        for (int var11 = 0; var11 < var10; ++var11) {
            int[] is = var9[var11];
            int l = x + SectionPos.sectionToBlockCoord(is[0]);
            int m = z + SectionPos.sectionToBlockCoord(is[1]);
            int n = this.noiseChunk.preliminarySurfaceLevel(l, m);
            int o = n + 8;
            boolean bl2 = is[0] == 0 && is[1] == 0;
            if (bl2 && k > o) {
                return fluidStatus;
            }

            boolean bl3 = j > o;
            if (bl3 || bl2) {
                FluidStatus fluidStatus2 = gfs;
                if (!fluidStatus2.at(o).isAir()) {
                    if (bl2) {
                        bl = true;
                    }

                    if (bl3) {
                        return fluidStatus2;
                    }
                }
            }

            i = Math.min(i, n);
        }

        int p = this.computeSurfaceLevel(x, y, z, fluidStatus, i, bl);
        return new FluidStatus(p, this.computeFluidType(x, y, z, fluidStatus, p));

    }

    private int computeSurfaceLevel(int i, int j, int k, FluidStatus fluidStatus, int l, boolean bl) {
        DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(i, j, k);
        double d;
        double e;
        if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion.compute(singlePointContext), this.depth.compute(singlePointContext))) {
            d = -1.0;
            e = -1.0;
        } else {
            int m = l + 8 - j;
            int n = 64;
            double f = bl ? Mth.clampedMap((double) m, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double g = Mth.clamp(this.fluidLevelFloodednessNoise.compute(singlePointContext), -1.0, 1.0);
            double h = Mth.map(f, 1.0, 0.0, -0.3, 0.8);
            double o = Mth.map(f, 1.0, 0.0, -0.8, 0.4);
            d = g - o;
            e = g - h;
        }

        int m;
        if (e > 0.0) {
            m = fluidStatus.fluidLevel;
        } else if (d > 0.0) {
            m = this.computeRandomizedFluidSurfaceLevel(i, j, k, l);
        } else {
            m = DimensionType.WAY_BELOW_MIN_Y;
        }

        return m;
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

    private BlockState computeFluidType(int i, int j, int k, FluidStatus fluidStatus, int l) {
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

    public interface FluidPicker {
        net.minecraft.world.level.levelgen.Aquifer.FluidStatus computeFluid(int i, int j, int k);
    }

    public static final class FluidStatus {
        /**
         * The y height of the aquifer.
         */
        final int fluidLevel;
        /**
         * The fluid state the aquifer is filled with.
         */
        final BlockState fluidType;

        public FluidStatus(int i, BlockState blockState) {
            this.fluidLevel = i;
            this.fluidType = blockState;
        }

        public BlockState at(int y) {
            return y < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
        }
    }
}

