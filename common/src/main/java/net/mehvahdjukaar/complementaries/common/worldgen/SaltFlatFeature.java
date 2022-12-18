package net.mehvahdjukaar.complementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Material;

import java.util.Arrays;

public class SaltFlatFeature extends Feature<SaltFlatFeature.Configuration> {
    private static final BlockState AIR;

    public SaltFlatFeature() {
        super(Configuration.CODEC);
    }

    public void setArray(Boolean[] array, int x, int y, int z, boolean value) {
        array[(x * 16 + z) * 8 + y] = value;
    }

    public boolean getArray(Boolean[] array, int x, int y, int z) {
        return array[(x * 16 + z) * 8 + y];
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        try {

            BlockPos blockPos = context.origin();
            WorldGenLevel worldGenLevel = context.level();
            RandomSource randomSource = context.random();
            Configuration configuration = context.config();
            if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 4) {
                return false;
            } else {
                blockPos = blockPos.below(4);
                Boolean[] positions = new Boolean[2048];
                Arrays.fill(positions, false);
                int iterations = randomSource.nextInt(4) + 4;
iterations = 1;
                for (int j = 0; j < iterations; ++j) {
                    double dx = randomSource.nextDouble() * 6.0 + 3.0;
                    double dy = randomSource.nextDouble() * 4.0 + 2.0;
                    double dz = randomSource.nextDouble() * 6.0 + 3.0;
                    double hx = dx / 2.0;
                    double maxX = randomSource.nextDouble() * (16.0 - dx - 2.0) + 1.0 + hx;
                    double hy = dy / 2.0;
                    double maxY = randomSource.nextDouble() * (8.0 - dy - 4.0) + 2.0 + hy;
                    double hz = dz / 2.0;
                    double maxZ = randomSource.nextDouble() * (16.0 - dz - 2.0) + 1.0 + hz;

                    for (int px = 1; px < 15; ++px) {
                        for (int pz = 1; pz < 15; ++pz) {
                            for (int py = 1; py < 7; ++py) {
                                double rx = (px - maxX) / (hx);
                                double ry = (py - maxY) / (hy);
                                double rz = (pz - maxZ) / (hz);
                                double r = rx * rx + ry * ry + rz * rz;
                                if (r < 1.0) {
                                    setArray(positions, px, py, pz, true);
                                }
                            }
                        }
                    }
                }

                BlockState blockState = configuration.fluid().getState(randomSource, blockPos);

                //validate
                for (int s = 0; s < 16; ++s) {
                    for (int t = 0; t < 16; ++t) {
                        for (int u = 0; u < 8; ++u) {
                            boolean bl = !positions[(s * 16 + t) * 8 + u]
                                    && (
                                    s < 15 && positions[((s + 1) * 16 + t) * 8 + u]
                                            || s > 0 && positions[((s - 1) * 16 + t) * 8 + u]
                                            || t < 15 && positions[(s * 16 + t + 1) * 8 + u]
                                            || t > 0 && positions[(s * 16 + (t - 1)) * 8 + u]
                                            || u < 7 && positions[(s * 16 + t) * 8 + u + 1]
                                            || u > 0 && positions[(s * 16 + t) * 8 + (u - 1)]
                            );
                            if (bl) {
                                Material material = worldGenLevel.getBlockState(blockPos.offset(s, u, t)).getMaterial();
                                if (u >= 4 && material.isLiquid()) {
                                    return false;
                                }

                                if (u < 4 && !material.isSolid() && worldGenLevel.getBlockState(blockPos.offset(s, u, t)) != blockState) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                for (int s = 0; s < 16; ++s) {
                    for (int t = 0; t < 16; ++t) {
                        for (int u = 0; u < 8; ++u) {
                            if (positions[(s * 16 + t) * 8 + u]) {
                                BlockPos blockPos2 = blockPos.offset(s, u, t);
                                if (this.canReplaceBlock(worldGenLevel.getBlockState(blockPos2))) {
                                    boolean bl2 = u >= 4;
                                    worldGenLevel.setBlock(blockPos2, bl2 ? AIR : blockState, 2);
                                    if (bl2) {
                                        worldGenLevel.scheduleTick(blockPos2, AIR.getBlock(), 0);
                                        this.markAboveForPostProcessing(worldGenLevel, blockPos2);
                                    }
                                }
                            }
                        }
                    }
                }
if(true)return true;
                BlockState blockState2 = configuration.barrier().getState(randomSource, blockPos);
                if (!blockState2.isAir()) {
                    for (int t = 0; t < 16; ++t) {
                        for (int u = 0; u < 16; ++u) {
                            for (int v = 0; v < 8; ++v) {
                                boolean bl2 = !positions[(t * 16 + u) * 8 + v]
                                        && (
                                        t < 15 && positions[((t + 1) * 16 + u) * 8 + v]
                                                || t > 0 && positions[((t - 1) * 16 + u) * 8 + v]
                                                || u < 15 && positions[(t * 16 + u + 1) * 8 + v]
                                                || u > 0 && positions[(t * 16 + (u - 1)) * 8 + v]
                                                || v < 7 && positions[(t * 16 + u) * 8 + v + 1]
                                                || v > 0 && positions[(t * 16 + u) * 8 + (v - 1)]
                                );
                                if (bl2 && (v < 4 || randomSource.nextInt(2) != 0)) {
                                    BlockState blockState3 = worldGenLevel.getBlockState(blockPos.offset(t, v, u));
                                    if (blockState3.getMaterial().isSolid() && !blockState3.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                                        BlockPos blockPos3 = blockPos.offset(t, v, u);
                                        worldGenLevel.setBlock(blockPos3, blockState2, 2);
                                        this.markAboveForPostProcessing(worldGenLevel, blockPos3);
                                    }
                                }
                            }
                        }
                    }
                }

                if (blockState.getFluidState().is(FluidTags.WATER)) {
                    for (int t = 0; t < 16; ++t) {
                        for (int u = 0; u < 16; ++u) {
                            int v = 4;
                            BlockPos blockPos4 = blockPos.offset(t, 4, u);
                            if ((worldGenLevel.getBiome(blockPos4).value()).shouldFreeze(worldGenLevel, blockPos4, false)
                                    && this.canReplaceBlock(worldGenLevel.getBlockState(blockPos4))) {
                                worldGenLevel.setBlock(blockPos4, Blocks.ICE.defaultBlockState(), 2);
                            }
                        }
                    }
                }

                return true;
            }
        }catch (Exception e){
            return false;
        }
    }

    private boolean canReplaceBlock(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    static {
        AIR = Blocks.CAVE_AIR.defaultBlockState();
    }

    public record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        BlockStateProvider.CODEC.fieldOf("fluid").forGetter(Configuration::fluid),
                        BlockStateProvider.CODEC.fieldOf("barrier").forGetter(Configuration::barrier)
                ).apply(instance, Configuration::new));
    }
}
