package dev.poptartking.poptartcore.millstone;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class MillstoneBlockItem extends BlockItem {
    public MillstoneBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result != InteractionResult.FAIL) {
            return result;
        }

        BlockPlaceContext raised =
                BlockPlaceContext.at(context, context.getClickedPos().above(), Direction.UP);
        result = super.place(raised);
        if (result != InteractionResult.FAIL) {
            return result;
        }

        if (context.getLevel().isClientSide) {
            MillstoneItemClient.showBounds(context);
        }

        return result;
    }
}
