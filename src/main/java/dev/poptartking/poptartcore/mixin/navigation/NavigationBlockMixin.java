package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.Lodestones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Block.class})
public abstract class NavigationBlockMixin {
    @Inject(
            method = {"setPlacedBy"},
            at = {@At("HEAD")})
    private void poptartcore$nameLodestone(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack,
            CallbackInfo ci) {
        if (placer instanceof ServerPlayer player && Lodestones.is(state)) {
            Lodestones.open(player, pos);
        }
    }
}
