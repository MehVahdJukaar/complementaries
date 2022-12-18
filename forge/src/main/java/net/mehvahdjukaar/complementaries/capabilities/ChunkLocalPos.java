package net.mehvahdjukaar.complementaries.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.concurrent.Immutable;

@Unmodifiable
@Immutable
public class ChunkLocalPos implements Comparable<ChunkLocalPos> {

    private final byte x;
    private final short y;
    private final byte z;

    public ChunkLocalPos(byte x, short y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static ChunkLocalPos fromTag(CompoundTag tag) {
        return new ChunkLocalPos(tag.getByte("X"), tag.getShort("Y"), tag.getByte("Z"));
    }

    public static ChunkLocalPos fromBuffer(FriendlyByteBuf buff) {
       // int i = buff.readInt();
       // return new ChunkLocalPos((byte) (i&255), (short) (i>>8 & 65535), (byte) (i >> 24 &255));
        return new ChunkLocalPos(buff.readByte(), buff.readShort(), buff.readByte());
    }

    public void save(FriendlyByteBuf buff) {
       // int i =  z << 24 | y << 8 | x;
       // buff.writeInt(i);
        buff.writeByte(x);
        buff.writeShort(y);
        buff.writeByte(z);
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putByte("X", this.getX());
        compoundTag.putShort("Y", this.getY());
        compoundTag.putByte("Z", this.getZ());
        return compoundTag;
    }

    public byte getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public byte getZ() {
        return z;
    }

    @Override
    public int compareTo(ChunkLocalPos other) {
        if (this.getY() == other.getY()) {
            return this.getZ() == other.getZ() ? this.getX() - other.getX() : this.getZ() - other.getZ();
        } else {
            return this.getY() - other.getY();
        }
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ChunkLocalPos vec3i)) {
            return false;
        } else {
            if (this.getX() != vec3i.getX()) {
                return false;
            } else if (this.getY() != vec3i.getY()) {
                return false;
            } else {
                return this.getZ() == vec3i.getZ();
            }
        }
    }
    @Override
    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public BlockPos toPos(ChunkPos pos) {
        return new BlockPos(pos.getBlockX(x), y,pos.getBlockZ(z));
    }
}
