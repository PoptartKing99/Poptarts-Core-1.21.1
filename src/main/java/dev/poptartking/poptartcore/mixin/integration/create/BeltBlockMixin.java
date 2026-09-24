package dev.poptartking.poptartcore.mixin.integration.create;

import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasing;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasingAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltBlock.class)
public abstract class BeltBlockMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void poptartcore$applyCasing(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit,
                                          CallbackInfoReturnable<ItemInteractionResult> callback) {
        PoptartBeltCasing casing = PoptartBeltCasing.fromItem(stack);
        if (casing == null || player.isShiftKeyDown() || !player.mayBuild()) {
            return;
        }
        BeltBlockEntity belt = BeltHelper.getSegmentBE(level, pos);
        if (belt == null) {
            return;
        }
        ((PoptartBeltCasingAccess) belt).poptartcore$setCasing(casing);
        ((BeltBlock) (Object) this).updateCoverProperty(level, pos, level.getBlockState(pos));
        SoundType sound = casing.block().defaultBlockState().getSoundType(level, pos, player);
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        callback.setReturnValue(ItemInteractionResult.SUCCESS);
    }
}
