package dev.poptartking.poptartcore.decoration.client;

import dev.poptartking.poptartcore.decoration.TapperDripShape;
import dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluids;

/** One continuous descend/gather/stretch/release animation at the nozzle tip. */
public final class TapperDripParticle extends DripParticle {
    private final BlockPos bucket;
    private final SpriteSet sprites;
    private int stage;
    private int stageTicks;

    private TapperDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, int[] palette) {
        super(level, x, y, z, Fluids.EMPTY);
        bucket = BlockPos.containing(x, y - .06, z);
        this.sprites = sprites;
        // Falling drop: one model pixel across. The hanging film has its own geometry below.
        quadSize = .125F;
        // The bucket's solid collision shape covers its opening. Use its actual fluid surface instead.
        hasPhysics = false;
        xd = yd = zd = 0;
        int color = palette[random.nextInt(palette.length)];
        setColor(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F);
        setSprite(sprites.get(0, 2));
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (++age > TapperDripShape.MAX_AGE) {
            remove();
            return;
        }
        var entity = level.getBlockEntity(bucket);
        if (!(entity instanceof TreeTapperBlockEntity tapper)) {
            remove();
            return;
        }
        double surface = bucket.getY()
                + TreeTapperBlockEntity.surfaceHeight(tapper.storedFluid().getAmount());
        if (stage == 2) {
            y = surface + .002;
            if (++stageTicks >= 10) remove();
            return;
        }
        if (stage == 0) {
            if (++stageTicks <= TapperDripShape.FORM_TICKS) return;
            stage = 1;
            stageTicks = 0;
            yd = 0;
            setSprite(sprites.get(1, 2));
            // Keep the release frame identical to the final attached frame.
            return;
        }
        stageTicks++;
        yd -= .016F;
        move(0, yd, 0);
        var shape = TapperDripShape.falling(stageTicks);
        if (y + shape.topOffset() - shape.height() <= surface) {
            y = surface + .002;
            yd = 0;
            stage = 2;
            stageTicks = 0;
            setSprite(sprites.get(2, 2));
            return;
        }
        yd *= .98F;
    }

    @Override
    public void render(
            com.mojang.blaze3d.vertex.VertexConsumer out, net.minecraft.client.Camera camera, float partialTick) {
        if (stage != 2) {
            renderDrop(out, camera, partialTick);
            return;
        }
        var rotation = new org.joml.Quaternionf(camera.rotation());
        var eye = camera.getPosition();
        float px = (float) (net.minecraft.util.Mth.lerp(partialTick, xo, x) - eye.x);
        float py = (float) (net.minecraft.util.Mth.lerp(partialTick, yo, y) - eye.y);
        float pz = (float) (net.minecraft.util.Mth.lerp(partialTick, zo, z) - eye.z);
        // Keep the top of the falling sprite aligned with the release point.
        float visibleTop = stage == 1 ? .75F : 0F;
        var offset = new org.joml.Vector3f(0, visibleTop * getQuadSize(partialTick), 0).rotate(rotation);
        renderRotatedQuad(out, rotation, px - offset.x, py - offset.y, pz - offset.z, partialTick);
    }

    private void renderDrop(
            com.mojang.blaze3d.vertex.VertexConsumer out, net.minecraft.client.Camera camera, float partialTick) {
        var eye = camera.getPosition();
        float px = (float) (net.minecraft.util.Mth.lerp(partialTick, xo, x) - eye.x);
        float pz = (float) (net.minecraft.util.Mth.lerp(partialTick, zo, z) - eye.z);
        var shape = stage == 0
                ? TapperDripShape.forming(stageTicks + partialTick)
                : TapperDripShape.falling(stageTicks + partialTick);
        float top = (float) (net.minecraft.util.Mth.lerp(partialTick, yo, y) - eye.y) + shape.topOffset();
        float halfWidth = shape.width() / 2;
        var rotation = camera.rotation();
        float u0 = sprite.getU(3F / 8F);
        float u1 = sprite.getU(5F / 8F);
        float firstRow = stage == 0 ? 0F : 1F;
        float v0 = sprite.getV(firstRow / 8F);
        float v1 = sprite.getV((firstRow + 2F) / 8F);
        int light = getLightColor(partialTick);
        // One camera-facing sprite, anchored by its visible top edge. No box or side faces.
        dropVertex(out, rotation, px, top, pz, halfWidth, -shape.height(), u1, v1, light);
        dropVertex(out, rotation, px, top, pz, halfWidth, 0, u1, v0, light);
        dropVertex(out, rotation, px, top, pz, -halfWidth, 0, u0, v0, light);
        dropVertex(out, rotation, px, top, pz, -halfWidth, -shape.height(), u0, v1, light);
    }

    private void dropVertex(
            com.mojang.blaze3d.vertex.VertexConsumer out,
            org.joml.Quaternionf rotation,
            float x,
            float y,
            float z,
            float offsetX,
            float offsetY,
            float u,
            float v,
            int light) {
        var point = new org.joml.Vector3f(offsetX, offsetY, 0).rotate(rotation).add(x, y, z);
        out.addVertex(point.x, point.y, point.z)
                .setUv(u, v)
                .setColor(rCol, gCol, bCol, alpha)
                .setLight(light);
    }

    public record Provider(SpriteSet sprites, int[] palette) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd) {
            return new TapperDripParticle(level, x, y, z, sprites, palette);
        }
    }
}
