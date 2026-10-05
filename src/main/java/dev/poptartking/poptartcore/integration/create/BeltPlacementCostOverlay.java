package dev.poptartking.poptartcore.integration.create;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class BeltPlacementCostOverlay {
    private BeltPlacementCostOverlay() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || player.isCreative()
                || !(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof BeltConnectorItem) || !held.has(AllDataComponents.BELT_FIRST_SHAFT)) {
                continue;
            }
            BlockPos first = held.get(AllDataComponents.BELT_FIRST_SHAFT);
            if (first == null || !level.getBlockState(first).hasProperty(BlockStateProperties.AXIS)) {
                return;
            }
            BlockPos selected = hit.getBlockPos();
            if (level.getBlockState(selected).canBeReplaced()) {
                return;
            }
            if (!ShaftBlock.isShaft(level.getBlockState(selected))) {
                selected = selected.relative(hit.getDirection());
            }
            if (first.equals(selected) || !selected.closerThan(first, BeltConnectorItem.maxLength() * 2.0)) {
                return;
            }
            if (BeltConnectorItem.validateAxis(level, selected)
                    && BeltConnectorItem.canConnect(level, first, selected)) {
                int required = BeltConnectorCost.length(first, selected);
                BlueprintOverlayRenderer.displayChainRequirements(held.getItem(), required,
                        BeltConnectorCost.available(player) >= required);
            }
            return;
        }
    }
}
