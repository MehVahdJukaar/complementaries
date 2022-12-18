package net.mehvahdjukaar.complementaries.forge;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.capabilities.ClientBoundSyncAllSaltedMessage;
import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.mehvahdjukaar.complementaries.capabilities.SaltedCoverCapability;
import net.mehvahdjukaar.complementaries.common.NetworkHandler;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Complementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ModCapabilities.register(event);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        ModCapabilities.attachCapabilities(event);
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        var chunk = event.getChunk();
        SaltedCoverCapability cap = ModCapabilities.get(chunk, ModCapabilities.SALTED_CAPABILITY);
        if (cap != null && !cap.getSaltedBlocks().isEmpty()) {
            cap.validateAll();
            var s = cap.getSaltedBlocks();
            if (!s.isEmpty()) {
                NetworkHandler.CHANNEL.sendToClientPlayer(event.getPlayer(),
                        new ClientBoundSyncAllSaltedMessage(chunk.getPos(), cap.getSaltedBlocks()));
            }
        }
    }
}
