package dev.poptartking.poptartcore.lostheart.client;

import dev.poptartking.poptartcore.lostheart.LostHeartEmberOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class LostHeartEmberParticle extends TextureSheetParticle {
    private static final int[][] PALETTES = {
            {16777215, 14408916, 13158320, 11645838, 10527108},
            {16775908, 16050099, 13747342, 12364409, 11767392},
            {16767691, 15705217, 13466974, 12020816, 10704704},
            {16759731, 15491408, 13252142, 11742761, 9707808}
    };
    private final int owner;

    private LostHeartEmberParticle(ClientLevel level, double x, double y, double z,
                                   double xd, double yd, double zd, SpriteSet sprites,
                                   LostHeartEmberOptions options) {
        super(level, x, y, z, 0, 0, 0);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        gravity = 0;
        friction = 0.92F;
        hasPhysics = false;
        lifetime = 70 + random.nextInt(40);
        quadSize = 0.03125F;
        setSpriteFromAge(sprites);
        owner = options.owner();
        int[] palette = PALETTES[Math.floorMod(options.stage() - 1, PALETTES.length)];
        int color = palette[random.nextInt(palette.length)];
        setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        if (level.getEntity(owner) instanceof Player player && age > 6) {
            double dx = player.getX() - x;
            double dy = player.getY() + player.getBbHeight() * 0.5 - y;
            double dz = player.getZ() - z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 0.4) {
                player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.12F, 0.32F + random.nextFloat() * 0.16F, false);
                remove();
                return;
            }
            double pull = 0.055 / Math.max(distance, 0.75);
            xd += dx / distance * pull;
            yd += dy / distance * pull;
            zd += dz / distance * pull;
        }
        xd *= friction;
        yd *= friction;
        zd *= friction;
        move(xd, yd, zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<LostHeartEmberOptions> {
        @Override
        public Particle createParticle(LostHeartEmberOptions options, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new LostHeartEmberParticle(level, x, y, z, xd, yd, zd, sprites, options);
        }
    }
}
