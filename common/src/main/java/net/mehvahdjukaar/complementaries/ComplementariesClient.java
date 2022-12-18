package net.mehvahdjukaar.complementaries;

import net.mehvahdjukaar.complementaries.client.HaliteColor;
import net.mehvahdjukaar.complementaries.client.SaltParticle;
import net.mehvahdjukaar.complementaries.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ComplementariesClient {

    public static void init() {
        ClientPlatformHelper.addBlockColorsRegistration(ComplementariesClient::registerBlockColors);
        ClientPlatformHelper.addParticleRegistration(ComplementariesClient::registerParticles);
    }

    public static void setup() {
        ClientPlatformHelper.registerRenderType(ModRegistry.HALITE_BLOCK.get(), RenderType.cutout());
    }

    @EventCalled
    private static void registerParticles(ClientPlatformHelper.ParticleEvent event) {
        event.register(ModRegistry.SALT_PARTICLE.get(), SaltParticle.Factory::new);

    }

    @EventCalled
    private static void registerBlockColors(ClientPlatformHelper.BlockColorEvent event) {
        event.register(new HaliteColor(), ModRegistry.HALITE_BLOCK.get());
    }

}
