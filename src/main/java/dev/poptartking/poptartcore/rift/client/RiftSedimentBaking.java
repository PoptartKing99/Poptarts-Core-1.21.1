package dev.poptartking.poptartcore.rift.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class RiftSedimentBaking {
    private static final ModelResourceLocation DOT =
            ModelResourceLocation.standalone(PoptartCore.location("block/rift_sediment_piece_dot"));
    private static final ModelResourceLocation ARM =
            ModelResourceLocation.standalone(PoptartCore.location("block/rift_sediment_piece_arm"));

    private RiftSedimentBaking() {}

    @SubscribeEvent
    public static void registerAdditionalModels(RegisterAdditional event) {
        event.register(DOT);
        event.register(ARM);
    }

    @SubscribeEvent
    public static void replaceSedimentModels(ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        BakedModel dot = models.get(DOT);
        BakedModel arm = models.get(ARM);
        if (dot == null || arm == null) {
            return;
        }

        Block block = PoptartCoreBlocks.RIFT_SEDIMENT.get();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        RiftSedimentModel connectedModel = new RiftSedimentModel(dot, arm);
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            models.put(BlockModelShaper.stateToModelLocation(id, state), connectedModel);
        }
    }
}
