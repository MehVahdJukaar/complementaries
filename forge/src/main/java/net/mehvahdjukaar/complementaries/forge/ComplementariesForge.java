package net.mehvahdjukaar.complementaries.forge;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.ComplementariesClient;
import net.mehvahdjukaar.complementaries.capabilities.ClientBoundSyncAllSaltedMessage;
import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.mehvahdjukaar.complementaries.common.NetworkHandler;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(Complementaries.MOD_ID)
public class ComplementariesForge {

    public ComplementariesForge() {
        Complementaries.commonInit();

        if (PlatformHelper.getEnv().isClient()) {
            ComplementariesClient.init();
        }

        NetworkHandler.CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncAllSaltedMessage.class,
                ClientBoundSyncAllSaltedMessage::new);
    }



}

