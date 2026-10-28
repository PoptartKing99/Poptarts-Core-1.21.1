package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RatRingGameTests {
    private static final ResourceLocation SPEED_ID = PoptartCore.location("ring_of_the_rat_sneak");

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void unchangedRatBonusDoesNotQueueAttributeUpdates(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        new PlayerAccessoryHandler(player).setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_THE_RAT.get()));
        crouch(player, true);
        RingEffects.tick(player);
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        helper.assertTrue(speed.hasModifier(SPEED_ID), "Crouching applies the ring bonus");
        clearSpeedUpdates(player);

        for (int tick = 0; tick < 40; tick++) {
            RingEffects.tick(player);
            helper.assertTrue(
                    !player.getAttributes().getAttributesToUpdate().contains(speed)
                            && !player.getAttributes().getAttributesToSync().contains(speed),
                    "Unchanged crouching does not queue speed recalculation or synchronization");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ratBonusFollowsCrouchingAndEquipmentChanges(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var other = new AttributeModifier(
                PoptartCore.location("test_other_speed"), .025, AttributeModifier.Operation.ADD_VALUE);
        speed.addTransientModifier(other);
        double base = speed.getValue();
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_THE_RAT.get()));
        RingEffects.tick(player);
        helper.assertTrue(!speed.hasModifier(SPEED_ID), "Standing never grants the crouching bonus");

        crouch(player, true);
        clearSpeedUpdates(player);
        RingEffects.tick(player);
        helper.assertTrue(Math.abs(speed.getValue() - base * 1.15) < .000001, "Crouching keeps the 15 percent bonus");
        helper.assertTrue(
                player.getAttributes().getAttributesToSync().contains(speed), "Starting crouching synchronizes speed");
        crouch(player, false);
        clearSpeedUpdates(player);
        RingEffects.tick(player);
        helper.assertTrue(
                !speed.hasModifier(SPEED_ID) && speed.getValue() == base, "Standing removes only the Rat bonus");
        helper.assertTrue(
                player.getAttributes().getAttributesToSync().contains(speed), "Stopping crouching synchronizes speed");
        clearSpeedUpdates(player);
        for (int tick = 0; tick < 40; tick++) RingEffects.tick(player);
        helper.assertTrue(
                !player.getAttributes().getAttributesToSync().contains(speed),
                "Remaining standing does not resynchronize speed");

        handler.setStackInSlot(0, ItemStack.EMPTY);
        crouch(player, true);
        RingEffects.tick(player);
        helper.assertTrue(!speed.hasModifier(SPEED_ID), "Crouching without the ring has no bonus");
        var ring = new ItemStack(PoptartRingItems.RING_OF_THE_RAT.get());
        handler.setStackInSlot(1, ring);
        RingEffects.tick(player);
        helper.assertTrue(speed.hasModifier(SPEED_ID), "Equipping in either ring slot works while already crouching");
        ring.setDamageValue(ring.getMaxDamage());
        RingEffects.tick(player);
        helper.assertTrue(!speed.hasModifier(SPEED_ID), "A broken ring loses its bonus");

        handler.setStackInSlot(1, new ItemStack(PoptartRingItems.RING_OF_THE_RAT.get()));
        RingEffects.tick(player);
        handler.setStackInSlot(1, new ItemStack(PoptartRingItems.RUSTED_IRON_RING.get()));
        RingEffects.tick(player);
        helper.assertTrue(
                !speed.hasModifier(SPEED_ID) && speed.getValue() == base && speed.getModifier(other.id()) == other,
                "Replacing the ring preserves unrelated movement modifiers");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ratBonusRecoversFromExternalModifierChanges(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        new PlayerAccessoryHandler(player).setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_THE_RAT.get()));
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        double base = speed.getValue();
        crouch(player, true);
        RingEffects.tick(player);
        speed.removeModifier(SPEED_ID);
        RingEffects.tick(player);
        helper.assertTrue(
                Math.abs(speed.getValue() - base * 1.15) < .000001, "Removed bonus is restored when still applicable");

        speed.removeModifier(SPEED_ID);
        speed.addTransientModifier(new AttributeModifier(SPEED_ID, 1, AttributeModifier.Operation.ADD_VALUE));
        RingEffects.tick(player);
        helper.assertTrue(
                Math.abs(speed.getValue() - base * 1.15) < .000001
                        && speed.getModifier(SPEED_ID).operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                "Changed bonus is corrected without stacking the wrong operation");
        helper.succeed();
    }

    private static void crouch(Player player, boolean crouching) {
        player.setShiftKeyDown(crouching);
        player.setPose(crouching ? Pose.CROUCHING : Pose.STANDING);
    }

    private static void clearSpeedUpdates(Player player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        player.getAttributes().getAttributesToUpdate().remove(speed);
        player.getAttributes().getAttributesToSync().remove(speed);
    }
}
