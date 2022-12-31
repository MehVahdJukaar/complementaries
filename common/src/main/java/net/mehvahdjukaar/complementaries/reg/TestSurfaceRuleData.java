package net.mehvahdjukaar.complementaries.reg;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.NoOpFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TestSurfaceRuleData
{
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource HALITE = makeStateRule(ModRegistry.HALITE_BLOCK.get());
    private static final SurfaceRules.RuleSource BLUE_TERRACOTTA = makeStateRule(Blocks.BLUE_TERRACOTTA);

    public static SurfaceRules.RuleSource makeRules()
    {
        SurfaceRules.ConditionSource isAtOrAboveWaterLevel = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.RuleSource grassSurface = SurfaceRules.sequence(SurfaceRules.ifTrue(isAtOrAboveWaterLevel, COARSE_DIRT), DIRT);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue( SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(64),0)),
                        SurfaceRules.ifTrue(SurfaceRules.isBiome(ModWorldgen.SALT_FLATS.getHolder().unwrapKey().get()),
                        HALITE)),
              //  SurfaceRules.ifTrue( SurfaceRules.noiseCondition(Noises.SOUL_SAND_LAYER, -0.012),
               //         COARSE_DIRT),
                //SurfaceRules.ifTrue(SurfaceRules.isBiome(TestBiomes.COLD_BLUE), BLUE_TERRACOTTA),

                // Default to a grass and dirt surface
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, grassSurface)

        );
    }

    private static SurfaceRules.RuleSource makeStateRule(Block block)
    {
        return SurfaceRules.state(block.defaultBlockState());
    }

    /*ivate static class SaltFlatSurfaceRule implements SurfaceRules.ConditionSource {
        public static final SaltFlatSurfaceRule INSTANCE = new SaltFlatSurfaceRule();

        static final KeyDispatchDataCodec<SaltFlatSurfaceRule> CODEC = KeyDispatchDataCodec.of(
                Codec.unit(() -> INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return null;
        }

        @Override
        public SurfaceRules.Condition apply(SurfaceRules.Context context) {
            return context.;
        }
    }*/

}
