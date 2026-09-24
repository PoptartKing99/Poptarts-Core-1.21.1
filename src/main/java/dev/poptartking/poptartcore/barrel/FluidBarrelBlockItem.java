package dev.poptartking.poptartcore.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class FluidBarrelBlockItem extends BlockItem {
    public FluidBarrelBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        BlockPos placedPos = context.getClickedPos().immutable();
        InteractionResult result = super.place(context);
        Player player = context.getPlayer();
        if (result.consumesAction() && player != null && context.getLevel() instanceof ServerLevel level) {
            FluidBarrelFormation.onPlayerPlaced(level, placedPos, player, context.getHand(), this);
        }
        return result;
    }
}
