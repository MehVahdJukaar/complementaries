package net.mehvahdjukaar.complementaries.mixins;

import net.mehvahdjukaar.complementaries.common.worldgen.BeardifierWithSaltProcessor;
import net.mehvahdjukaar.complementaries.common.worldgen.SaltBeardifier;
import net.minecraft.world.level.levelgen.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Beardifier.class)
public abstract class BeardifierMixin implements BeardifierWithSaltProcessor {

    @Unique
    @Nullable
    private SaltBeardifier saltFlatsBeardifier = null;

    @Override
    public void addSaltPostProcessor(SaltBeardifier saltBeardifier) {
        this.saltFlatsBeardifier = saltBeardifier;
    }

    @Inject(method = "compute", at = @At("HEAD"), cancellable = true)
    public void computeSaltFlats(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValueD() + saltFlatsBeardifier.compute(context));
    }

    @Override
    public SaltBeardifier getSaltPostProcessor() {
        return saltFlatsBeardifier;
    }
}
