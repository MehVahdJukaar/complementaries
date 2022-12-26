package net.mehvahdjukaar.complementaries.reg;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.common.worldgen.SmallSaltLakeFeature;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ModWorldgen {

    public static void init() {

    }

    public static final RegSupplier<Feature<SmallSaltLakeFeature.Configuration>> SALT_LAKE_FEATURE = RegHelper.registerFeature(
            Complementaries.res("salt_lake"), SmallSaltLakeFeature::new);

    public static final RegSupplier<ConfiguredFeature<SmallSaltLakeFeature.Configuration, Feature<SmallSaltLakeFeature.Configuration>>> SALT_FLAT =
            RegHelper.registerConfiguredFeature(Complementaries.res("salt_lake"), () ->
                    new ConfiguredFeature<>(SALT_LAKE_FEATURE.get(),
                            new SmallSaltLakeFeature.Configuration(BlockStateProvider.simple(ModRegistry.HALITE_BLOCK.get().defaultBlockState()),
                                    BlockStateProvider.simple(Blocks.STONE.defaultBlockState()))));


}
