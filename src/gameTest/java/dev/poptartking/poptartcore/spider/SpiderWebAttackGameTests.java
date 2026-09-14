package dev.poptartking.poptartcore.spider;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpiderWebAttackGameTests {
    private SpiderWebAttackGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void changingTargetRestartsPreparationWithoutResettingCooldown(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        spider.setNoAi(true);
        var first = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        var second = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 6));
        first.setNoAi(true);
        second.setNoAi(true);
        spider.setTarget(first);
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        goal.start();
        for (int tick = 0; tick < 59; tick++) {
            goal.tick();
        }
        spider.setTarget(second);
        for (int tick = 0; tick < 59; tick++) {
            goal.tick();
        }
        helper.assertTrue(countShots(helper, spider) == 0, "New target inherited preparation progress");
        goal.tick();
        helper.assertTrue(countShots(helper, spider) == 1, "New target did not receive a shot after full preparation");
        spider.setTarget(first);
        // Run another preparation without advancing world time: the shot cooldown must still block firing.
        for (int tick = 0; tick < 60; tick++) {
            goal.tick();
        }
        helper.assertTrue(countShots(helper, spider) == 1, "Target change reset the shot cooldown");
        helper.succeed();
    }

    private static long countShots(GameTestHelper helper, Spider spider) {
        return helper
                .getLevel()
                .getEntitiesOfClass(WebProjectile.class, spider.getBoundingBox().inflate(3))
                .stream()
                .filter(shot -> shot.getOwner() == spider)
                .count();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void webCatchAlertsOnlyNearbyRegularSpidersWithSameTarget(GameTestHelper helper) {
        Spider shooter = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        var target = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        var otherTarget = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 6));
        Spider ally = helper.spawn(EntityType.SPIDER, new BlockPos(3, 2, 1));
        Spider differentTarget = helper.spawn(EntityType.SPIDER, new BlockPos(5, 2, 1));
        Spider idle = helper.spawn(EntityType.SPIDER, new BlockPos(6, 2, 1));
        Spider cave = helper.spawn(EntityType.CAVE_SPIDER, new BlockPos(7, 2, 1));
        Spider distant = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 2));
        distant.setPos(shooter.getX() + 11, shooter.getY(), shooter.getZ());
        for (Spider spider : new Spider[] {shooter, ally, differentTarget, idle, cave, distant}) {
            spider.setNoAi(true);
        }
        shooter.setTarget(target);
        ally.setTarget(target);
        cave.setTarget(target);
        distant.setTarget(target);
        differentTarget.setTarget(otherTarget);
        ((WebShootingSpider) ally).poptartcore$setWebShooting(true);
        new WebProjectile(helper.getLevel(), shooter).onHitEntity(new net.minecraft.world.phys.EntityHitResult(target));
        helper.assertTrue(
                ((WebShootingSpider) ally).poptartcore$isPursuingWebbedTarget(target), "Nearby ally missed web alert");
        helper.assertTrue(
                !((WebShootingSpider) ally).poptartcore$isWebShooting(), "Alerted ally retained shooting pose");
        for (Spider excluded : new Spider[] {differentTarget, idle, cave, distant}) {
            helper.assertTrue(
                    !((WebShootingSpider) excluded).poptartcore$isPursuingWebbedTarget(target),
                    "Excluded spider received alert");
        }
        helper.assertTrue(
                differentTarget.getTarget() == otherTarget && idle.getTarget() == null,
                "Alert changed unrelated targets");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void rangedAttackDoesNotFireThroughWall(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(2, 2, 1));
        spider.setNoAi(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 6));
        target.setNoAi(true);
        spider.setTarget(target);
        for (int x = 0; x <= 4; x++) {
            for (int y = 1; y <= 5; y++) {
                helper.setBlock(new BlockPos(x, y, 3), net.minecraft.world.level.block.Blocks.STONE);
            }
        }
        helper.assertTrue(!spider.getSensing().hasLineOfSight(target), "Wall did not block sight in test");
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        goal.start();
        for (int tick = 0; tick < 60; tick++) {
            goal.tick();
        }
        helper.assertTrue(
                helper
                        .getLevel()
                        .getEntitiesOfClass(
                                WebProjectile.class, spider.getBoundingBox().inflate(3))
                        .stream()
                        .noneMatch(shot -> shot.getOwner() == spider),
                "Spider fired through wall");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void referenceDistanceBandKeepsThenResumesChasing(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        spider.setNoAi(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        target.setNoAi(true);
        spider.setTarget(target);
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        goal.start();
        goal.tick();
        helper.assertTrue(spider.getMoveControl().getSpeedModifier() == 0, "Spider did not stop within 9.5 blocks");

        target.setPos(spider.getX(), spider.getY(), spider.getZ() + 11);
        goal.tick();
        helper.assertTrue(goal.canUse(), "Retreating beyond nine blocks cancelled ranged attack");
        helper.assertTrue(
                spider.getMoveControl().getSpeedModifier() == 0, "Spider chased too soon within distance band");

        target.setPos(spider.getX(), spider.getY(), spider.getZ() + 14);
        goal.tick();
        spider.getNavigation().tick();
        helper.assertTrue(
                spider.getMoveControl().getSpeedModifier() > 0, "Spider did not resume chase beyond 13.4 blocks");
        helper.assertTrue(((WebShootingSpider) spider).poptartcore$isWebShooting(), "Chasing dropped ranged pose");

        target.setPos(spider.getX(), spider.getY(), spider.getZ() + 11);
        goal.tick();
        spider.getNavigation().tick();
        helper.assertTrue(
                spider.getMoveControl().getSpeedModifier() > 0, "Returning into band stopped pursuit too soon");

        target.setPos(spider.getX(), spider.getY(), spider.getZ() + 9);
        goal.tick();
        helper.assertTrue(spider.getMoveControl().getSpeedModifier() == 0, "Spider did not stop at inner threshold");
        goal.stop();
        helper.assertTrue(
                !((WebShootingSpider) spider).poptartcore$isWebShooting(), "Leaving ranged attack retained pose");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void preparationClearsClimbingPursuit(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        spider.setNoAi(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        target.setNoAi(true);
        spider.setTarget(target);
        spider.getNavigation().moveTo(target, 1);
        spider.getNavigation().stop();
        spider.getNavigation().tick();
        helper.assertTrue(spider.getMoveControl().hasWanted(), "Test did not reproduce fallback chasing");
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        goal.start();
        goal.tick();
        spider.getNavigation().tick();
        helper.assertTrue(spider.getMoveControl().getWantedZ() == spider.getZ(), "Fallback navigation resumed chasing");
        helper.assertTrue(spider.getMoveControl().getSpeedModifier() == 0, "Preparation retained chase speed");
        spider.getMoveControl().tick();
        helper.assertTrue(spider.getSpeed() == 0, "Spider still walking during preparation");
        goal.stop();
        spider.getNavigation().moveTo(target, 1);
        spider.getNavigation().tick();
        helper.assertTrue(spider.getMoveControl().getSpeedModifier() > 0, "Spider could not resume pursuing afterward");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 120)
    public static void successfulCatchSuppressesRangedAttackForWebLifetime(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        spider.setNoAi(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        target.setNoAi(true);
        spider.setTarget(target);
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        WebProjectile shot = new WebProjectile(helper.getLevel(), spider);
        shot.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target));
        WebShootingSpider state = (WebShootingSpider) spider;
        helper.assertTrue(state.poptartcore$isPursuingWebbedTarget(target), "Web catch did not trigger melee pursuit");
        helper.assertTrue(!goal.canUse(), "Caught target still allowed ranged attacks");
        var other = helper.spawn(EntityType.PIG, new BlockPos(3, 2, 6));
        helper.assertTrue(!state.poptartcore$isPursuingWebbedTarget(other), "Catch state leaked to another target");
        helper.runAtTickTime(101, () -> {
            helper.assertTrue(!state.poptartcore$isPursuingWebbedTarget(target), "Melee pursuit state never expired");
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void webAttackWarmsUpFiresAndKeepsRangedPose(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        spider.setNoAi(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        target.setNoAi(true);
        spider.setTarget(target);
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        WebShootingSpider pose = (WebShootingSpider) spider;
        helper.assertTrue(goal.canUse(), "Spider cannot start ranged attack");
        goal.start();
        goal.tick();
        helper.assertTrue(pose.poptartcore$isWebShooting(), "Preparation pose was not synchronized");
        for (int tick = 1; tick < 59; tick++) {
            goal.tick();
        }
        helper.assertTrue(
                helper
                        .getLevel()
                        .getEntitiesOfClass(
                                WebProjectile.class, spider.getBoundingBox().inflate(3))
                        .stream()
                        .noneMatch(shot -> shot.getOwner() == spider),
                "Spider fired before three-second preparation");
        goal.tick();
        helper.assertTrue(pose.poptartcore$isWebShooting(), "Ranged pose dropped between shots");
        var shots = helper.getLevel()
                .getEntitiesOfClass(WebProjectile.class, spider.getBoundingBox().inflate(3));
        helper.assertTrue(
                shots.stream().anyMatch(shot -> shot.getOwner() == spider), "Spider did not spawn its projectile");
        goal.stop();
        goal.start();
        for (int tick = 0; tick < 60; tick++) {
            goal.tick();
        }
        helper.assertTrue(
                helper
                                .getLevel()
                                .getEntitiesOfClass(
                                        WebProjectile.class,
                                        spider.getBoundingBox().inflate(3))
                                .stream()
                                .filter(shot -> shot.getOwner() == spider)
                                .count()
                        == 1,
                "Restart bypassed cooldown");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void interruptionResetsPoseAndCaveSpidersCannotShoot(GameTestHelper helper) {
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 2, 1));
        spider.setNoAi(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 6));
        spider.setTarget(target);
        SpiderWebAttackGoal goal = new SpiderWebAttackGoal(spider);
        goal.start();
        goal.tick();
        goal.stop();
        helper.assertTrue(
                !((WebShootingSpider) spider).poptartcore$isWebShooting(), "Interrupted pose remained raised");
        Spider cave = helper.spawn(EntityType.CAVE_SPIDER, new BlockPos(2, 2, 1));
        cave.setTarget(target);
        helper.assertTrue(!new SpiderWebAttackGoal(cave).canUse(), "Cave spider can shoot webs");
        helper.succeed();
    }
}
