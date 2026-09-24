package dev.poptartking.poptartcore.lostheart;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.system.RagdollBody;
import com.farcr.ragdoll.util.RagdollDataHolder;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Vector3dc;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public class LostHeartEntity extends Entity {
    public static final double HOVER_HEIGHT = 0.9;
    private static final EntityDataAccessor<Integer> CRACKS =
            SynchedEntityData.defineId(LostHeartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORPSE_ID =
            SynchedEntityData.defineId(LostHeartEntity.class, EntityDataSerializers.INT);
    private static final Map<UUID, Claim> PENDING = new HashMap<>();
    private static final List<Queued> QUEUE = new ArrayList<>();
    private UUID owner;
    private UUID corpse;
    private int token;
    private int ticksSinceHit;
    private int missingTicks;

    public LostHeartEntity(EntityType<? extends LostHeartEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public int cracks() {
        return entityData.get(CRACKS);
    }

    public int corpseId() {
        return entityData.get(CORPSE_ID);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CRACKS, 0);
        builder.define(CORPSE_ID, -1);
    }

    public static Vec3 headOf(Entity body) {
        if (body instanceof RagdollDataHolder holder) {
            RagdollBody ragdoll = holder.getRagdoll();
            if (ragdoll != null) {
                RagdollBody.Limb head = ragdoll.isLimbDetached("head")
                        ? ragdoll.getLimbs().get("body") : ragdoll.getHead();
                if (head != null) {
                    Vector3dc point = head.getPose().position();
                    return new Vec3(point.x(), point.y(), point.z());
                }
            }
        }
        return body.position().add(0, body.getBbHeight() * 0.5, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            followCorpse();
            return;
        }
        if (owner == null || LostHeartTokens.of(level().getServer()).tokenOf(owner) != token) {
            discard();
            return;
        }
        if (corpse != null && level() instanceof ServerLevel server) {
            Entity body = server.getEntity(corpse);
            boolean gone = body == null || body.isRemoved();
            int id = gone ? -1 : body.getId();
            if (corpseId() != id) entityData.set(CORPSE_ID, id);
            if (gone) {
                if (++missingTicks > 60) {
                    discard();
                    return;
                }
            } else {
                missingTicks = 0;
            }
        }
        followCorpse();
        if (++ticksSinceHit > 60 && cracks() > 0) {
            entityData.set(CRACKS, cracks() - 1);
            ticksSinceHit = 0;
        }
    }

    private void followCorpse() {
        if (corpseId() == -1) return;
        Entity body = level().getEntity(corpseId());
        if (body != null && !body.isRemoved()) {
            Vec3 head = headOf(body);
            setPos(head.x, head.y + HOVER_HEIGHT - getBbHeight() * 0.5, head.z);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) punch(player);
        return false;
    }

    private void punch(Player player) {
        if (!(level() instanceof ServerLevel server) || isRemoved() || !player.getUUID().equals(owner)) return;
        if (Hearts.get(player) >= Hearts.MAX) return;
        ticksSinceHit = 0;
        int next = cracks() + 1;
        if (next >= 4) {
            shatter(server, player);
        } else {
            entityData.set(CRACKS, next);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS, 0.35F, 1.5F + 0.15F * next);
            embers(server, player, next, 6);
        }
    }

    private void embers(ServerLevel server, Player player, int stage, int count) {
        server.sendParticles(new LostHeartEmberOptions(player.getId(), stage),
                getX(), getY(), getZ(), count, 0.18, 0.18, 0.18, 0.035);
    }

    private void shatter(ServerLevel server, Player player) {
        Hearts.set(player, Hearts.get(player) + 1);
        player.heal(2.0F);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.7F, 0.75F);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.PLAYERS, 0.6F, 1.2F);
        embers(server, player, 4, 26);
        discard();
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        corpse = tag.hasUUID("corpse") ? tag.getUUID("corpse") : null;
        token = tag.getInt("token");
        entityData.set(CRACKS, tag.getInt("cracks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (owner != null) tag.putUUID("owner", owner);
        if (corpse != null) tag.putUUID("corpse", corpse);
        tag.putInt("token", token);
        tag.putInt("cracks", cracks());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Hearts.get(player) > Hearts.MIN) {
            int token = LostHeartTokens.of(player.server).bump(player.getUUID());
            PENDING.put(player.getUUID(), new Claim(token, player.level().getGameTime() + 40));
            // Previously spawned hearts become invalid even if their chunk is unloaded.
        }
    }

    @SubscribeEvent
    public static void onCorpseSpawned(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof CorpseEntity body)) return;
        UUID owner = body.getOwnerUUID();
        if (owner == null) return;
        Claim claim = PENDING.remove(owner);
        if (claim != null && level.getGameTime() <= claim.deadline()) {
            QUEUE.add(new Queued(level, body, owner, claim.token()));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        PENDING.entrySet().removeIf(entry -> level.getGameTime() > entry.getValue().deadline() + 40);
        QUEUE.removeIf(queued -> {
            if (queued.level() != level) return false;
            CorpseEntity body = queued.body();
            if (body.isRemoved()) return true;
            if (!body.isAddedToLevel()) return false;
            LostHeartEntity heart = new LostHeartEntity(PoptartCoreEntities.LOST_HEART.get(), level);
            heart.owner = queued.owner();
            heart.corpse = body.getUUID();
            heart.token = queued.token();
            heart.entityData.set(CORPSE_ID, body.getId());
            Vec3 head = headOf(body);
            heart.setPos(head.x, head.y + HOVER_HEIGHT - heart.getBbHeight() * 0.5, head.z);
            level.addFreshEntity(heart);
            return true;
        });
    }

    @SubscribeEvent
    public static void onCorpseAttacked(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof CorpseEntity body)) return;
        Player player = event.getEntity();
        if (!player.getUUID().equals(body.getOwnerUUID())) return;
        for (LostHeartEntity heart : body.level().getEntitiesOfClass(LostHeartEntity.class,
                body.getBoundingBox().inflate(4), candidate -> body.level().isClientSide
                        ? candidate.corpseId() == body.getId()
                        : body.getUUID().equals(candidate.corpse))) {
            event.setCanceled(true);
            break;
        }
    }

    @SubscribeEvent
    public static void onCorpseKilled(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof CorpseEntity body)
                || !(event.getSource().getEntity() instanceof Player player)
                || !(body.level() instanceof ServerLevel level)
                || !player.getUUID().equals(body.getOwnerUUID())) return;
        for (LostHeartEntity heart : level.getEntitiesOfClass(LostHeartEntity.class,
                body.getBoundingBox().inflate(4), candidate -> body.getUUID().equals(candidate.corpse))) {
            if (Hearts.get(player) < Hearts.MAX) heart.shatter(level, player);
            break;
        }
    }

    private record Claim(int token, long deadline) {}
    private record Queued(ServerLevel level, CorpseEntity body, UUID owner, int token) {}
}
