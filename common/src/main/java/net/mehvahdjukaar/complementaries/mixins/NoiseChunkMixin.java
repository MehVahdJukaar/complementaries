package net.mehvahdjukaar.complementaries.mixins;

import net.mehvahdjukaar.complementaries.Complementaries;
import net.mehvahdjukaar.complementaries.ComplementariesClient;
import net.mehvahdjukaar.complementaries.common.worldgen.NC;
import net.mehvahdjukaar.complementaries.common.worldgen.Saltifer;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements NC {


    @Shadow @Final
    int firstNoiseX;
    @Shadow @Final
    int firstNoiseZ;
    @Shadow @Final private Aquifer aquifer;
    @Shadow @Final private NoiseSettings noiseSettings;
    @Unique
    private Saltifer saltifer;

    @ModifyArg(method = "<init>", at=@At(target = "Lnet/minecraft/world/level/levelgen/material/MaterialRuleList;<init>(Ljava/util/List;)V",
            value = "INVOKE"))
    public List<NoiseChunk.BlockStateFiller> bb(List<NoiseChunk.BlockStateFiller> old){

        this.saltifer  = Saltifer.create(((NoiseChunk) (Object)this),(AquiferAccessor)this.aquifer,
                this.noiseSettings.minY(), this.noiseSettings.height());

     return   old;
    }

    @Override
    public Saltifer getSaltifer() {
        return saltifer;
    }
}
