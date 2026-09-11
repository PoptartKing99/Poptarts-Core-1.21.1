package dev.poptartking.poptartcore.millstone;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;

public class MillstoneItemClient {
    private static final int COLOR = -41620;

    public static void showBounds(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        AABB bounds = new AABB(pos).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 1.0, 0.0);
        Outliner.getInstance().showAABB(Pair.of("millstone", pos), bounds).colored(-41620);
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.poptartcore.millstone_space")
                            .withColor(16735596),
                    true);
        }
    }
}
