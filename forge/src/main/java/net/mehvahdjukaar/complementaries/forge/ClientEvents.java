package net.mehvahdjukaar.complementaries.forge;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.capabilities.ClientBoundSyncAllSaltedMessage;
import net.mehvahdjukaar.complementaries.capabilities.ModCapabilities;
import net.mehvahdjukaar.complementaries.capabilities.SaltedCoverCapability;
import net.mehvahdjukaar.complementaries.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Complementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void renderSaltParticles(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && !Minecraft.getInstance().isPaused()) {
            var pl = Minecraft.getInstance().player;
            if (pl != null && pl.getItemInHand(InteractionHand.MAIN_HAND).is(ModRegistry.SALT_ITEM.get())) {
                Level level = pl.getLevel();
                int posX = pl.getBlockX();
                int posY = pl.getBlockY();
                int posZ = pl.getBlockZ();

                Map<LevelChunk, SaltedCoverCapability> map = new IdentityHashMap<>();
                pl.level.getChunkAt(pl.getOnPos());

                RandomSource randomsource = RandomSource.create();
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

                for (int j = 0; j < 667; ++j) {
                    doAnimateTick(level, posX, posY, posZ, 16, randomsource, map, mutableBlockPos);
                    doAnimateTick(level, posX, posY, posZ, 32, randomsource, map, mutableBlockPos);
                }
            }
        }
    }

    //TODO: optimize
    public static void doAnimateTick(Level level, int posX, int posY, int posZ, int range, RandomSource random,
                                     Map<LevelChunk, SaltedCoverCapability> map, BlockPos.MutableBlockPos blockPos) {
        int i = posX + random.nextInt(range) - random.nextInt(range);
        int j = posY + random.nextInt(range) - random.nextInt(range);
        int k = posZ + random.nextInt(range) - random.nextInt(range);
        blockPos.set(i, j, k);
        var c = level.getChunkAt(blockPos);
        var cap = map.computeIfAbsent(c, chunk -> ModCapabilities.get(chunk, ModCapabilities.SALTED_CAPABILITY));
        if (cap != null && cap.isSalted(blockPos)) {
            for (int m = 0; m < (1 + level.random.nextInt(1)); m++) {

                ParticleUtil.spawnParticleOnFace(level, blockPos, Direction.UP,
                        ModRegistry.SALT_PARTICLE.get(), 0.1f, 0.2f, true);
            }
        }
    }

    public static void handleSyncAllSalted(ClientBoundSyncAllSaltedMessage message, Player player) {
        var level = Minecraft.getInstance().level;
        if(level != null) {
            LevelChunk chunk = level.getChunk(message.chunkPos.x,message.chunkPos.z);

            var cap = ModCapabilities.get(chunk, ModCapabilities.SALTED_CAPABILITY);
            if (cap != null) {
                cap.setAllSalted(message.saltedPos);
            }
        }
    }
}
