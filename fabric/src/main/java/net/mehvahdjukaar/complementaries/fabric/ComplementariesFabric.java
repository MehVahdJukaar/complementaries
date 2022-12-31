package net.mehvahdjukaar.complementaries.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.ComplementariesClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;

public class ComplementariesFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        Complementaries.commonInit();

        if (PlatformHelper.getEnv().isClient()) {
            FabricSetupCallbacks.CLIENT_SETUP.add(ComplementariesClient::init);
        }
        FabricSetupCallbacks.COMMON_SETUP.add(Complementaries::setup);
        RegHelperImpl.finishRegistration(Complementaries.MOD_ID);
    }
}
