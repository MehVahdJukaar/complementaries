package net.mehvahdjukaar.complementaries;

import net.mehvahdjukaar.complementaries.common.NetworkHandler;
import net.mehvahdjukaar.complementaries.common.SaltCrystalItem;
import net.mehvahdjukaar.complementaries.common.SaltLampBlock;
import net.mehvahdjukaar.complementaries.reg.ModRegistry;
import net.mehvahdjukaar.complementaries.reg.ModWorldgen;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

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
        ModWorldgen .init();
    }


}
