package dev.poptartking.poptartcore.depthmeter;

import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class DepthMeter {
    private DepthMeter() {}

    public static boolean carries(Player player, Item item) {
        if (player.getMainHandItem().is(item) || player.getOffhandItem().is(item)) return true;
        var accessories = AccessoryInventory.get(player);
        for (int i = 0; i < accessories.getSlots(); i++)
            if (accessories.getStackInSlot(i).is(item)) return true;
        return BackpackAttachments.has(
                player,
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(item)
                        .toString());
    }

    public static float compute(Level level, int y) {
        if (y < level.getMinBuildHeight()) {
            return 0.0F;
        } else if (level.dimension() == Level.NETHER) {
            return isErrorFlash(level) ? frame(20) : 0.0F;
        } else {
            int seaLevel = level.getSeaLevel();
            if (y == seaLevel) {
                return 0.0F;
            } else {
                return y < seaLevel
                        ? frame(0
                                + band((float) (seaLevel - y)
                                        / (float) Math.max(1, seaLevel - level.getMinBuildHeight())))
                        : frame(10
                                + band((float) (y - seaLevel)
                                        / (float) Math.max(1, level.getMaxBuildHeight() - seaLevel)));
            }
        }
    }

    public static String attachmentState(Level level, double y) {
        int blockY = Mth.floor(y);
        if (blockY < level.getMinBuildHeight()) {
            return "depth_meter";
        } else if (level.dimension() == Level.NETHER) {
            return isErrorFlash(level) ? "depth_meter_error" : "depth_meter";
        } else {
            int seaLevel = level.getSeaLevel();
            if (blockY > seaLevel) {
                return "depth_meter_high";
            } else {
                return blockY < seaLevel ? "depth_meter_low" : "depth_meter";
            }
        }
    }

    private static boolean isErrorFlash(Level level) {
        return level.getGameTime() / 10L % 2L == 0L;
    }

    private static int band(float ratio) {
        return Mth.clamp((int) (ratio * 10.0F), 0, 9);
    }

    private static float frame(int frame) {
        return (float) (frame + 1) * 0.04F;
    }
}
