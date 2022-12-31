package net.mehvahdjukaar.complementaries.reg;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.worldgen.noise.LayeredNoiseUtil;

import java.util.function.Consumer;

public class ModRegion extends Region {

    public ModRegion(ResourceLocation name, RegionType type, int weight) {
        super(name, type, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        var key = ModWorldgen.SALT_FLATS.getHolder().unwrapKey().get();
        ResourceKey<Biome> biomeKey = key;

        //map of params and their inverse
        var snowcappedParams = BiomePointParser.RELOAD_INSTANCE.getPoints();

        if(false) {
            var nonInverse = snowcappedParams.values().stream().findFirst().get();

            var b = new VanillaParameterOverlayBuilder();

            nonInverse.forEach(k -> b.add(k, biomeKey));
            var newPoints = b.build();
            var json = new JsonArray();
            for (var hh : newPoints) {
                addBiome(mapper, hh.getFirst(), hh.getSecond());
                JsonObject jo = new JsonObject();
                jo.add("parameters", Climate.ParameterPoint.CODEC.encodeStart(JsonOps.INSTANCE,hh.getFirst())
                        .get().orThrow());
                jo.addProperty("biome",hh.getSecond().location().toString());
                json.add(jo);
            }
            String ssss = json.toString();
            //second approach
        }else {
            for (var v : snowcappedParams.entrySet()) {
                for (var p : v.getValue()) {
                         addBiome(mapper, p, key);
                }
                  key = DEFERRED_PLACEHOLDER;
            }
        }
        /*
        Set<Climate.ParameterPoint> points = new HashSet<>(new ParameterPointListBuilder()
                .temperature(Temperature.WARM, Temperature.HOT)
                .humidity(Humidity.ARID, Humidity.DRY, Humidity.NEUTRAL)
                .continentalness(Continentalness.FAR_INLAND)
                .erosion(Erosion.EROSION_4, Erosion.EROSION_5, Erosion.EROSION_6)
                .depth(Depth.SURFACE, Depth.FLOOR)
                .weirdness(
                        Weirdness.MID_SLICE_NORMAL_ASCENDING,
                        Weirdness.MID_SLICE_NORMAL_DESCENDING,
                        Weirdness.LOW_SLICE_NORMAL_DESCENDING,
                        Weirdness.VALLEY,
                        Weirdness.LOW_SLICE_VARIANT_ASCENDING,
                        Weirdness.MID_SLICE_VARIANT_ASCENDING,
                        Weirdness.MID_SLICE_VARIANT_DESCENDING
                )
                .build());
        Set<Climate.ParameterPoint> all = new HashSet<>(new ParameterPointListBuilder()
                .temperature(Temperature.FULL_RANGE)
                .humidity(Humidity.FULL_RANGE)
                .continentalness(Continentalness.FULL_RANGE)
                .erosion(Erosion.FULL_RANGE)
                .depth(Depth.FULL_RANGE)
                .weirdness(Weirdness.FULL_RANGE)
                .build());

        for (var p : all) {
            if (points.contains(p)) {
             //   addBiome(mapper, p, key);
            } else {
              //  addBiome(mapper, p, DEFERRED_PLACEHOLDER);
            }
        }
        ModifiedVanillaOverworldBuilder builder = new ModifiedVanillaOverworldBuilder();
        //onModify.accept(builder);
      //  builder.build().forEach(mapper::accept);

        for (long t = Temperature.FULL_RANGE.parameter().min(); t < Temperature.FULL_RANGE.parameter().max(); t++) {
            for (long a = Temperature.FULL_RANGE.parameter().min(); a < Temperature.FULL_RANGE.parameter().max(); a++) {

            }
        }
        */
    }

}
