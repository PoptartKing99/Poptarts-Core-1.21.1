package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
            context.getLevel()
                    .playSound(null, context.getClickedPos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private boolean tryPlaceCrucible(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        CrucibleBlock.CampfireType campfireType = campfireType(state);
        if (player == null || campfireType == null || state.getValue(CampfireBlock.WATERLOGGED)) {
            return false;
        }

        if (!level.isClientSide) {
            level.setBlockAndUpdate(
                    pos,
                    PoptartCoreBlocks.CRUCIBLE
                            .get()
                            .defaultBlockState()
                            .setValue(CrucibleBlock.CAMPFIRE_TYPE, campfireType)
                            .setValue(CrucibleBlock.FACING, state.getValue(CampfireBlock.FACING)));

            context.getItemInHand().consume(1, player);
        }

        return true;
    }

    private static CrucibleBlock.CampfireType campfireType(BlockState state) {
        if (state.is(Blocks.CAMPFIRE)) return CrucibleBlock.CampfireType.NORMAL;
        if (state.is(Blocks.SOUL_CAMPFIRE)) return CrucibleBlock.CampfireType.SOUL;
        if (state.is(PoptartCoreBlocks.RIFT_CAMPFIRE.get())) return CrucibleBlock.CampfireType.RIFT;
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock())
                .equals(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "cupric_campfire"))) {
            return CrucibleBlock.CampfireType.CUPRIC;
        }
        return null;
    }
}
