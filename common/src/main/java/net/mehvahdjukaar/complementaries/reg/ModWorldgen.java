package net.mehvahdjukaar.complementaries.reg;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.common.worldgen.SmallSaltLakeFeature;
import net.mehvahdjukaar.complementaries.common.worldgen.SodicSoilFeature;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;
import java.util.function.Supplier;

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


    public static final Supplier<Feature<SodicSoilFeature.Config>> SODIC_SOILD_FEATURE = RegHelper.registerFeature(
            Complementaries.res("layered_blocks"), () -> new SodicSoilFeature(SodicSoilFeature.Config.CODEC));
    public static final RegSupplier<ConfiguredFeature<SodicSoilFeature.Config, Feature<SodicSoilFeature.Config>>> SODIC_SOIL_PATCH =
            RegHelper.registerConfiguredFeature(Complementaries.res("sodic_soil"),
                    () -> new ConfiguredFeature<>(SODIC_SOILD_FEATURE.get(),
                            new SodicSoilFeature.Config(120,8, 4)));

    public static final RegSupplier<PlacedFeature> PLACED_SODIC_SOIL =
            RegHelper.registerPlacedFeature(Complementaries.res("sodic_soil"),
                    SODIC_SOIL_PATCH,
                    () -> List.of(
                            HeightRangePlacement.uniform(VerticalAnchor.absolute(62), VerticalAnchor.absolute(66)),
                            CountPlacement.of(11),
                            InSquarePlacement.spread(),
                            BiomeFilter.biome()));

}
