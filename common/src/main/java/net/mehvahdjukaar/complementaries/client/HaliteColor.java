package net.mehvahdjukaar.complementaries.client;

import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HCLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class HaliteColor implements BlockColor {
    private static final List<HCLColor> SOAP_COLORS =
            Stream.of(0xd9c1cb, 0xdbd7c8, 0xdbbcb7, 0xe6ceb7, -1, -1, -1, -1, -1)
                    .map(RGBColor::new).map(BaseColor::asHCL).toList();

    PerlinSimplexNoise NOISE = new PerlinSimplexNoise(new LegacyRandomSource(2382921L),
            List.of(-4, -3, -2, -1, 0, 1, 2, 3, 4));


    public static HCLColor getHaliteColor(float phase) {
        if (phase >= 1) phase = phase % 1;
       var SOAP_COLORS =
                Stream.of(0xd9c1cb,0xf7cec2, 0xdbbcb7,-1,-1, 0xe6ceb7, -1, -1, -1, -1, -1)
                        .map(RGBColor::new).toList();
        int n = SOAP_COLORS.size();
        float g = n * phase;
        int ind = (int) Math.floor(g);

        float delta = g % 1;

        RGBColor start = SOAP_COLORS.get(ind);
        RGBColor end = SOAP_COLORS.get((ind + 1) % n);

        return start.mixWith(end, delta).asHCL();
    }

    @Override
    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter,
                        @Nullable BlockPos blockPos, int i) {
        if (blockPos != null && i > 0) {
            double h;
            int x = blockPos.getX();
            int z = blockPos.getZ();
            int yy = blockPos.getY();
            h = Mth.abs((float) NOISE.getValue(x * 0.3 +0* Mth.cos(yy * 0.2f), z * 0.3 + Mth.sin(yy * 0.2f), false));

            if (h > 1 || h < -1) {
                int aa = 1;
            }
            float y = (float) (blockPos.getY() + h * 16);
            int maxColors = 50;
            var c = getHaliteColor((1 + (y / maxColors) % 1) % 1).asHSL();

           c = c.withSaturation(c.saturation()*1.4f);
            if (i == 2) {
                var d = c.asRGB();

                return d.mixWith(new RGBColor(-1), 0.4f).toInt();
            } else {
                return c.withLightness(c.lightness()).asRGB().toInt();
            }
        }
        return -1;
    }

}