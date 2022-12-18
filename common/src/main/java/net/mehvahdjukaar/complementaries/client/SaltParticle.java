package net.mehvahdjukaar.complementaries.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class SaltParticle extends TextureSheetParticle {

    SaltParticle(ClientLevel arg, double d, double e, double f, double g, double h, double i) {
        super(arg, d, e, f, g, h, i);
        float j = 1;
        float v = this.random.nextFloat() * 0.3F + 0.5f;
        this.rCol = j;
        this.gCol = this.random.nextFloat() * 0.2F + 0.7f;
        this.bCol = v;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.7F;
        this.xd *= 0.019999999552965164;
        this.yd *= 0.019999999552965164*2;
        this.zd *= 0.019999999552965164;
        this.lifetime = (int) (20.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    public void tick() {
        this.quadSize*=0.99f;
        double xoo = xo;
        double zoo = zo;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            //this.x += 0.1*Mth.sin(this.lifetime*0.1f);
            //this.z += 0.1*Mth.cos(this.lifetime*0.1f);

            this.xd *= 0.99;
            this.yd *= 0.99;
            this.zd *= 0.99;
        }
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SaltParticle particle = new SaltParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
