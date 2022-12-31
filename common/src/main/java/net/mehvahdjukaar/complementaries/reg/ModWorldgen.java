package net.mehvahdjukaar.complementaries.reg;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.common.worldgen.SmallSaltLakeFeature;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
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

    protected static int calculateSkyColor(float f) {
        float g = f / 3.0F;
        g = Mth.clamp(g, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - g * 0.05F, 0.5F + g * 0.1F, 1.0F);
    }

    public static final RegSupplier<Biome> SALT_FLATS = RegHelper.register(
            Complementaries.res("salt_flats"), () -> new Biome.BiomeBuilder()
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1)
                    .downfall(0)
                    .specialEffects(new BiomeSpecialEffects.Builder()
                            .waterColor(4159204)
                            .waterFogColor(329011)
                            .fogColor(12638463)
                            .skyColor(calculateSkyColor(2.0F))
                            .build()
                    )
                    .generationSettings(new BiomeGenerationSettings.Builder()
                            .build()
                    )
                    .mobSpawnSettings(new MobSpawnSettings.Builder()
                            .build()
                    )
                    .build(),
            BuiltinRegistries.BIOME
    );


}
