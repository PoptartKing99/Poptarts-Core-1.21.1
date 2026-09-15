package dev.poptartking.poptartcore.rift.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class RiftFlameParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected RiftFlameParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        friction = 0.96F;
        xd = xd * 0.01 + xSpeed;
        yd = yd * 0.01 + ySpeed;
        zd = zd * 0.01 + zSpeed;
        this.x += (random.nextFloat() - random.nextFloat()) * 0.05F;
        this.y += (random.nextFloat() - random.nextFloat()) * 0.05F;
        this.z += (random.nextFloat() - random.nextFloat()) * 0.05F;
        lifetime = (int) (12.0 / (Math.random() * 0.8 + 0.2)) + 6;
        quadSize *= 0.85F + random.nextFloat() * 0.4F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = (age + partialTick) / lifetime;
        return quadSize * (1.0F - progress * progress * 0.5F);
    }

    @Override
    public int getLightColor(float partialTick) {
        float progress = Mth.clamp((age + partialTick) / lifetime, 0.0F, 1.0F);
        int packedLight = super.getLightColor(partialTick);
        int blockLight = Math.min(240, (packedLight & 0xFF) + (int) (progress * 240.0F));
        int skyLight = packedLight >> 16 & 0xFF;
        return blockLight | skyLight << 16;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed) {
            return new RiftFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
