package net.mehvahdjukaar.complementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.complementaries.reg.ModRegistry;
import net.mehvahdjukaar.complementaries.reg.ModWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.function.Predicate;

public class SodicSoilFeature extends Feature<SodicSoilFeature.Config> {

    public SodicSoilFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        int xzSpread = context.config().xzSpread+ 1;
        int ySpread = context.config().ySpread;
        int tries = context.config().tries;
        RandomSource randomSource = context.random();
        BlockPos blockPos = context.origin();
        WorldGenLevel worldGenLevel = context.level();
        int placed = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        // Set<Pair<Integer, Integer>> blacklist = new HashSet<>();
        boolean isEdge = false;
        for (int l = 0; l < tries; ++l) {
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread), 0, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread));
            if(!isEdge && !worldGenLevel.getBiome(mutableBlockPos).is(ModWorldgen.SALT_FLATS.getId())) isEdge = true;
            if (placeAsh(worldGenLevel, ySpread, mutableBlockPos, isEdge, randomSource)) {
                ++placed;
            }
        }

        return placed > 0;
    }

    public boolean placeAsh(WorldGenLevel worldGenLevel, int ySpread, BlockPos origin, boolean flag, RandomSource randomSource) {

        BlockPos.MutableBlockPos pos = origin.mutable();
        int inY = pos.getY();

        boolean success = false;
        int dy = 0;
        BlockState state = worldGenLevel.getBlockState(pos.setY(inY + dy++));
        boolean up = false;
        Predicate<BlockState> replacePredicate = s->{
           var b = s.getBlock();
           return s.is(BlockTags.SAND) || s.is(BlockTags.DIRT) ;
        };
        while (replacePredicate.test(state)&& dy < ySpread) {
            up = true;
            state = worldGenLevel.getBlockState(pos.setY(inY + dy++));
            if (state.isAir()) {
                success = true;
                dy-=1;
                break;
            }
        }
        if(!up) {
            while (state.isAir() && dy > -ySpread) {
                state = worldGenLevel.getBlockState(pos.setY(inY + dy--));
                if (replacePredicate.test(state)) {
                    success = true;
                    dy+=2;
                    break;
                }
            }
        }

        if (success) {
            pos.setY(inY + dy - 1);
            worldGenLevel.setBlock(pos, ModRegistry.SODIC_SOIL.get().defaultBlockState(), 2);
            if(randomSource.nextFloat()<0.01){
                pos.setY(inY + dy);
                worldGenLevel.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 2);
            }
        }
        return success;
    }

    public record Config(int tries, int xzSpread, int ySpread) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(Config::tries),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(Config::xzSpread),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(2).forGetter(Config::ySpread)
        ).apply(instance, Config::new));
    }
}
