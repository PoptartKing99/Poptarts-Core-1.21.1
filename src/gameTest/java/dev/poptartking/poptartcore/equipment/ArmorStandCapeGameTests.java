package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCapeItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArmorStandCapeGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void allColorsEquipSaveAndRemoveWithoutReplacingArmor(GameTestHelper helper) {
        for (var color : DyeColor.values()) {
            for (var hand : InteractionHand.values()) {
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                var stand = new ArmorStand(helper.getLevel(), 0, 0, 0);
                stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                var cape = new ItemStack(PoptartCapeItems.AGILITY_CAPE.get());
                CapeColors.setColor(cape, color);
                player.setItemInHand(hand, cape);
                helper.assertTrue(
                        stand.interactAt(player, Vec3.ZERO, hand).consumesAction(), "Native interaction equips cape");
                helper.assertTrue(player.getItemInHand(hand).isEmpty(), "Survival equip consumes held cape");
                helper.assertTrue(CapeColors.color(ArmorStandCape.get(stand)) == color, "Selected color retained");
                helper.assertTrue(
                        stand.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE), "Chest armor unchanged");
                player.setItemInHand(hand, ArmorStandCape.get(stand).copy());
                stand.interactAt(player, Vec3.ZERO, hand);
                helper.assertTrue(
                        player.getItemInHand(hand).getCount() == 1, "Occupied cape slot cannot consume another cape");
                var saved = stand.saveWithoutId(new net.minecraft.nbt.CompoundTag());
                var loaded = new ArmorStand(helper.getLevel(), 0, 0, 0);
                loaded.load(saved);
                helper.assertTrue(
                        ItemStack.matches(ArmorStandCape.get(stand), ArmorStandCape.get(loaded)),
                        "Save/load preserves cape");
                player.setItemInHand(hand, ItemStack.EMPTY);
                player.setShiftKeyDown(true);
                loaded.interactAt(player, Vec3.ZERO, hand);
                helper.assertTrue(
                        ArmorStandCape.get(loaded).isEmpty() && CapeColors.color(player.getItemInHand(hand)) == color,
                        "Shift empty-hand interaction returns cape with dye intact");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void creativeEquipAndBreakDropExactlyOneCape(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        player.getAbilities().instabuild = true;
        var position = helper.absolutePos(new net.minecraft.core.BlockPos(4, 2, 4));
        var stand = new ArmorStand(helper.getLevel(), position.getX(), position.getY(), position.getZ());
        helper.getLevel().addFreshEntity(stand);
        var cape = new ItemStack(PoptartCapeItems.AGILITY_CAPE.get());
        CapeColors.setColor(cape, DyeColor.PURPLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, cape);
        stand.interactAt(player, Vec3.ZERO, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Creative keeps held cape");
        stand.hurt(player.damageSources().playerAttack(player), 1);
        helper.assertTrue(
                stand.isRemoved() && ArmorStandCape.get(stand).isEmpty(), "Actual creative break clears cape");
        stand.kill();
        var drops = helper
                .getLevel()
                .getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(position).inflate(2))
                .stream()
                .filter(entity -> CapeColors.isCape(entity.getItem()))
                .toList();
        helper.assertTrue(
                drops.size() == 1
                        && drops.getFirst().getItem().getCount() == 1
                        && CapeColors.color(drops.getFirst().getItem()) == DyeColor.PURPLE,
                "Breaking drops exactly one correctly dyed cape, even if kill repeats");
        helper.succeed();
    }
}
