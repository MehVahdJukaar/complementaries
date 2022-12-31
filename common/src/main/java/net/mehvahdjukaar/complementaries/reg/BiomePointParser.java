package net.mehvahdjukaar.complementaries.reg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiomePointParser extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final BiomePointParser RELOAD_INSTANCE = new BiomePointParser();

    private Map<String,  List<Climate.ParameterPoint>> points = new HashMap<>();

    public BiomePointParser() {
        super(GSON, "biome_mask");
    }


    public Map<String, List<Climate.ParameterPoint>> getPoints() {
        return points;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        jsons.forEach((key, json) -> {
            var j = json.getAsJsonObject().get("generator")
                    .getAsJsonObject().get("biome_source").getAsJsonObject().get("biomes").getAsJsonArray();
            points.clear();
            for(var v : j){
                var p = Climate.ParameterPoint.CODEC
                        .decode(JsonOps.INSTANCE, v.getAsJsonObject().get("parameters"));
                String s = v.getAsJsonObject().get("biome").getAsString();
                points.computeIfAbsent(s, a->new ArrayList<>()).add(p.get().left().get().getFirst());
            }

        });
    }

}
