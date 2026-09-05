package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.CrucibleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.register(
            "crucible",
            () -> new CrucibleBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE).sound(SoundType.MUD_BRICKS)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
