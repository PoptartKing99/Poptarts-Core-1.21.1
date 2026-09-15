package dev.poptartking.poptartcore.rift.client;

import com.mojang.math.Transformation;
import dev.poptartking.poptartcore.rift.RiftSedimentBlock;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class RiftSedimentModel implements IDynamicBakedModel {
    private static final ModelProperty<int[]> CONNECTIONS = new ModelProperty<>();
    private static final Direction[] FACES = Direction.values();

    private final BakedModel dot;
    private final BakedModel arm;
    private final Map<Direction, IQuadTransformer> faceTransforms = new EnumMap<>(Direction.class);
    private final Map<Direction, IQuadTransformer[]> armTransforms = new EnumMap<>(Direction.class);

    public RiftSedimentModel(BakedModel dot, BakedModel arm) {
        this.dot = dot;
        this.arm = arm;
        for (Direction face : FACES) {
            faceTransforms.put(face, QuadTransformers.applying(oriented(face, 0)));
            IQuadTransformer[] arms = new IQuadTransformer[4];
            for (int index = 0; index < arms.length; index++) {
                arms[index] = QuadTransformers.applying(oriented(face, index));
            }
            armTransforms.put(face, arms);
        }
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        int[] masks = new int[FACES.length];
        for (Direction face : FACES) {
            if (!MultifaceBlock.hasFace(state, face)) {
                continue;
            }
            Direction[] plane = planeOf(face);
            for (int index = 0; index < plane.length; index++) {
                if (connects(level, pos, state, face, plane[index])) {
                    masks[face.ordinal()] |= 1 << index;
                }
            }
        }
        return data.derive().with(CONNECTIONS, masks).build();
    }

    private static boolean connects(
            BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, Direction along) {
        if (MultifaceBlock.hasFace(state, along)) {
            return true;
        }
        BlockState side = level.getBlockState(pos.relative(along));
        return side.getBlock() instanceof RiftSedimentBlock && MultifaceBlock.hasFace(side, face);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType renderType) {
        if (side != null || state == null) {
            return List.of();
        }

        int[] masks = data.get(CONNECTIONS);
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction face : FACES) {
            if (!MultifaceBlock.hasFace(state, face)) {
                continue;
            }
            int mask = masks == null ? 0 : masks[face.ordinal()];
            if (mask == 0) {
                quads.addAll(faceTransforms
                        .get(face)
                        .process(dot.getQuads(state, null, random, ModelData.EMPTY, renderType)));
            } else {
                IQuadTransformer[] arms = armTransforms.get(face);
                for (int index = 0; index < arms.length; index++) {
                    if ((mask & 1 << index) != 0) {
                        quads.addAll(
                                arms[index].process(arm.getQuads(state, null, random, ModelData.EMPTY, renderType)));
                    }
                }
            }
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }

    private static Direction[] planeOf(Direction face) {
        return switch (face) {
            case DOWN -> new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            case UP -> new Direction[] {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
            case NORTH -> new Direction[] {Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST};
            case SOUTH -> new Direction[] {Direction.DOWN, Direction.EAST, Direction.UP, Direction.WEST};
            case WEST -> new Direction[] {Direction.NORTH, Direction.DOWN, Direction.SOUTH, Direction.UP};
            case EAST -> new Direction[] {Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN};
        };
    }

    private static Transformation oriented(Direction face, int quarterTurns) {
        Matrix4f faceRotation =
                switch (face) {
                    case DOWN -> new Matrix4f();
                    case UP -> new Matrix4f().rotationX((float) Math.PI);
                    case NORTH -> new Matrix4f().rotationX((float) (Math.PI / 2.0));
                    case SOUTH -> new Matrix4f().rotationX((float) (-Math.PI / 2.0));
                    case WEST -> new Matrix4f().rotationZ((float) (-Math.PI / 2.0));
                    case EAST -> new Matrix4f().rotationZ((float) (Math.PI / 2.0));
                };
        Matrix4f matrix = new Matrix4f()
                .translation(0.5F, 0.5F, 0.5F)
                .mul(faceRotation)
                .rotateY((float) (-quarterTurns * Math.PI / 2.0))
                .translate(-0.5F, -0.5F, -0.5F);
        return new Transformation(matrix);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return dot.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
