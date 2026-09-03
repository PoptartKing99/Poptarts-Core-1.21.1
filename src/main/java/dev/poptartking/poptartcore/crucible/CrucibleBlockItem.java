package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CrucibleBlockItem extends BlockItem {

    public CrucibleBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (tryPlaceCrucible(context)) {
            context.getLevel().playSound(
                    null,
                    context.getClickedPos(),
                    SoundEvents.STONE_PLACE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private boolean tryPlaceCrucible(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        if (player == null
                || !state.is(Blocks.CAMPFIRE)
                || state.getValue(CampfireBlock.WATERLOGGED)) {
            return false;
        }

        if (!level.isClientSide) {
            level.setBlockAndUpdate(
                    pos,
                    PoptartCoreBlocks.CRUCIBLE.get()
                            .defaultBlockState()
                            .setValue(
                                    CrucibleBlock.FACING,
                                    state.getValue(CampfireBlock.FACING)
                            )
            );

            context.getItemInHand().consume(1, player);
        }

        return true;
    }
}