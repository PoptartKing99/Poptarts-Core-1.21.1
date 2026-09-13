package dev.poptartking.poptartcore.beekeeping.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BeeSmokerClientExtensions implements IClientItemExtensions {
    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (entity.isUsingItem() && entity.getUsedItemHand() == hand) {
            return HumanoidModel.ArmPose.TOOT_HORN;
        }
        return null;
    }
}
