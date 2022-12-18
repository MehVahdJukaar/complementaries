package net.mehvahdjukaar.complementaries.capabilities;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;

public class ModCapabilities {


    public static final Capability<SaltedCoverCapability> SALTED_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(SaltedCoverCapability.class);
    }

    public static void attachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        SaltedCoverCapability capability = new SaltedCoverCapability(event.getObject());
        event.addCapability(Complementaries.res("salt_cover"), capability);
        event.addListener(capability::invalidate);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static <T> T get(ICapabilityProvider provider, Capability<T> cap){
        return provider.getCapability(cap).orElse(null);
    }

}