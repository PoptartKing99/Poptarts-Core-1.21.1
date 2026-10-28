package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.spider.TemporaryCobwebBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartSpiderBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final DeferredBlock<TemporaryCobwebBlock> TEMPORARY_COBWEB = BLOCKS.register(
            "temporary_cobweb",
            () -> new TemporaryCobwebBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB)
                    .strength(0.4F)
                    .noLootTable()));

    private PoptartSpiderBlocks() {}

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
