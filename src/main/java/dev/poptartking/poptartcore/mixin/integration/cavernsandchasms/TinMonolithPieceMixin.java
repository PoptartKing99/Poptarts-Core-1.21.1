package dev.poptartking.poptartcore.mixin.integration.cavernsandchasms;

import com.teamabnormals.caverns_and_chasms.common.levelgen.structure.TinMonolithPieces;
import com.teamabnormals.caverns_and_chasms.core.registry.CCBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TinMonolithPieces.TinMonolithPiece.class)
public class TinMonolithPieceMixin {
    @ModifyArg(
            method = "postProcess",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            index = 1)
    private BlockState poptartcore$replaceTinWithTitanium(BlockState state) {
        if (state.is(CCBlocks.RAW_TIN_BLOCK.get())) {
            return PoptartCoreBlocks.RAW_TITANIUM_BLOCK.get().defaultBlockState();
        }
        if (state.is(CCBlocks.CASSITERITE_TIN_ORE.get())) {
            return PoptartCoreBlocks.CASSITERITE_TITANIUM_ORE.get().defaultBlockState();
        }
        if (state.is(CCBlocks.CYLINDRITE_TIN_ORE.get())) {
            return PoptartCoreBlocks.CYLINDRITE_TITANIUM_ORE.get().defaultBlockState();
        }
        return state;
    }
}
