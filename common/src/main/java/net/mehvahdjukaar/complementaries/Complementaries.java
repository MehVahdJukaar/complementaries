package net.mehvahdjukaar.complementaries;

import net.mehvahdjukaar.complementaries.common.NetworkHandler;
import net.mehvahdjukaar.complementaries.reg.*;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

/**
 * Author: MehVahdJukaar, Plantkillable
 */
public class Complementaries {

    public static final String MOD_ID = "complementaries";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static void commonInit() {

        NetworkHandler.registerMessages();
        ModRegistry.init();
        ModWorldgen.init();

        PlatformHelper.addServerReloadListener(BiomePointParser.RELOAD_INSTANCE,res("biome_mask"));
    }

    public static void setup() {
        // Given we only add two biomes, we should keep our weight relatively low.
        Regions.register(new ModRegion(res("overworld"), RegionType.OVERWORLD, 10));

        // Register our surface rules
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD,
                MOD_ID, TestSurfaceRuleData.makeRules());
    }

}
