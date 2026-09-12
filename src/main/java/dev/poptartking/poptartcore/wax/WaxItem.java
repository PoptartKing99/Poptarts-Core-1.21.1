package dev.poptartking.poptartcore.wax;

import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class WaxItem extends Item implements SignApplicator {
    public WaxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(PoptartCoreTags.UNWAXABLE)) {
            return InteractionResult.PASS;
        }

        return HoneycombItem.getWaxed(state)
                .map(waxedState -> {
                    Player player = context.getPlayer();
                    ItemStack stack = context.getItemInHand();
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    }

                    stack.shrink(1);
                    level.setBlock(pos, waxedState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxedState));
                    level.levelEvent(player, 3003, pos, 0);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                })
                .orElse(InteractionResult.PASS);
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity sign, boolean frontText, Player player) {
        if (!sign.setWaxed(true)) {
            return false;
        }
        level.levelEvent(null, 3003, sign.getBlockPos(), 0);
        return true;
    }
}
