package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AllaySummonController {
    private static final int CARRY_CAP = 16;
    private static final int LIFESPAN = Integer.MAX_VALUE;
    private static final int IDLE_GRACE = 60;
    private static final int OWNER_GONE_GRACE = 200;
    private static final double COLLECT_SPEED = 1.75;
    private static final double DELIVER_SPEED = 2.25;
    private static final double IDLE_SPEED = 1.0;
    private static final double PICKUP_SQR = 2.0;
    private static final double DELIVER_SQR = 4.0;
    private static final double SEARCH_RADIUS = 20.0;
    private static final double AIM_RADIUS_SQR = 16.0;
    private static final double FRONT_RANGE_SQR = 256.0;
    private static final double FRONT_COS = 0.5;
    private static final double OWNER_RADIUS_SQR = 256.0;
    private static final double LURE_ANIMAL_RADIUS = 32.0;
    private static final double LURE_RANGE = 6.0;
    private static final double LURE_FOLLOW_SPEED = 1.15;

    public static void summon(ServerLevel level, Player summoner, Vec3 aim, int count) {
        PixieSummon summon = new PixieSummon(UUID.randomUUID(), PixieType.ALLAY, aim);
        spawnAndRegister(level, summoner, summon, count);
    }

    public static void summonLure(ServerLevel level, Player summoner, EntityType<?> animalType, int count) {
        PixieSummon summon = new PixieSummon(UUID.randomUUID(), PixieType.ALLAY, null);
        summon.setLureType(animalType);
        spawnAndRegister(level, summoner, summon, count);
    }

    private static void spawnAndRegister(ServerLevel level, Player summoner, PixieSummon summon, int count) {
        List<UUID> members = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Allay allay = (Allay) EntityType.ALLAY.create(level);
            if (allay != null) {
                double ox = summoner.getX() + (summoner.getRandom().nextDouble() - 0.5) * 2.0;
                double oz = summoner.getZ() + (summoner.getRandom().nextDouble() - 0.5) * 2.0;
                allay.moveTo(ox, summoner.getEyeY(), oz, summoner.getYRot(), 0.0F);
                allay.setPersistenceRequired();
                AllayData ad = (AllayData) allay.getData(PoptartCoreAttachments.ALLAY_DATA);
                ad.setOwnerUuid(summoner.getUUID());
                ad.setSummonId(summon.getId());
                ad.setLifespan(LIFESPAN);
                allay.setData(PoptartCoreAttachments.ALLAY_DATA, ad);
                level.addFreshEntity(allay);
                level.gameEvent(summoner, GameEvent.ENTITY_PLACE, allay.position());
                members.add(allay.getUUID());
                PhasingMover.pixieBurst(level, allay.getX(), allay.getY() + 0.5, allay.getZ(), 10, PixieType.ALLAY);
            }
        }

        summon.setMembers(members);
        PixieSummons.register(summoner, summon);
        level.playSound(
                null, summoner.blockPosition(), PoptartCoreSounds.ALLAY_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
        level.playSound(
                null, summoner.blockPosition(), PoptartCoreSounds.PIXIE_SHIMMER.get(), SoundSource.PLAYERS, 0.8F, 1.3F);
    }

    public static void tick(ServerLevel level, Allay allay, AllayData data) {
        int lifespan = data.getLifespan() - 1;
        data.setLifespan(lifespan);
        if (lifespan <= 0) {
            PixieSummons.remove(level, allay);
        } else {
            Entity ownerEntity = PixieSummons.findEntity(level, data.getOwnerUuid());
            ServerPlayer owner = ownerEntity instanceof ServerPlayer player ? player : null;
            if (owner != null
                    && owner.level() == level
                    && owner.isAlive()
                    && !owner.isRemoved()
                    && !owner.isSpectator()) {
                PixieSummon summon = ((PixieSummonerData) owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA))
                        .get(data.getSummonId());
                if (summon == null || !summon.getMembers().contains(allay.getUUID())) {
                    PixieSummons.remove(level, allay);
                } else {
                    PhaseState phase = (PhaseState) allay.getData(PoptartCoreAttachments.PHASE_STATE);
                    if (summon.getLureType() != null) {
                        lureTick(level, allay, owner, summon, data, phase, summon.getLureType());
                    } else {
                        ItemStack carried = allay.getItemInHand(InteractionHand.MAIN_HAND);
                        boolean canCollectMore = carried.isEmpty()
                                || carried.getCount() < Math.min(CARRY_CAP, carried.getMaxStackSize());
                        ItemEntity target = canCollectMore ? selectTarget(level, allay, owner, summon, carried) : null;
                        if (target != null) {
                            data.setTargetUuid(target.getUUID());
                            Vec3 itemPos = target.position().add(0.0, 0.2, 0.0);
                            PhasingMover.moveToward(level, allay, phase, itemPos, COLLECT_SPEED, PixieType.ALLAY);
                            if (phase.getState() == 0
                                    && allay.position().distanceToSqr(target.position()) < PICKUP_SQR) {
                                pickUp(level, allay, target);
                            }

                            data.setIdleTicks(0);
                        } else {
                            data.setTargetUuid(null);
                            if (!carried.isEmpty()) {
                                Vec3 ownerPos = orbit(owner, allay, 1.5, 1.2);
                                PhasingMover.moveToward(level, allay, phase, ownerPos, DELIVER_SPEED, PixieType.ALLAY);
                                if (phase.getState() == 0 && allay.distanceToSqr(owner) < DELIVER_SQR) {
                                    giveItemsToPlayer(level, allay, owner);
                                }

                                data.setIdleTicks(0);
                            } else {
                                int idle = data.getIdleTicks() + 1;
                                data.setIdleTicks(idle);
                                if (idle > IDLE_GRACE) {
                                    PixieSummons.remove(level, allay);
                                } else {
                                    Vec3 hover = orbit(owner, allay, 1.8, 2.3);
                                    PhasingMover.moveToward(level, allay, phase, hover, IDLE_SPEED, PixieType.ALLAY);
                                }
                            }
                        }
                    }
                }
            } else {
                PhasingMover.clearPhase(allay, (PhaseState) allay.getData(PoptartCoreAttachments.PHASE_STATE));
                int idle = data.getIdleTicks() + 1;
                data.setIdleTicks(idle);
                if (idle > OWNER_GONE_GRACE) {
                    PixieSummons.remove(level, allay);
                }
            }
        }
    }

    private static Vec3 orbit(ServerPlayer owner, Allay allay, double radius, double height) {
        double angle = (double) (allay.getUUID().hashCode() & 65535) / 65536.0 * (Math.PI * 2);
        return owner.position().add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private static void lureTick(
            ServerLevel level,
            Allay allay,
            ServerPlayer owner,
            PixieSummon summon,
            AllayData data,
            PhaseState phase,
            EntityType<?> lureType) {
        Animal reference = nearestAnimalOfType(level, owner, lureType);
        if (reference == null) {
            PixieSummons.remove(level, allay);
        } else {
            ItemStack carried = allay.getItemInHand(InteractionHand.MAIN_HAND);
            if (carried.getCount() > 1) {
                throwToOwner(level, allay, owner, carried.copyWithCount(carried.getCount() - 1));
                allay.setItemInHand(InteractionHand.MAIN_HAND, carried.copyWithCount(1));
                carried = allay.getItemInHand(InteractionHand.MAIN_HAND);
            }

            boolean holdingLure = !carried.isEmpty() && reference.isFood(carried);
            if (!holdingLure) {
                if (!carried.isEmpty()) {
                    throwToOwner(level, allay, owner, carried);
                    allay.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }

                if (acquireLure(level, allay, owner, reference)) {
                    data.setIdleTicks(0);
                } else {
                    int idle = data.getIdleTicks() + 1;
                    data.setIdleTicks(idle);
                    if (idle > IDLE_GRACE) {
                        PixieSummons.remove(level, allay);
                        return;
                    }

                    PhasingMover.moveToward(
                            level, allay, phase, orbit(owner, allay, 1.8, 2.3), IDLE_SPEED, PixieType.ALLAY);
                }
            } else {
                data.setIdleTicks(0);
                ItemStack lure = allay.getItemInHand(InteractionHand.MAIN_HAND);

                for (Animal follower : level.getEntitiesOfClass(
                        Animal.class, allay.getBoundingBox().inflate(LURE_RANGE), a -> a.isAlive() && a.isFood(lure))) {
                    follower.getNavigation().moveTo(allay.getX(), allay.getY(), allay.getZ(), LURE_FOLLOW_SPEED);
                }

                Animal stray = nearestStray(level, allay, owner, summon, lureType);
                if (stray != null) {
                    data.setTargetUuid(stray.getUUID());
                    PhasingMover.moveToward(
                            level, allay, phase, stray.position().add(0.0, 0.5, 0.0), COLLECT_SPEED, PixieType.ALLAY);
                } else {
                    data.setTargetUuid(null);
                    PhasingMover.moveToward(
                            level, allay, phase, orbit(owner, allay, 1.8, 2.3), IDLE_SPEED, PixieType.ALLAY);
                }
            }
        }
    }

    @Nullable
    private static Animal nearestAnimalOfType(ServerLevel level, ServerPlayer owner, EntityType<?> type) {
        Animal best = null;
        double bestDist = Double.MAX_VALUE;

        for (Animal animal : level.getEntitiesOfClass(
                Animal.class,
                owner.getBoundingBox().inflate(LURE_ANIMAL_RADIUS),
                a -> a.getType() == type && a.isAlive())) {
            double dist = owner.distanceToSqr(animal);
            if (dist < bestDist) {
                best = animal;
                bestDist = dist;
            }
        }

        return best;
    }

    @Nullable
    private static Animal nearestStray(
            ServerLevel level, Allay allay, ServerPlayer owner, PixieSummon summon, EntityType<?> type) {
        List<UUID> claimed = claimedBySiblings(level, summon, allay.getUUID());
        Animal best = null;
        double bestDist = Double.MAX_VALUE;

        for (Animal animal : level.getEntitiesOfClass(
                Animal.class,
                owner.getBoundingBox().inflate(LURE_ANIMAL_RADIUS),
                a -> a.getType() == type && a.isAlive())) {
            if (!claimed.contains(animal.getUUID()) && !isLured(level, summon, animal)) {
                double dist = allay.distanceToSqr(animal);
                if (dist < bestDist) {
                    best = animal;
                    bestDist = dist;
                }
            }
        }

        return best;
    }

    private static boolean isLured(ServerLevel level, PixieSummon summon, Animal animal) {
        for (UUID uuid : summon.getMembers()) {
            if (level.getEntity(uuid) instanceof Allay member
                    && member.distanceToSqr(animal) <= LURE_RANGE * LURE_RANGE) {
                return true;
            }
        }

        return false;
    }

    private static boolean acquireLure(ServerLevel level, Allay allay, ServerPlayer owner, Animal reference) {
        Inventory inventory = owner.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && reference.isFood(stack)) {
                allay.setItemInHand(InteractionHand.MAIN_HAND, stack.copyWithCount(1));
                stack.shrink(1);
                level.playSound(null, owner.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.6F, 1.4F);
                return true;
            }
        }

        Iterator var8 = level.getEntitiesOfClass(
                        ItemEntity.class,
                        allay.getBoundingBox().inflate(LURE_ANIMAL_RADIUS),
                        e -> e.isAlive() && !e.hasPickUpDelay() && reference.isFood(e.getItem()))
                .iterator();
        if (var8.hasNext()) {
            ItemEntity dropped = (ItemEntity) var8.next();
            ItemStack ground = dropped.getItem();
            allay.setItemInHand(InteractionHand.MAIN_HAND, ground.copyWithCount(1));
            ground.shrink(1);
            if (ground.isEmpty()) {
                dropped.discard();
            } else {
                dropped.setItem(ground);
            }

            level.playSound(null, allay.blockPosition(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }

    private static void throwToOwner(ServerLevel level, Allay allay, ServerPlayer owner, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity drop = new ItemEntity(level, allay.getX(), allay.getY() + 0.5, allay.getZ(), stack);
            Vec3 toOwner = owner.position().add(0.0, 1.0, 0.0).subtract(drop.position());
            if (toOwner.lengthSqr() > 1.0E-4) {
                drop.setDeltaMovement(toOwner.normalize().scale(0.3));
            }

            drop.setPickUpDelay(0);
            level.addFreshEntity(drop);
        }
    }

    @Nullable
    private static ItemEntity selectTarget(
            ServerLevel level, Allay allay, ServerPlayer owner, PixieSummon summon, ItemStack carried) {
        Vec3 aim = summon.getAim();
        Vec3 eye = owner.getEyePosition();
        Vec3 look = owner.getLookAngle();
        List<UUID> claimed = claimedBySiblings(level, summon, allay.getUUID());
        AABB box = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                itemx -> itemx.isAlive()
                        && !itemx.hasPickUpDelay()
                        && matches(carried, itemx.getItem())
                        && !claimed.contains(itemx.getUUID()));
        ItemEntity best = null;
        int bestRank = Integer.MAX_VALUE;
        double bestDist = Double.MAX_VALUE;

        for (ItemEntity item : items) {
            int rank = rank(item, aim, eye, look, owner);
            if (rank != Integer.MAX_VALUE) {
                double dist = allay.position().distanceToSqr(item.position());
                if (rank < bestRank || rank == bestRank && dist < bestDist) {
                    best = item;
                    bestRank = rank;
                    bestDist = dist;
                }
            }
        }

        return best;
    }

    private static int rank(ItemEntity item, @Nullable Vec3 aim, Vec3 eye, Vec3 look, Player owner) {
        Vec3 pos = item.position();
        if (aim != null && pos.distanceToSqr(aim) <= AIM_RADIUS_SQR) {
            return 0;
        } else {
            Vec3 toItem = pos.subtract(eye);
            if (toItem.lengthSqr() <= FRONT_RANGE_SQR
                    && toItem.lengthSqr() > 1.0E-4
                    && toItem.normalize().dot(look) >= FRONT_COS) {
                return 1;
            } else {
                return pos.distanceToSqr(owner.position()) <= OWNER_RADIUS_SQR ? 2 : Integer.MAX_VALUE;
            }
        }
    }

    private static List<UUID> claimedBySiblings(ServerLevel level, PixieSummon summon, UUID self) {
        List<UUID> claimed = new ArrayList<>();

        for (UUID uuid : summon.getMembers()) {
            if (!uuid.equals(self)) {
                Entity targetUuid = level.getEntity(uuid);
                if (targetUuid instanceof Allay) {
                    Allay sibling = (Allay) targetUuid;
                    UUID targetUuidx = ((AllayData) sibling.getData(PoptartCoreAttachments.ALLAY_DATA)).getTargetUuid();
                    if (targetUuidx != null) {
                        claimed.add(targetUuidx);
                    }
                }
            }
        }

        return claimed;
    }

    private static boolean matches(ItemStack carried, ItemStack candidate) {
        return carried.isEmpty() || ItemStack.isSameItemSameComponents(carried, candidate);
    }

    private static void pickUp(ServerLevel level, Allay allay, ItemEntity target) {
        ItemStack carried = allay.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack onGround = target.getItem();
        int space = Math.min(CARRY_CAP, onGround.getMaxStackSize()) - carried.getCount();
        if (space > 0) {
            int taken = Math.min(space, onGround.getCount());
            if (carried.isEmpty()) {
                allay.setItemInHand(InteractionHand.MAIN_HAND, onGround.copyWithCount(taken));
            } else {
                ItemStack grown = carried.copy();
                grown.grow(taken);
                allay.setItemInHand(InteractionHand.MAIN_HAND, grown);
            }

            onGround.shrink(taken);
            if (onGround.isEmpty()) {
                target.discard();
            } else {
                target.setItem(onGround);
            }

            level.playSound(null, allay.blockPosition(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    private static void giveItemsToPlayer(ServerLevel level, Allay allay, ServerPlayer owner) {
        ItemStack stack = allay.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stack.isEmpty()) {
            allay.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            ItemEntity drop = new ItemEntity(level, allay.getX(), allay.getY() + 0.5, allay.getZ(), stack);
            Vec3 toOwner = owner.position().add(0.0, 1.0, 0.0).subtract(drop.position());
            if (toOwner.lengthSqr() > 1.0E-4) {
                drop.setDeltaMovement(toOwner.normalize().scale(0.3));
            }

            drop.setPickUpDelay(0);
            level.addFreshEntity(drop);
            level.playSound(null, allay.blockPosition(), SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }
}
