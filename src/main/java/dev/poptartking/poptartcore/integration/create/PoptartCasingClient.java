package dev.poptartking.poptartcore.integration.create;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogCTBehaviour;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class PoptartCasingClient {
    private static final ResourceLocation PLATING = PoptartCore.location("block/industrial_plating");
    private static final ResourceLocation PLATING_CONNECTED =
            PoptartCore.location("block/industrial_plating_connected");
    private static final ResourceLocation COG_SIDE =
            PoptartCore.location("block/industrial_plating_encased_cogwheel_side");
    private static final ResourceLocation COG_SIDE_CONNECTED =
            PoptartCore.location("block/industrial_plating_encased_cogwheel_side_connected");
    private static final ResourceLocation TREATED_WOOD = PoptartCore.location("block/treated_wood_casing");
    private static final ResourceLocation TREATED_WOOD_CONNECTED =
            PoptartCore.location("block/treated_wood_casing_connected");
    private static final ResourceLocation TREATED_WOOD_COG_SIDE =
            PoptartCore.location("block/treated_wood_casing_encased_cogwheel_side");
    private static final ResourceLocation TREATED_WOOD_COG_SIDE_CONNECTED =
            PoptartCore.location("block/treated_wood_casing_encased_cogwheel_side_connected");

    private PoptartCasingClient() {}

    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerFamily(
                    PoptartCoreBlocks.INDUSTRIAL_PLATING.get(),
                    PoptartCoreBlocks.INDUSTRIAL_ENCASED_SHAFT.get(),
                    PoptartCoreBlocks.INDUSTRIAL_ENCASED_COGWHEEL.get(),
                    PoptartCoreBlocks.INDUSTRIAL_ENCASED_LARGE_COGWHEEL.get(),
                    PLATING,
                    PLATING_CONNECTED,
                    COG_SIDE,
                    COG_SIDE_CONNECTED);
            registerFamily(
                    PoptartCoreBlocks.TREATED_WOOD_CASING.get(),
                    PoptartCoreBlocks.TREATED_WOOD_ENCASED_SHAFT.get(),
                    PoptartCoreBlocks.TREATED_WOOD_ENCASED_COGWHEEL.get(),
                    PoptartCoreBlocks.TREATED_WOOD_ENCASED_LARGE_COGWHEEL.get(),
                    TREATED_WOOD,
                    TREATED_WOOD_CONNECTED,
                    TREATED_WOOD_COG_SIDE,
                    TREATED_WOOD_COG_SIDE_CONNECTED);
        });
    }

    private static void registerFamily(
            Block casingBlock,
            Block shaftBlock,
            Block cogBlock,
            Block largeCogBlock,
            ResourceLocation casingTexture,
            ResourceLocation connectedTexture,
            ResourceLocation sideTexture,
            ResourceLocation connectedSideTexture) {
        CTSpriteShiftEntry casing =
                CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, casingTexture, connectedTexture);
        CTSpriteShiftEntry cogCasing = new CTSpriteShiftEntry(AllCTTypes.OMNIDIRECTIONAL);
        cogCasing.set(casingTexture, connectedTexture);
        CTSpriteShiftEntry cogSideVertical =
                CTSpriteShifter.getCT(AllCTTypes.VERTICAL, sideTexture, connectedSideTexture);
        CTSpriteShiftEntry cogSideHorizontal =
                CTSpriteShifter.getCT(AllCTTypes.HORIZONTAL, sideTexture, connectedSideTexture);

        registerCasing(casingBlock, casing);
        registerCasing(shaftBlock, casing);
        registerCog(cogBlock, cogCasing, cogSideVertical, cogSideHorizontal);
        registerLargeCog(largeCogBlock, cogCasing);
    }

    private static void registerCasing(Block block, CTSpriteShiftEntry casing) {
        CreateClient.CASING_CONNECTIVITY.makeCasing(block, casing);
        CreateClient.MODEL_SWAPPER
                .getCustomBlockModels()
                .register(
                        BuiltInRegistries.BLOCK.getKey(block),
                        model -> new CTModel(model, new EncasedCTBehaviour(casing)));
    }

    private static void registerCog(
            Block block,
            CTSpriteShiftEntry casing,
            CTSpriteShiftEntry cogSideVertical,
            CTSpriteShiftEntry cogSideHorizontal) {
        CreateClient.CASING_CONNECTIVITY.make(block, casing);
        CreateClient.MODEL_SWAPPER
                .getCustomBlockModels()
                .register(
                        BuiltInRegistries.BLOCK.getKey(block),
                        model -> new CTModel(
                                model,
                                new EncasedCogCTBehaviour(casing, Couple.create(cogSideVertical, cogSideHorizontal))));
    }

    private static void registerLargeCog(Block block, CTSpriteShiftEntry casing) {
        CreateClient.CASING_CONNECTIVITY.make(block, casing);
        CreateClient.MODEL_SWAPPER
                .getCustomBlockModels()
                .register(
                        BuiltInRegistries.BLOCK.getKey(block),
                        model -> new CTModel(model, new EncasedCogCTBehaviour(casing)));
    }
}
