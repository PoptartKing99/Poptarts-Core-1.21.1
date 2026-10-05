package dev.poptartking.poptartcore.mixin.integration.create;

import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltBlock.class)
public abstract class BeltDropCostMixin {
    @Inject(method = "getDrops", at = @At("RETURN"))
    private void poptartcore$refundConnectors(BlockState state, LootParams.Builder builder,
            CallbackInfoReturnable<List<ItemStack>> callback) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof BeltBlockEntity belt) || belt.beltLength <= 1) {
            return;
        }
        List<ItemStack> drops = callback.getReturnValue();
        for (ItemStack connector : drops) {
            if (connector.getItem() instanceof BeltConnectorItem) {
                int remaining = belt.beltLength - connector.getCount();
                int added = Math.min(remaining, connector.getMaxStackSize() - connector.getCount());
                connector.grow(added);
                remaining -= added;
                while (remaining > 0) {
                    int count = Math.min(remaining, connector.getMaxStackSize());
                    drops.add(connector.copyWithCount(count));
                    remaining -= count;
                }
                return;
            }
        }
    }
}
