package dev.poptartking.poptartcore.equipment;

import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.items.NMLItems;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FourRingGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bulwarkReducesActualExplosionAndAddsResistance(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        var stack = new ItemStack(PoptartRingItems.BULWARK_RING.get());
        helper.assertTrue(
                handler.isItemValid(0, stack),
                "Bulwark fits ring slot: " + stack + " type=" + stack.getItem().getClass() + " tag="
                        + AccessoryType.RING.accepts(stack) + " healthy=" + stack.getDamageValue() + "/"
                        + stack.getMaxDamage());
        handler.setStackInSlot(0, stack);
        helper.assertTrue(
                Math.abs(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - .05) < .0001,
                "5% knockback resistance");
        player.hurt(player.damageSources().explosion(null, null), 10);
        helper.assertTrue(Math.abs(player.getHealth() - 12) < .001, "Explosion reduced from 10 to 8 damage");
        helper.assertTrue(
                stack.getItem().isValidRepairItem(stack, new ItemStack(PoptartMaterialItems.LEAD_INGOT.get())),
                "Lead repairs Bulwark");
        handler.setStackInSlot(0, ItemStack.EMPTY);
        helper.assertTrue(
                player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0, "Unequipping removes resistance");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ratSneakSpeedAndDetection(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        double base = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_THE_RAT.get()));
        player.setShiftKeyDown(true);
        player.setPose(net.minecraft.world.entity.Pose.CROUCHING);
        RingEffects.tick(player);
        helper.assertTrue(
                Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - base * 1.15) < .00001,
                "Rat grants 15% crouch speed: " + player.getAttributeValue(Attributes.MOVEMENT_SPEED) + " base=" + base
                        + " crouch=" + player.isCrouching() + " equipped="
                        + EquipmentRing.has(player, EquipmentRing.Kind.RAT));
        var visibility = new net.neoforged.neoforge.event.entity.living.LivingEvent.LivingVisibilityEvent(
                player, net.minecraft.world.entity.EntityType.ZOMBIE.create(helper.getLevel()), 1);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(visibility);
        helper.assertTrue(
                Math.abs(visibility.getVisibilityModifier() - .8) < .00001, "Rat lowers mob visibility by 20%");
        player.setShiftKeyDown(false);
        player.setPose(net.minecraft.world.entity.Pose.STANDING);
        RingEffects.tick(player);
        helper.assertTrue(
                Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - base) < .00001,
                "Standing removes speed bonus");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void sulfurReducesActualCorrosion(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PoptartRingItems.SULFUR_GEMSTONE_RING.get());
        new PlayerAccessoryHandler(player).setStackInSlot(0, stack);
        player.hurt(
                new DamageSource(helper.getLevel()
                        .registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(NMLDamageTypes.CORROSION)),
                10);
        helper.assertTrue(Math.abs(player.getHealth() - 13) < .001, "Corrosion reduced from 10 to 7 damage");
        helper.assertTrue(
                stack.getItem().isValidRepairItem(stack, new ItemStack(NMLItems.SULFUR.get())),
                "Sulfur repairs Sulfur ring");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void silenceActivationDamageMovementAndRemoval(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_SILENCE.get()));
        player.tickCount = 100;
        for (int i = 0; i < 10; i++) SilenceRing.tick(player);
        helper.assertTrue(!SilenceRing.isIntangible(player), "Requires ten full still ticks");
        SilenceRing.tick(player);
        helper.assertTrue(
                SilenceRing.isIntangible(player)
                        && player.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY),
                "Stillness activates invisibility and protection: active=" + SilenceRing.isIntangible(player)
                        + " invisible=" + player.isInvisible() + " equipped="
                        + EquipmentRing.has(player, EquipmentRing.Kind.SILENCE));
        helper.assertTrue(!player.isPushable(), "Active Silence prevents entity pushing");
        player.hurt(player.damageSources().magic(), 8);
        helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Active Silence cancels actual damage");
        player.setPos(player.position().add(.1, 0, 0));
        SilenceRing.tick(player);
        helper.assertTrue(
                !SilenceRing.isIntangible(player)
                        && !player.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY),
                "Movement cancels Silence");
        for (int i = 0; i < 11; i++) SilenceRing.tick(player);
        // No Man's Land's unrelated listener loads client code on the dedicated test server.
        // Exercise our subscribed listener with the real event without invoking that external listener.
        SilenceRing.rightItem(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(
                player, net.minecraft.world.InteractionHand.MAIN_HAND));
        helper.assertTrue(!SilenceRing.isIntangible(player), "Interaction cancels Silence");
        for (int i = 0; i < 11; i++) SilenceRing.tick(player);
        handler.setStackInSlot(0, ItemStack.EMPTY);
        SilenceRing.tick(player);
        helper.assertTrue(
                !SilenceRing.isIntangible(player) && player.isPushable(),
                "Unequipping clears protection and push state");
        helper.succeed();
    }
}
