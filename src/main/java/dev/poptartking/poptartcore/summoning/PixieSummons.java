package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Tracks bell summons independently of whether their entity chunks are currently loaded. */
public final class PixieSummons {
    public static final int OWNERLESS_LIFESPAN = 600;

    private PixieSummons() {}

    public static void register(Entity owner, PixieSummon summon) {
        var data = owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA);
        if (!summon.getMembers().isEmpty()) data.add(summon);
        owner.setData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA, data);
    }

    public static boolean hasActive(ServerLevel level, Entity owner, PixieType type) {
        return owner.hasData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)
                && owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA).getByType(type).stream()
                        .anyMatch(summon -> !summon.getMembers().isEmpty());
    }

    public static boolean isSummoned(Entity entity) {
        var data = data(entity);
        return data != null && data.getSummonId() != null;
    }

    @Nullable
    public static PixieData data(Entity entity) {
        if (entity instanceof Allay && entity.hasData(PoptartCoreAttachments.ALLAY_DATA)) {
            return entity.getData(PoptartCoreAttachments.ALLAY_DATA);
        }
        if (entity instanceof Vex && entity.hasData(PoptartCoreAttachments.VEX_DATA)) {
            return entity.getData(PoptartCoreAttachments.VEX_DATA);
        }
        return null;
    }

    @Nullable
    public static Entity findEntity(ServerLevel level, @Nullable UUID id) {
        if (id == null) return null;
        var player = level.getServer().getPlayerList().getPlayer(id);
        if (player != null) return player;
        for (var world : level.getServer().getAllLevels()) {
            var entity = world.getEntity(id);
            if (entity != null) return entity;
        }
        return null;
    }

    public static boolean isRegistered(Entity member, Entity owner, PixieData data) {
        if (!owner.hasData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)) return false;
        var summon = owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA).get(data.getSummonId());
        return summon != null && summon.getMembers().contains(member.getUUID());
    }

    public static void dismiss(ServerLevel level, Entity owner, PixieType type) {
        var data = owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA);
        var groups = new ArrayList<>(data.getByType(type));
        // Clear ownership first. Unloaded members also dismiss themselves when their chunks load.
        for (var group : groups) data.remove(group.getId());
        owner.setData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA, data);
        for (var group : groups) {
            for (var id : group.getMembers()) {
                var member = findEntity(level, id);
                var memberData = member == null ? null : data(member);
                if (memberData != null
                        && owner.getUUID().equals(memberData.getOwnerUuid())
                        && group.getId().equals(memberData.getSummonId())
                        && member.level() instanceof ServerLevel world) {
                    remove(world, member);
                }
            }
        }
    }

    public static void forget(ServerLevel level, Entity member) {
        var memberData = data(member);
        if (memberData == null) return;
        var owner = findEntity(level, memberData.getOwnerUuid());
        if (owner == null || !owner.hasData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA)) return;
        var data = owner.getData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA);
        var group = data.get(memberData.getSummonId());
        if (group == null || !group.getMembers().remove(member.getUUID())) return;
        if (group.getMembers().isEmpty()) {
            data.remove(group.getId());
            if (owner instanceof Player player) {
                var bell = group.getType().bell();
                player.getCooldowns().addCooldown(bell, bell.cooldown);
            }
        }
        owner.setData(PoptartCoreAttachments.PIXIE_SUMMONER_DATA, data);
    }

    public static void releaseCarriedItems(ServerLevel level, Entity member) {
        if (member instanceof Allay allay) {
            var carried = allay.getItemInHand(InteractionHand.MAIN_HAND);
            if (!carried.isEmpty()) {
                allay.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                var drop = new ItemEntity(level, allay.getX(), allay.getY() + .3, allay.getZ(), carried);
                drop.setDeltaMovement(0, .1, 0);
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
            }
        }
    }

    public static void remove(ServerLevel level, Entity member) {
        if (!isSummoned(member) || member.isRemoved()) return;
        releaseCarriedItems(level, member);
        forget(level, member);
        poof(level, member, member instanceof Allay ? PixieType.ALLAY : PixieType.VEX);
        member.discard();
    }

    public static void poof(ServerLevel level, Entity member, PixieType type) {
        PhasingMover.pixieBurst(level, member.getX(), member.getY() + .4, member.getZ(), 18, type);
        level.playSound(
                null,
                member.blockPosition(),
                PoptartCoreSounds.PIXIE_VANISH.get(),
                SoundSource.NEUTRAL,
                .6F,
                type == PixieType.VEX ? .7F : 1.4F);
    }

    public static boolean tickOwnerlessLifespan(
            ServerLevel level, LivingEntity member, PixieData data, PixieType type) {
        if (data.hasOwner() || member.tickCount < OWNERLESS_LIFESPAN) return false;
        remove(level, member);
        return true;
    }
}
