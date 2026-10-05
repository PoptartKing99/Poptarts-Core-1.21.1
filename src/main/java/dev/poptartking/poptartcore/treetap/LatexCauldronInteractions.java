package dev.poptartking.poptartcore.treetap;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class LatexCauldronInteractions {
    public static final CauldronInteraction.InteractionMap LATEX =
            CauldronInteraction.newInteractionMap("poptartcore_latex");

    private LatexCauldronInteractions() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(LatexCauldronInteractions::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CauldronInteraction.EMPTY.map().put(PoptartCoreItems.LATEX_BOTTLE.get(),
                    (state, level, pos, player, hand, stack) -> {
                        if (!state.is(Blocks.CAULDRON)) {
                            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                        }
                        if (!level.isClientSide) {
                            Item item = stack.getItem();
                            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                                    new ItemStack(Items.GLASS_BOTTLE)));
                            player.awardStat(Stats.USE_CAULDRON);
                            player.awardStat(Stats.ITEM_USED.get(item));
                            level.setBlockAndUpdate(pos, PoptartCoreBlocks.LATEX_CAULDRON.get()
                                    .defaultBlockState());
                            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    });
        });
    }
}
