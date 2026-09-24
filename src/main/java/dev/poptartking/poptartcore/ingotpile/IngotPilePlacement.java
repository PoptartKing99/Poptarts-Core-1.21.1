package dev.poptartking.poptartcore.ingotpile;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class IngotPilePlacement {
    private IngotPilePlacement() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!player.isSecondaryUseActive() || !player.mayBuild() || !IngotPileMaterials.supports(stack)) return;
        Level level = event.getLevel();
        BlockPos clicked = event.getPos();
        BlockState clickedState = level.getBlockState(clicked);
        if (clickedState.is(PoptartCoreBlocks.INGOT_PILE.get())) {
            if (clickedState.getValue(IngotPileBlock.COUNT) < 64) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                if (!level.isClientSide && level.getBlockEntity(clicked) instanceof IngotPileBlockEntity pile
                        && pile.add(stack) && !player.getAbilities().instabuild) stack.shrink(1);
            }
            return;
        }
        BlockPos target = clickedState.canBeReplaced() ? clicked : clicked.relative(event.getFace());
        if (!level.getBlockState(target).canBeReplaced()) return;
        BlockState state = PoptartCoreBlocks.INGOT_PILE.get().defaultBlockState();
        if (!state.canSurvive(level, target) || !level.isUnobstructed(state, target, net.minecraft.world.phys.shapes.CollisionContext.of(player))) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (!level.isClientSide && level.setBlock(target, state, 3)) {
            if (level.getBlockEntity(target) instanceof IngotPileBlockEntity pile && pile.add(stack)) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
            } else level.removeBlock(target, false);
        }
    }
}
