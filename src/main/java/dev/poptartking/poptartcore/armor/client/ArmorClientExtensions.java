package dev.poptartking.poptartcore.armor.client;

import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class ArmorClientExtensions implements IClientItemExtensions {

    private final Supplier<PoptartCoreArmorModel> helmetModel;
    private final Supplier<PoptartCoreArmorModel> chestplateModel;
    private final Supplier<PoptartCoreArmorModel> leggingsModel;
    private final Supplier<PoptartCoreArmorModel> bootsModel;

    private ArmorClientExtensions(
            Supplier<PoptartCoreArmorModel> helmetModel,
            Supplier<PoptartCoreArmorModel> chestplateModel,
            Supplier<PoptartCoreArmorModel> leggingsModel,
            Supplier<PoptartCoreArmorModel> bootsModel) {
        this.helmetModel = helmetModel;
        this.chestplateModel = chestplateModel;
        this.leggingsModel = leggingsModel;
        this.bootsModel = bootsModel;
    }

    public static ArmorClientExtensions iron() {
        return new ArmorClientExtensions(
                () -> PoptartCoreModelLayers.IRON_HELMET_MODEL,
                () -> PoptartCoreModelLayers.IRON_CHESTPLATE_MODEL,
                () -> PoptartCoreModelLayers.IRON_LEGGINGS_MODEL,
                () -> PoptartCoreModelLayers.IRON_BOOTS_MODEL);
    }

    public static ArmorClientExtensions steel() {
        return new ArmorClientExtensions(
                () -> PoptartCoreModelLayers.STEEL_HELMET_MODEL,
                () -> PoptartCoreModelLayers.STEEL_CHESTPLATE_MODEL,
                () -> PoptartCoreModelLayers.STEEL_LEGGINGS_MODEL,
                () -> PoptartCoreModelLayers.STEEL_BOOTS_MODEL);
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> original) {
        PoptartCoreArmorModel model = pick(armorSlot);

        if (model == null) {
            return original;
        }

        model.slot = armorSlot;
        // NeoForge copies the original model's animated pose after this method returns.

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
