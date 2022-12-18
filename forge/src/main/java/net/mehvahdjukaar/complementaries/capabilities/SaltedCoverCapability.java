package net.mehvahdjukaar.complementaries.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

//provider & instance. Only one instance is attached to a world at a time
public class SaltedCoverCapability implements ICapabilitySerializable<CompoundTag> {

    private final LazyOptional<SaltedCoverCapability> lazyOptional = LazyOptional.of(() -> this);

    private final Set<ChunkLocalPos> saltedBlocks = new HashSet<>();
    private final LevelChunk chunk;


    SaltedCoverCapability(LevelChunk chunk) {
        this.chunk = chunk;
    }

    public void invalidate() {
        lazyOptional.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == ModCapabilities.SALTED_CAPABILITY ?
                lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag total = new CompoundTag();
        var l = new ListTag();
        saltedBlocks.forEach(s -> l.add(s.save()));
        total.put("SaltPos", l);
        return total;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        var l = tag.getList("SaltPos", 10);

        for (var v : l) {
            saltedBlocks.add(ChunkLocalPos.fromTag((CompoundTag) v));
        }
    }

    public void setSalted(BlockPos pos, boolean salted) {
        ChunkLocalPos c = getChunkPos(pos);
        if (salted) saltedBlocks.add(c);
        else saltedBlocks.remove(c);
    }

    @NotNull
    private static ChunkLocalPos getChunkPos(BlockPos pos) {
        return new ChunkLocalPos((byte) (pos.getX() & 15), (short) pos.getY(), (byte) (pos.getZ() & 15));
    }

    public Set<ChunkLocalPos> getSaltedBlocks() {
        return saltedBlocks;
    }

    public boolean isSalted(BlockPos pos) {
        if (saltedBlocks.isEmpty()) return false;
        ChunkLocalPos c = getChunkPos(pos);
        boolean result = this.saltedBlocks.contains(c);
        if (result) {
            result = this.validate(pos);
            if (!result) {
                saltedBlocks.remove(c);
                int aaa = 1;
            }
        }
        return result;
    }

    public void validateAll() {
        saltedBlocks.removeIf(s -> !this.validate(s.toPos(chunk.getPos())));
    }

    private boolean validate(BlockPos pos) {
        BlockState state = chunk.getBlockState(pos);
        return !state.isAir() && Block.isFaceFull(state.getCollisionShape(chunk, pos), Direction.UP);
    }

    public void setAllSalted(Set<ChunkLocalPos> saltedPos) {
        this.saltedBlocks.clear();
        this.saltedBlocks.addAll(saltedPos);
    }
}