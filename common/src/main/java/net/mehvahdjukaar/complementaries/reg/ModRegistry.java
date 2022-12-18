package net.mehvahdjukaar.complementaries.reg;

import net.mehvahdjukaar.complementaries.common.HaliteFormation;
import net.mehvahdjukaar.complementaries.common.SaltCrystalItem;
import net.mehvahdjukaar.complementaries.common.SaltLampBlock;
import net.mehvahdjukaar.complementaries.common.SaltLanternBlock;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static net.mehvahdjukaar.complementaries.Complementaries.res;

public class ModRegistry {

    public static void init() {

    }

    public static final Supplier<SimpleParticleType> SALT_PARTICLE = regParticle("salt");

    public static final Supplier<Block> HALITE_BLOCK = regWithItem("halite",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<Block> SALT_LANTERN = regWithItem("salt_lantern",
            () -> new SaltLanternBlock(5, 8, BlockBehaviour.Properties.copy(Blocks.CALCITE)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<Block> HALITE_FORMATION = regWithItem("halite_formation",
            () -> new HaliteFormation(5, 8, BlockBehaviour.Properties.copy(Blocks.CALCITE)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<Block> SALT_BRICKS = regWithItem("salt_brick",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CALCITE)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<Block> SALT_LAMP = regWithItem("salt_lamp",
            () -> new SaltLampBlock(BlockBehaviour.Properties.copy(Blocks.CALCITE)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<Item> SALT_ITEM = regItem("salt",
            () -> new SaltCrystalItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static CreativeModeTab getTab(CreativeModeTab g, String regName) {
        return g;
    }


    private static Supplier<SimpleParticleType> regParticle(String string) {
        return RegHelper.registerParticle(res(string));
    }

    public static <T extends Item> Supplier<T> regItem(String name, Supplier<T> sup) {
        return RegHelper.registerItem(res(name), sup);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> regTile(String name, Supplier<T> sup) {
        return RegHelper.registerBlockEntityType(res(name), sup);
    }

    public static <T extends Block> Supplier<T> regBlock(String name, Supplier<T> sup) {
        return RegHelper.registerBlock(res(name), sup);
    }


    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, CreativeModeTab tab) {
        return regWithItem(name, blockFactory, new Item.Properties().tab(getTab(tab, name)), 0);
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, CreativeModeTab tab, int burnTime) {
        return regWithItem(name, blockFactory, new Item.Properties().tab(getTab(tab, name)), burnTime);
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, Item.Properties properties, int burnTime) {
        Supplier<T> block = regBlock(name, blockFactory);
        regBlockItem(name, block, properties, burnTime);
        return block;
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> block, CreativeModeTab tab, String requiredMod) {
        CreativeModeTab t = PlatformHelper.isModLoaded(requiredMod) ? tab : null;
        return regWithItem(name, block, t);
    }

    public static Supplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties, int burnTime) {
        return RegHelper.registerItem(res(name), () -> burnTime == 0 ? new BlockItem(blockSup.get(), properties) :
                new WoodBasedBlockItem(blockSup.get(), properties, burnTime));
    }

    public static Supplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties) {
        return regBlockItem(name, blockSup, properties, 0);
    }
}
