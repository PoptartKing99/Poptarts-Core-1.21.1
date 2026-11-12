package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.navigation.network.OpenLodestonePayload;
import dev.poptartking.poptartcore.navigation.network.UpdateLodestonePayload;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Lodestones {
    private static final double EDIT_DISTANCE_SQUARED = 64.0;
    private static int moving;

    private Lodestones() {}

    public static boolean is(BlockState state) {
        return state.is(Blocks.LODESTONE);
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        ServerLevel level = getLevel(player);
        LodestoneMark mark = LodestoneRegistry.get(level.getServer()).get(GlobalPos.of(level.dimension(), pos));
        PacketDistributor.sendToPlayer(player, new OpenLodestonePayload(pos, mark.name(), mark.icon()));
    }

    public static void bindCompass(ItemStack compass, ServerPlayer player, GlobalPos pos) {
        LodestoneMark mark = LodestoneRegistry.get(getLevel(player).getServer()).get(pos);
        if (!mark.isNamed()) {
            NavigationMessages.overlay(player, Component.translatable("poptartcore.lodestone.unnamed"));
        } else {
            Waypoints.refresh(compass, player);
            Waypoints.bindLodestone(compass, player, pos, mark);
        }
    }

    public static void onUpdate(UpdateLodestonePayload packet, ServerPlayer player) {
        ServerLevel level = getLevel(player);
        BlockPos pos = packet.pos();
        if (player.isSpectator() || !level.isInWorldBounds(pos)) return;
        // Never load a chunk for a client-supplied position; Sable also supports this lookup for loaded plots.
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null
                && is(chunk.getBlockState(pos))
                && player.distanceToSqr(WaypointAnchors.getWorldPosition(level, pos)) <= EDIT_DISTANCE_SQUARED
                && level.mayInteract(player, pos)) {
            LodestoneMark mark = new LodestoneMark(clean(packet.name()), WaypointIcons.sanitize(packet.icon()));
            GlobalPos key = GlobalPos.of(level.dimension(), pos);
            LodestoneRegistry registry = LodestoneRegistry.get(level.getServer());
            if (!mark.equals(registry.get(key))) {
                registry.set(key, mark);
                syncCompasses(level.getServer());
            }
            if (mark.isNamed()) {
                CompassAccess.mutateAll(player, compass -> Waypoints.rememberLodestone(compass, player, key, mark));
                player.inventoryMenu.broadcastChanges();
            }
        }
    }

    public static void onBlockChange(ServerLevel level, BlockPos pos, BlockState previous, BlockState next) {
        boolean removed = is(previous);
        if (moving <= 0 && removed != is(next)) {
            LodestoneRegistry registry = LodestoneRegistry.get(level.getServer());
            GlobalPos key = GlobalPos.of(level.dimension(), pos.immutable());
            if (!removed) {
                registry.claim(key);
            }

            if (registry.remove(key) && removed) {
                syncCompasses(level.getServer());
            }
        }
    }

    public static void move(ServerLevel from, ServerLevel to, Map<BlockPos, BlockPos> lodestones, Runnable action) {
        moving++;

        try {
            action.run();
        } finally {
            moving--;
        }

        LodestoneRegistry registry = LodestoneRegistry.get(from.getServer());
        boolean changed = false;

        for (Entry<BlockPos, BlockPos> lodestone : lodestones.entrySet()) {
            GlobalPos source = GlobalPos.of(from.dimension(), lodestone.getKey());
            if (is(to.getBlockState(lodestone.getValue()))) {
                GlobalPos target = GlobalPos.of(to.dimension(), lodestone.getValue());
                if (registry.relocate(source, target)) {
                    changed = true;
                } else {
                    registry.claim(target);
                }
            } else if (!is(from.getBlockState(lodestone.getKey())) && registry.remove(source)) {
                changed = true;
            }
        }

        if (changed) {
            syncCompasses(from.getServer());
        }
    }

    private static void syncCompasses(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inventory = player.getInventory();

            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.is(Items.COMPASS)) {
                    Waypoints.refresh(stack, player);
                }
            }

            Waypoints.refreshCarried(player);
            Waypoints.refreshAttached(player);
        }
    }

    private static String clean(String name) {
        StringBuilder cleaned = new StringBuilder();

        for (int index = 0; index < name.length() && cleaned.length() < 32; index++) {
            char character = name.charAt(index);
            if (character >= ' ' && character != 127 && character != 167) {
                cleaned.append(character);
            }
        }

        return cleaned.toString().trim();
    }

    private static ServerLevel getLevel(ServerPlayer player) {
        return player.serverLevel();
    }
}
