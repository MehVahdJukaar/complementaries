package net.mehvahdjukaar.complementaries.common;

import net.mehvahdjukaar.complementaries.ComplementariesPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import static net.minecraft.world.level.block.Block.dropResources;

public class SaltCrystalItem extends Item {
    public SaltCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        if(state.getMaterial().isReplaceable()){
            if(state.is(Blocks.SNOW)){
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
            pos = pos.below();
        }else{
            Direction dir = context.getClickedFace();
            pos = pos.relative(dir).below();
        }
        var chunk = context.getLevel().getChunk(pos);
        if(chunk instanceof LevelChunk lc){
            ComplementariesPlatformStuff.toggleSalted(lc, pos, true);

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return super.useOn(context);
    }
}
