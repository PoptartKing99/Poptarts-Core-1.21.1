package dev.poptartking.poptartcore.client.model;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public abstract class PlateArmorClientExtensions implements IClientItemExtensions {

    private final Supplier<PoptartCoreArmorModel> helmetModel;
    private final Supplier<PoptartCoreArmorModel> chestplateModel;
    private final Supplier<PoptartCoreArmorModel> leggingsModel;
    private final Supplier<PoptartCoreArmorModel> bootsModel;

    protected PlateArmorClientExtensions(
            Supplier<PoptartCoreArmorModel> helmetModel,
            Supplier<PoptartCoreArmorModel> chestplateModel,
            Supplier<PoptartCoreArmorModel> leggingsModel,
            Supplier<PoptartCoreArmorModel> bootsModel) {
        this.helmetModel = helmetModel;
        this.chestplateModel = chestplateModel;
        this.leggingsModel = leggingsModel;
        this.bootsModel = bootsModel;
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> original) {
        PoptartCoreArmorModel model = pick(armorSlot);

        if (model == null) {
            return original;
        }

        float partialTick = Minecraft.getInstance().getFrameTimeNs() / 20000000000L;
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);

        model.slot = armorSlot;
        model.copyFromDefault(original);
        model.setupAnim(
                entity,
                entity.walkAnimation.position(),
                entity.walkAnimation.speed(),
                entity.tickCount + partialTick,
                headRot - bodyRot,
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));

        return model;
    }

    private PoptartCoreArmorModel pick(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> helmetModel.get();
            case CHEST -> chestplateModel.get();
            case LEGS -> leggingsModel.get();
            case FEET -> bootsModel.get();
            default -> null;
        };
    }
}
