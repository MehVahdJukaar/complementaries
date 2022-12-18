package net.mehvahdjukaar.complementaries.capabilities;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.complementaries.forge.ClientEvents;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;


public class ClientBoundSyncAllSaltedMessage implements Message {
    public final Set<ChunkLocalPos> saltedPos;
    public final ChunkPos chunkPos;

    public ClientBoundSyncAllSaltedMessage(FriendlyByteBuf buffer) {
        this.chunkPos = buffer.readChunkPos();
        int count = buffer.readInt();
        this.saltedPos = new HashSet<>();
        for (int i = 0; i < count; i++) {
            this.saltedPos.add(ChunkLocalPos.fromBuffer(buffer));
        }
        if(this.saltedPos.size() != count){
            int aa = 1;
        }
    }

    public ClientBoundSyncAllSaltedMessage(ChunkPos chunkPos, Set<ChunkLocalPos> saltedPos) {
        this.saltedPos = saltedPos;
        this.chunkPos = chunkPos;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeChunkPos(chunkPos);
        buffer.writeInt(this.saltedPos.size());
        for (ChunkLocalPos p : this.saltedPos) {
            p.save(buffer);
        }
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        ClientEvents.handleSyncAllSalted(this, context.getSender());
    }
}