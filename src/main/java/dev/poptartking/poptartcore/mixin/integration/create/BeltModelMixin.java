package dev.poptartking.poptartcore.mixin.integration.create;

import com.mojang.math.Transformation;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltModel;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasing;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasingClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltModel.class)
public abstract class BeltModelMixin {
    @Inject(method = "getParticleIcon(Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
            at = @At("RETURN"), cancellable = true)
    private void poptartcore$customCasingParticle(ModelData data,
                                                   CallbackInfoReturnable<TextureAtlasSprite> callback) {
        if (data.has(PoptartBeltCasingClient.CASING_PROPERTY)) {
            PoptartBeltCasing casing = data.get(PoptartBeltCasingClient.CASING_PROPERTY);
            if (casing != null) {
                callback.setReturnValue(sprite(PoptartCore.location("block/"
                        + (casing == PoptartBeltCasing.INDUSTRIAL ? "industrial_plating" : "treated_wood_casing"))));
            }
        }
    }

    @Inject(method = "getQuads", at = @At("RETURN"), cancellable = true)
    private void poptartcore$renderCustomCasing(BlockState state, Direction side, RandomSource random,
                                                 ModelData data, RenderType renderType,
                                                 CallbackInfoReturnable<List<BakedQuad>> callback) {
        if (!data.has(PoptartBeltCasingClient.CASING_PROPERTY)) {
            return;
        }
        PoptartBeltCasing casing = data.get(PoptartBeltCasingClient.CASING_PROPERTY);
        if (casing == null) {
            return;
        }

        ModelSelection selection = modelFor(state);
        BakedModel casingModel = model(PoptartBeltCasingClient.casingModel(casing, selection.name()));
        List<BakedQuad> result = new ArrayList<>();
        List<BakedQuad> casingQuads = casingModel.getQuads(state, side, random, ModelData.EMPTY, renderType);
        result.addAll(rotate(casingQuads, selection.xRotation(), selection.yRotation()));

        if (Boolean.TRUE.equals(data.get(BeltModel.COVER_PROPERTY))) {
            String axis = state.getValue(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X ? "x" : "z";
            BakedModel coverModel = model(PoptartBeltCasingClient.coverModel(casing, axis));
            result.addAll(coverModel.getQuads(state, side, random, ModelData.EMPTY, renderType));
        }
        callback.setReturnValue(result);
    }

    private static TextureAtlasSprite sprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }

    private static BakedModel model(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(location));
    }

    private static List<BakedQuad> rotate(List<BakedQuad> quads, int xRotation, int yRotation) {
        if (xRotation == 0 && yRotation % 360 == 0) {
            return quads;
        }

        BlockModelRotation rotation = BlockModelRotation.by(xRotation, yRotation);
        Matrix4f centered = new Matrix4f()
                .translation(0.5F, 0.5F, 0.5F)
                .mul(rotation.getRotation().getMatrix())
                .translate(-0.5F, -0.5F, -0.5F);
        IQuadTransformer transformer = QuadTransformers.applying(new Transformation(centered));
        List<BakedQuad> rotated = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            BakedQuad transformed = transformer.process(quad);
            rotated.add(new BakedQuad(transformed.getVertices(), transformed.getTintIndex(),
                    rotation.actualRotation().rotate(quad.getDirection()), transformed.getSprite(),
                    transformed.isShade(), transformed.hasAmbientOcclusion()));
        }
        return rotated;
    }

    private static ModelSelection modelFor(BlockState state) {
        BeltPart part = state.getValue(BeltBlock.PART);
        Direction facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
        BeltSlope slope = state.getValue(BeltBlock.SLOPE);
        boolean downward = slope == BeltSlope.DOWNWARD;
        boolean diagonal = downward || slope == BeltSlope.UPWARD;
        boolean vertical = slope == BeltSlope.VERTICAL;
        boolean sideways = slope == BeltSlope.SIDEWAYS;
        boolean negativeFacing = facing.getAxisDirection() == AxisDirection.NEGATIVE;

        if ((vertical && negativeFacing || downward || sideways && negativeFacing)
                && part != BeltPart.MIDDLE && part != BeltPart.PULLEY) {
            part = part == BeltPart.END ? BeltPart.START : BeltPart.END;
        }

        String slopeName = diagonal ? "diagonal" : (vertical ? "sideways" : slope.getSerializedName());
        int xRotation = vertical ? 90 : (sideways && negativeFacing ? 180 : 0);
        int yRotation = (int) facing.toYRot()
                + (slope == BeltSlope.UPWARD ? 180 : 0)
                + (vertical ? 90 : 0);
        return new ModelSelection(slopeName + "_" + part.getSerializedName(), xRotation, yRotation);
    }

    private record ModelSelection(String name, int xRotation, int yRotation) {}
}
