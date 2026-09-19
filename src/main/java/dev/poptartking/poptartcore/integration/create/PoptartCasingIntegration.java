package dev.poptartking.poptartcore.integration.create;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import dev.poptartking.poptartcore.mixin.accessor.BlockEntityTypeAccessor;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.HashSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class PoptartCasingIntegration {
    private PoptartCasingIntegration() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PoptartCasingIntegration::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EncasingRegistry.addVariant(AllBlocks.SHAFT.get(), PoptartCoreBlocks.INDUSTRIAL_ENCASED_SHAFT.get());
            EncasingRegistry.addVariant(AllBlocks.COGWHEEL.get(), PoptartCoreBlocks.INDUSTRIAL_ENCASED_COGWHEEL.get());
            EncasingRegistry.addVariant(
                    AllBlocks.LARGE_COGWHEEL.get(), PoptartCoreBlocks.INDUSTRIAL_ENCASED_LARGE_COGWHEEL.get());
            EncasingRegistry.addVariant(AllBlocks.SHAFT.get(), PoptartCoreBlocks.TREATED_WOOD_ENCASED_SHAFT.get());
            EncasingRegistry.addVariant(
                    AllBlocks.COGWHEEL.get(), PoptartCoreBlocks.TREATED_WOOD_ENCASED_COGWHEEL.get());
            EncasingRegistry.addVariant(
                    AllBlocks.LARGE_COGWHEEL.get(), PoptartCoreBlocks.TREATED_WOOD_ENCASED_LARGE_COGWHEEL.get());

            addValidBlock(AllBlockEntityTypes.ENCASED_SHAFT.get(), PoptartCoreBlocks.INDUSTRIAL_ENCASED_SHAFT.get());
            addValidBlock(
                    AllBlockEntityTypes.ENCASED_COGWHEEL.get(), PoptartCoreBlocks.INDUSTRIAL_ENCASED_COGWHEEL.get());
            addValidBlock(
                    AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL.get(),
                    PoptartCoreBlocks.INDUSTRIAL_ENCASED_LARGE_COGWHEEL.get());
            addValidBlock(AllBlockEntityTypes.ENCASED_SHAFT.get(), PoptartCoreBlocks.TREATED_WOOD_ENCASED_SHAFT.get());
            addValidBlock(
                    AllBlockEntityTypes.ENCASED_COGWHEEL.get(), PoptartCoreBlocks.TREATED_WOOD_ENCASED_COGWHEEL.get());
            addValidBlock(
                    AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL.get(),
                    PoptartCoreBlocks.TREATED_WOOD_ENCASED_LARGE_COGWHEEL.get());
        });
    }

    private static void addValidBlock(BlockEntityType<?> type, Block block) {
        BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) (Object) type;
        HashSet<Block> validBlocks = new HashSet<>(accessor.poptartcore$getValidBlocks());
        validBlocks.add(block);
        accessor.poptartcore$setValidBlocks(validBlocks);
    }
}
