package dev.poptartking.poptartcore.integration.newworld;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.system.RagdollBody;
import com.farcr.ragdoll.util.RagdollDataHolder;
import com.kryptography.newworld.common.blocks.TombstoneBlock;
import com.kryptography.newworld.init.NWBlocks;
import com.kryptography.newworld.init.data.tags.NWBlockTags;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Places a New World marker for a Ragdoll corpse without moving its inventory into the marker. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class TombstoneCorpse {
    private static final long NO_ANCHOR = Long.MIN_VALUE;
    private static final double LEASH = 5.0;

    private TombstoneCorpse() {}

    public static boolean canCarry(Player player, CorpseEntity corpse) {
        return !isAnchored(corpse) || player.getUUID().equals(corpse.getOwnerUUID());
    }

    private static boolean isAnchored(CorpseEntity corpse) {
        return corpse.getData(PoptartCoreAttachments.TOMBSTONE_ANCHOR) != NO_ANCHOR;
    }

    @SubscribeEvent
    public static void onCorpseSpawn(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof CorpseEntity corpse)
                || !corpse.holdsInventory()
                || corpse.getOwnerUUID() == null
                || isAnchored(corpse)) {
            return;
        }

        SimpleContainer items = corpse.getOrCreateContainer();
        int slot = -1;
        for (int i = 0; i < items.getContainerSize(); i++) {
            if (items.getItem(i).is(NWBlocks.TOMBSTONE.asItem())) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            return;
        }

        for (int offset = 0; offset < 6; offset++) {
            BlockPos pos = corpse.blockPosition().above(offset);
            BlockState existing = level.getBlockState(pos);
            if (!(existing.isAir() || existing.is(NWBlockTags.TOMBSTONE_REPLACEABLE))) {
                continue;
            }
            BlockState marker = NWBlocks.TOMBSTONE.get().defaultBlockState()
                    .setValue(BlockStateProperties.CRACKED, true);
            if (level.setBlockAndUpdate(pos, marker)) {
                items.removeItem(slot, 1);
                corpse.setData(PoptartCoreAttachments.TOMBSTONE_ANCHOR, pos.asLong());
                corpse.setSuspended(true);
            }
            return;
        }
    }

    @SubscribeEvent
    public static void onCorpseTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof CorpseEntity corpse)
                || !(corpse.level() instanceof ServerLevel level)) {
            return;
        }
        long packed = corpse.getData(PoptartCoreAttachments.TOMBSTONE_ANCHOR);
        if (packed == NO_ANCHOR) {
            return;
        }

        BlockPos pos = BlockPos.of(packed);
        Vec3 target = new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        if (!(level.getBlockState(pos).getBlock() instanceof TombstoneBlock)
                || corpse.position().distanceTo(target) > LEASH) {
            release(corpse);
            return;
        }
        RagdollBody body = ((RagdollDataHolder) corpse).getRagdoll();
        if (body == null) {
            return;
        }
        if (body.isHeldByEntity()) {
            release(corpse);
            return;
        }
        corpse.setNoGravity(true);
        body.suspendAt(target);
    }

    @SubscribeEvent
    public static void onCorpseDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof CorpseEntity corpse && isAnchored(corpse)) {
            event.setCanceled(true);
        }
    }

    private static void release(CorpseEntity corpse) {
        corpse.setData(PoptartCoreAttachments.TOMBSTONE_ANCHOR, NO_ANCHOR);
        corpse.setNoGravity(false);
        RagdollBody body = ((RagdollDataHolder) corpse).getRagdoll();
        if (body != null) {
            body.releaseSuspension();
        }
        corpse.setSuspended(false);
    }
}
