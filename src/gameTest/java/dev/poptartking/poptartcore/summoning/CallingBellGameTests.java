package dev.poptartking.poptartcore.summoning;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CallingBellGameTests {
    @SubscribeEvent
    public static void template(ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("calling_bells_test"))
                .load(
                        event.getServer()
                                .registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                        TagParser.parseTag(
                                "{size:[24,10,24],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void allayBellSummonsThreeRecallsAndCanSummonImmediately(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var bell = PoptartToolItems.ALLAY_CALLING_BELL.get();
            helper.assertTrue(
                    bell.getDefaultMaxStackSize() == 1 && bell.count == 3 && bell.cooldown == 0,
                    "Allay bell matches the non-stackable three-helper reference item");
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bell));
            helper.assertTrue(
                    bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND)
                            .getResult()
                            .consumesAction(),
                    "Bell can ring");
            var members = members(owner, PixieType.ALLAY);
            helper.assertTrue(
                    members.size() == 3 && members.stream().allMatch(e -> e instanceof Allay),
                    "Exactly three allays appear");
            helper.assertTrue(!owner.getCooldowns().isOnCooldown(bell), "Summoning leaves recall available");
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    members.stream().allMatch(Entity::isRemoved)
                            && !PixieSummons.hasActive(helper.getLevel(), owner, PixieType.ALLAY),
                    "Second ring recalls the complete group");
            helper.assertTrue(!owner.getCooldowns().isOnCooldown(bell), "Recall has no cooldown");
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(members(owner, PixieType.ALLAY).size() == 3, "Bell can summon again immediately");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void vexBellSummonsFiveChargesHealthAndCanSummonImmediately(GameTestHelper helper) {
        // ServerPlayer has login damage immunity; this ordinary Player exercises the health cost directly.
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(10, 2, 10))));
        try {
            var bell = PoptartToolItems.VEX_CALLING_BELL.get();
            owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(bell));
            float health = owner.getHealth();
            bell.use(helper.getLevel(), owner, InteractionHand.OFF_HAND);
            var members = owner
                    .getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)
                    .getByType(PixieType.VEX)
                    .getFirst()
                    .getMembers()
                    .stream()
                    .map(id -> helper.getLevel().getEntity(id))
                    .toList();
            helper.assertTrue(
                    bell.getDefaultMaxStackSize() == 1
                            && members.size() == 5
                            && members.stream().allMatch(e -> e instanceof Vex),
                    "Offhand bell summons five vexes");
            helper.assertTrue(owner.getHealth() == health - 3, "Summoning costs three health points");
            helper.assertTrue(
                    members.stream().allMatch(e -> ((Vex) e).getMainHandItem().is(Items.IRON_SWORD)),
                    "Vexes have their reference swords");
            bell.use(helper.getLevel(), owner, InteractionHand.OFF_HAND);
            helper.assertTrue(owner.getHealth() == health - 3, "Recall does not charge health again");
            helper.assertTrue(
                    members.stream().allMatch(Entity::isRemoved)
                            && bell.cooldown == 0
                            && !owner.getCooldowns().isOnCooldown(bell),
                    "Recall removes all vexes without a cooldown");
            bell.use(helper.getLevel(), owner, InteractionHand.OFF_HAND);
            helper.assertTrue(
                    owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)
                                    .getByType(PixieType.VEX)
                                    .getFirst()
                                    .getMembers()
                                    .size()
                            == 5,
                    "Bell can summon five vexes again immediately");
            helper.succeed();
        } finally {
            PixieSummons.dismiss(helper.getLevel(), owner, PixieType.VEX);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void recallsAreIsolatedBetweenPlayersAndBellTypes(GameTestHelper helper) {
        var owner = owner(helper);
        var other = owner(helper);
        try {
            var allayBell = PoptartToolItems.ALLAY_CALLING_BELL.get();
            var vexBell = PoptartToolItems.VEX_CALLING_BELL.get();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(allayBell));
            owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(vexBell));
            other.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(allayBell));
            allayBell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            allayBell.use(helper.getLevel(), other, InteractionHand.MAIN_HAND);
            vexBell.use(helper.getLevel(), owner, InteractionHand.OFF_HAND);
            var others = members(other, PixieType.ALLAY);
            var vexes = members(owner, PixieType.VEX);
            allayBell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    !PixieSummons.hasActive(helper.getLevel(), owner, PixieType.ALLAY)
                            && others.stream().noneMatch(Entity::isRemoved)
                            && vexes.stream().noneMatch(Entity::isRemoved),
                    "Recalling one type does not touch another type or another player's helpers");
            helper.succeed();
        } finally {
            cleanup(owner);
            cleanup(other);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void allaysCollectSixteenAndDeliverWithoutDuplicatingItems(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var allay = allay(helper, owner);
            allay.moveTo(owner.getX() + .5, owner.getY(), owner.getZ());
            var ground = drop(helper, new ItemStack(Items.DIAMOND, 20), allay.position());
            AllaySummonController.tick(helper.getLevel(), allay, allay.getData(PoptartCoreAttachments.ALLAY_DATA));
            helper.assertTrue(
                    allay.getMainHandItem().getCount() == 16 && ground.getItem().getCount() == 4,
                    "Collects at most sixteen matching items");
            helper.assertTrue(
                    !allay.canPickUpLoot(), "Summon controller owns pickup rather than a second vanilla inventory");
            AllaySummonController.tick(helper.getLevel(), allay, allay.getData(PoptartCoreAttachments.ALLAY_DATA));
            helper.assertTrue(
                    allay.getMainHandItem().isEmpty() && groundCount(helper, owner.position(), Items.DIAMOND) == 20,
                    "Delivery preserves all twenty items exactly once");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void allaysRespectUnstackableItemLimits(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var allay = allay(helper, owner);
            allay.moveTo(owner.getX() + 5, owner.getY(), owner.getZ());
            drop(helper, new ItemStack(Items.DIAMOND_PICKAXE), allay.position());
            drop(helper, new ItemStack(Items.DIAMOND_PICKAXE), allay.position());
            var data = allay.getData(PoptartCoreAttachments.ALLAY_DATA);
            AllaySummonController.tick(helper.getLevel(), allay, data);
            AllaySummonController.tick(helper.getLevel(), allay, data);
            helper.assertTrue(
                    allay.getMainHandItem().is(Items.DIAMOND_PICKAXE)
                            && allay.getMainHandItem().getCount() == 1
                            && groundCount(helper, allay.position(), Items.DIAMOND_PICKAXE) == 1,
                    "Helpers cannot combine tools into illegal stacks");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void allayBellLuresAnimalsUsingFoodAndReturnsFoodOnRecall(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var bell = PoptartToolItems.ALLAY_CALLING_BELL.get();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bell));
            owner.getInventory().setItem(1, new ItemStack(Items.WHEAT, 5));
            var cow = helper.spawn(EntityType.COW, new BlockPos(12, 2, 10));
            bell.interactLivingEntity(owner.getMainHandItem(), owner, cow, InteractionHand.MAIN_HAND);
            var group = owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)
                    .getByType(PixieType.ALLAY)
                    .getFirst();
            helper.assertTrue(
                    group.getLureType() == EntityType.COW && group.getMembers().size() == 3,
                    "Using the bell on an animal chooses lure mode");
            for (var member : members(owner, PixieType.ALLAY)) {
                var allay = (Allay) member;
                AllaySummonController.tick(helper.getLevel(), allay, allay.getData(PoptartCoreAttachments.ALLAY_DATA));
                helper.assertTrue(
                        allay.getMainHandItem().is(Items.WHEAT)
                                && allay.getMainHandItem().getCount() == 1,
                        "Each helper carries one lure item");
            }
            helper.assertTrue(
                    owner.getInventory().countItem(Items.WHEAT) == 2,
                    "Helpers take three actual food items from the owner");
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    groundCount(helper, owner.position(), Items.WHEAT) == 3,
                    "Recall returns the borrowed food without consuming it");
            cow.discard();
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void recallReturnsEveryCarriedStackExactlyOnce(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var first = allay(helper, owner);
            var group = members(owner, PixieType.ALLAY);
            for (var member : group)
                ((Allay) member).setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD, 2));
            PixieSummons.dismiss(helper.getLevel(), owner, PixieType.ALLAY);
            PixieSummons.remove(helper.getLevel(), first);
            helper.assertTrue(
                    group.stream().allMatch(Entity::isRemoved)
                            && groundCount(helper, owner.position(), Items.EMERALD) == 6,
                    "Recall and repeated cleanup release six emeralds exactly once");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void helperDeathsUpdateOwnershipWithoutCooldownWhenLastDies(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            allay(helper, owner);
            var group = members(owner, PixieType.ALLAY);
            ((Allay) group.getFirst()).setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT, 4));
            for (int i = 0; i < group.size(); i++) {
                var member = (LivingEntity) group.get(i);
                member.hurt(helper.getLevel().damageSources().genericKill(), Float.MAX_VALUE);
                helper.assertTrue(
                        members(owner, PixieType.ALLAY).size() == group.size() - i - 1,
                        "Deaths remove only the dead member from the ownership record");
            }
            helper.assertTrue(
                    !PixieSummons.hasActive(helper.getLevel(), owner, PixieType.ALLAY)
                            && !owner.getCooldowns().isOnCooldown(PoptartToolItems.ALLAY_CALLING_BELL.get()),
                    "Last death clears the active group without a cooldown");
            helper.assertTrue(
                    groundCount(helper, owner.position(), Items.GOLD_INGOT) == 4,
                    "Death releases the carried stack once");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void unloadedMembersStayTrackedAndDismissAfterReload(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var member = allay(helper, owner);
            member.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.AMETHYST_SHARD, 2));
            var saved = member.saveWithoutId(new CompoundTag());
            member.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            helper.assertTrue(
                    PixieSummons.hasActive(helper.getLevel(), owner, PixieType.ALLAY)
                            && owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)
                                            .getByType(PixieType.ALLAY)
                                            .getFirst()
                                            .getMembers()
                                            .size()
                                    == 3,
                    "Chunk unloading must not erase a helper or allow another group");
            PixieSummons.dismiss(helper.getLevel(), owner, PixieType.ALLAY);
            var reloaded = EntityType.ALLAY.create(helper.getLevel());
            reloaded.load(saved);
            helper.getLevel().addFreshEntity(reloaded);
            AllaySummonController.tick(
                    helper.getLevel(), reloaded, reloaded.getData(PoptartCoreAttachments.ALLAY_DATA));
            helper.assertTrue(
                    reloaded.isRemoved() && groundCount(helper, owner.position(), Items.AMETHYST_SHARD) == 2,
                    "A recalled unloaded helper dismisses safely when loaded again");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void droppingBellDismissesItsHelpers(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            allay(helper, owner);
            var group = members(owner, PixieType.ALLAY);
            owner.getInventory().clearContent();
            owner.tickCount = 10;
            SummonEvents.onPlayerTick(new PlayerTickEvent.Post(owner));
            helper.assertTrue(
                    group.stream().allMatch(Entity::isRemoved)
                            && !PixieSummons.hasActive(helper.getLevel(), owner, PixieType.ALLAY),
                    "Losing the bell recalls its helpers");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void vexesChooseEnemiesAndProtectOwnerAndOtherHelpers(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var bell = PoptartToolItems.VEX_CALLING_BELL.get();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bell));
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            var vex = (Vex) members(owner, PixieType.VEX).getFirst();
            var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(13, 2, 10));
            var cow = helper.spawn(EntityType.COW, new BlockPos(12, 2, 10));
            helper.assertTrue(
                    VexOwnerTargetGoal.findTarget(vex, owner) == zombie,
                    "Vexes select nearby hostiles rather than their owner, siblings or passive animals");
            owner.setLastHurtMob(cow);
            helper.assertTrue(
                    VexOwnerTargetGoal.findTarget(vex, owner) == cow, "The owner's attacked target has priority");
            vex.setTarget(owner);
            vex.tick();
            helper.assertTrue(
                    vex.getTarget() != owner
                            && vex.targetSelector.getAvailableGoals().size() == 1,
                    "Summoned vexes use the owner-aware goal instead of vanilla player targeting");
            zombie.discard();
            cow.discard();
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void unboundAllaysAndVexesRetainVanillaBehavior(GameTestHelper helper) {
        var wildAllay = helper.spawn(EntityType.ALLAY, new BlockPos(10, 2, 10));
        var wildVex = helper.spawn(EntityType.VEX, new BlockPos(13, 2, 10));
        wildAllay.tickCount = 601;
        wildVex.tickCount = 601;
        wildAllay.tick();
        wildVex.tick();
        helper.assertTrue(
                wildAllay.isAlive()
                        && wildVex.isAlive()
                        && !PixieSummons.isSummoned(wildAllay)
                        && !PixieSummons.isSummoned(wildVex),
                "Ordinary mobs do not gain summon expiry or tracking attachments");
        helper.assertTrue(
                wildVex.targetSelector.getAvailableGoals().size() == 3, "Ordinary vex targeting stays intact");
        wildAllay.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        helper.assertTrue(wildAllay.canPickUpLoot(), "Ordinary allay pickup remains enabled");
        wildAllay.discard();
        wildVex.discard();
        helper.succeed();
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void phasingDamageProtectionOnlyAppliesToSummonedHelpers(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var summoned = allay(helper, owner);
            var wild = helper.spawn(EntityType.ALLAY, new BlockPos(13, 2, 10));
            for (var damage : List.of(
                    helper.getLevel().damageSources().fall(),
                    helper.getLevel().damageSources().inWall())) {
                var protectedEvent = new LivingIncomingDamageEvent(
                        summoned, new net.neoforged.neoforge.common.damagesource.DamageContainer(damage, 2));
                var ordinaryEvent = new LivingIncomingDamageEvent(
                        wild, new net.neoforged.neoforge.common.damagesource.DamageContainer(damage, 2));
                SummonEvents.onPixieDamage(protectedEvent);
                SummonEvents.onPixieDamage(ordinaryEvent);
                helper.assertTrue(
                        protectedEvent.isCanceled() && !ordinaryEvent.isCanceled(),
                        "Only marked summons receive fall/suffocation protection");
            }
            wild.discard();
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void entityAndOwnerAttachmentsSurviveSaveLoad(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var allay = allay(helper, owner);
            var data = allay.getData(PoptartCoreAttachments.ALLAY_DATA);
            data.setTargetUuid(UUID.randomUUID());
            data.setIdleTicks(17);
            var saved = allay.saveWithoutId(new CompoundTag());
            var restored = EntityType.ALLAY.create(helper.getLevel());
            restored.load(saved);
            var copy = restored.getData(PoptartCoreAttachments.ALLAY_DATA);
            helper.assertTrue(
                    owner.getUUID().equals(copy.getOwnerUuid())
                            && data.getSummonId().equals(copy.getSummonId())
                            && data.getTargetUuid().equals(copy.getTargetUuid())
                            && copy.getIdleTicks() == 17,
                    "Entity saves preserve owner, group and AI state");
            var groups = new PixieSummonerData();
            groups.deserializeNBT(
                    helper.getLevel().registryAccess(),
                    owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)
                            .serializeNBT(helper.getLevel().registryAccess()));
            helper.assertTrue(
                    groups.getByType(PixieType.ALLAY).size() == 1
                            && groups.get(data.getSummonId()).getMembers().size() == 3,
                    "Owner saves preserve group membership");
            var invalid = new CompoundTag();
            var list = new ListTag();
            var badGroup = new CompoundTag();
            badGroup.putString("Type", "not-a-pixie");
            list.add(badGroup);
            invalid.put("Summons", list);
            groups.deserializeNBT(helper.getLevel().registryAccess(), invalid);
            helper.assertTrue(groups.isEmpty(), "Malformed saved groups do not crash world loading");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void phasingResumesAfterSaveAndRestoresPhysicalState(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = owner(helper);
        try {
            var allay = allay(helper, owner);
            var from = Vec3.atCenterOf(helper.absolutePos(new BlockPos(10, 2, 10)));
            var to = from.add(5, 0, 0);
            level.setBlock(helper.absolutePos(new BlockPos(12, 2, 10)), Blocks.STONE.defaultBlockState(), 3);
            var plan = PhasingMover.planPhase(level, from, to);
            helper.assertTrue(plan.hasSolid() && plan.exit().x > plan.entry().x, "Thin walls have a phase route");
            allay.moveTo(from);
            allay.setNoGravity(false);
            var phase = allay.getData(PoptartCoreAttachments.PHASE_STATE);
            PhasingMover.moveToward(level, allay, phase, to, 1, PixieType.ALLAY);
            allay.moveTo(phase.getEntry());
            PhasingMover.moveToward(level, allay, phase, to, 1, PixieType.ALLAY);
            helper.assertTrue(
                    phase.getState() == PhaseState.PHASING && allay.isInvisible() && allay.isInvulnerable(),
                    "Phase hides the helper during its delay");
            var restored = EntityType.ALLAY.create(level);
            restored.load(allay.saveWithoutId(new CompoundTag()));
            var restoredPhase = restored.getData(PoptartCoreAttachments.PHASE_STATE);
            for (int i = 0; i < 6; i++) PhasingMover.moveToward(level, restored, restoredPhase, to, 1, PixieType.ALLAY);
            helper.assertTrue(
                    restoredPhase.getState() == PhaseState.IDLE
                            && !restored.isInvisible()
                            && !restored.isInvulnerable()
                            && !restored.isNoGravity()
                            && !restored.noPhysics,
                    "Saved phases complete without permanently hiding or protecting the helper");
            helper.assertTrue(restored.position().distanceToSqr(plan.exit()) < .01, "Restored helper reaches the exit");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void recalledUnloadedVexDismissesWhenItsChunkReturns(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            var bell = PoptartToolItems.VEX_CALLING_BELL.get();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bell));
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            var member = (Vex) members(owner, PixieType.VEX).getFirst();
            var saved = member.saveWithoutId(new CompoundTag());
            member.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            helper.assertTrue(
                    PixieSummons.hasActive(helper.getLevel(), owner, PixieType.VEX), "Unloaded vexes stay tracked");
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            var restored = EntityType.VEX.create(helper.getLevel());
            restored.load(saved);
            helper.getLevel().addFreshEntity(restored);
            restored.tick();
            helper.assertTrue(
                    restored.isRemoved() && !PixieSummons.hasActive(helper.getLevel(), owner, PixieType.VEX),
                    "A saved vex cannot outlive a recalled summon group");
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 40)
    public static void idleHelpersVanishAtTheirReferenceTimeouts(GameTestHelper helper) {
        var owner = owner(helper);
        try {
            allay(helper, owner);
            for (var member : members(owner, PixieType.ALLAY)) {
                var allay = (Allay) member;
                var data = allay.getData(PoptartCoreAttachments.ALLAY_DATA);
                data.setIdleTicks(59);
                AllaySummonController.tick(helper.getLevel(), allay, data);
                helper.assertTrue(!allay.isRemoved(), "Allays keep their full sixty-tick idle grace");
                AllaySummonController.tick(helper.getLevel(), allay, data);
                helper.assertTrue(allay.isRemoved(), "Idle allays vanish after sixty ticks");
            }
            var bell = PoptartToolItems.VEX_CALLING_BELL.get();
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bell));
            bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
            for (var member : members(owner, PixieType.VEX)) {
                var vex = (Vex) member;
                var data = vex.getData(PoptartCoreAttachments.VEX_DATA);
                data.setIdleTicks(199);
                vex.tick();
                helper.assertTrue(!vex.isRemoved(), "Vexes keep their full two-hundred-tick idle grace");
                vex.tick();
                helper.assertTrue(vex.isRemoved(), "Idle vexes vanish after two hundred ticks");
            }
            helper.succeed();
        } finally {
            cleanup(owner);
        }
    }

    @GameTest(template = "calling_bells_test", timeoutTicks = 120)
    public static void summonedAllaysActuallyFlyToAndCollectDroppedItems(GameTestHelper helper) {
        var owner = owner(helper);
        for (int x = 6; x <= 17; x++) {
            for (int z = 6; z <= 17; z++) {
                helper.getLevel()
                        .setBlock(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.STONE.defaultBlockState(), 3);
            }
        }
        var item =
                drop(helper, new ItemStack(Items.DIAMOND, 3), owner.position().add(0, 0, 4));
        allay(helper, owner);
        helper.runAtTickTime(80, () -> {
            try {
                helper.assertTrue(
                        item.isRemoved(), "The real entity AI moves helpers to the dropped stack and collects it");
                helper.succeed();
            } finally {
                cleanup(owner);
            }
        });
    }

    private static FakePlayer owner(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "bell-test"));
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(10, 2, 10))));
        helper.getLevel().addNewPlayer(player);
        return player;
    }

    private static Allay allay(GameTestHelper helper, ServerPlayer owner) {
        var bell = PoptartToolItems.ALLAY_CALLING_BELL.get();
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bell));
        bell.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        return (Allay) members(owner, PixieType.ALLAY).getFirst();
    }

    private static List<Entity> members(ServerPlayer owner, PixieType type) {
        var result = new ArrayList<Entity>();
        for (var group :
                owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA).getByType(type)) {
            for (var id : group.getMembers()) {
                var member = PixieSummons.findEntity(owner.serverLevel(), id);
                if (member != null && member.isAlive()) result.add(member);
            }
        }
        return result;
    }

    private static ItemEntity drop(GameTestHelper helper, ItemStack stack, Vec3 position) {
        var item = new ItemEntity(helper.getLevel(), position.x, position.y, position.z, stack);
        item.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    private static int groundCount(GameTestHelper helper, Vec3 at, net.minecraft.world.item.Item item) {
        return helper
                .getLevel()
                .getEntitiesOfClass(ItemEntity.class, new AABB(BlockPos.containing(at)).inflate(10))
                .stream()
                .filter(e -> e.getItem().is(item))
                .mapToInt(e -> e.getItem().getCount())
                .sum();
    }

    private static void cleanup(ServerPlayer owner) {
        for (var type : PixieType.values()) PixieSummons.dismiss(owner.serverLevel(), owner, type);
        owner.discard();
    }
}
