package dev.poptartking.poptartcore.backpack;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import dev.poptartking.poptartcore.timekeeping.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TimekeepingGameTests {
    private static ServerPlayer player(GameTestHelper h, List<Packet<?>> packets) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "clock-test"), false);
        var player = new ServerPlayer(
                h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        new ServerGamePacketListenerImpl(
                h.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {
                packets.add(packet);
            }

            @Override
            public void send(Packet<?> packet, net.minecraft.network.PacketSendListener listener) {
                packets.add(packet);
            }
        };
        return player;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void clockUsePersistsTimerAndReportsPreciseResult(GameTestHelper h) {
        var packets = new ArrayList<Packet<?>>();
        var player = player(h, packets);
        var clock = new ItemStack(Items.CLOCK);
        player.setItemInHand(InteractionHand.MAIN_HAND, clock);
        h.assertTrue(
                Items.CLOCK
                                .use(h.getLevel(), player, InteractionHand.MAIN_HAND)
                                .getResult()
                                .consumesAction()
                        && Stopwatch.isRunning(clock),
                "Vanilla clock use starts timer");
        clock.set(TimekeepingComponents.STOPWATCH.get(), h.getLevel().getGameTime() - 1234);
        h.assertTrue(Stopwatch.getElapsed(h.getLevel(), clock) == 1234, "Elapsed time measured in game ticks");
        var copy = ItemStack.parseOptional(h.getLevel().registryAccess(), (net.minecraft.nbt.CompoundTag)
                clock.save(h.getLevel().registryAccess()));
        h.assertTrue(Stopwatch.getElapsed(h.getLevel(), copy) == 1234, "Timer survives item serialization");
        Items.CLOCK.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(!Stopwatch.isRunning(clock), "Second use stops timer");
        var chat = packets.stream()
                .filter(p -> p instanceof ClientboundSystemChatPacket)
                .map(p -> ((ClientboundSystemChatPacket) p).content())
                .toList();
        h.assertTrue(
                chat.size() == 1 && chat.getFirst().getString().contains("1m 1s"), "Stop sends elapsed time in chat");
        var contents = (net.minecraft.network.chat.contents.TranslatableContents)
                chat.getFirst().getContents();
        var time = (net.minecraft.network.chat.Component) contents.getArgs()[0];
        h.assertTrue(
                time.getStyle()
                        .getHoverEvent()
                        .getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT)
                        .getString()
                        .equals("1:01:700"),
                "Chat result carries precise hover text");
        h.assertTrue(Stopwatch.formatPrecise(1234).equals("1:01:700"), "Precise result uses fifty-millisecond ticks");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void timeoutClampsAndStopsExactlyOnce(GameTestHelper h) {
        var packets = new ArrayList<Packet<?>>();
        var player = player(h, packets);
        var clock = new ItemStack(Items.CLOCK);
        clock.set(TimekeepingComponents.STOPWATCH.get(), h.getLevel().getGameTime() - Stopwatch.MAX_TICKS - 500);
        player.getInventory().setItem(9, clock);
        h.assertTrue(
                Stopwatch.getElapsed(h.getLevel(), clock) == 20000, "Timer clamps to sixteen minutes forty seconds");
        TimekeepingEvents.tick(player);
        TimekeepingEvents.tick(player);
        h.assertTrue(!Stopwatch.isRunning(clock), "Expired inventory clock cleared");
        h.assertTrue(
                packets.stream()
                                .filter(p -> p instanceof ClientboundSystemChatPacket)
                                .count()
                        == 1,
                "Timeout reports once");
        clock.set(TimekeepingComponents.STOPWATCH.get(), h.getLevel().getGameTime() + 50);
        h.assertTrue(
                Stopwatch.getElapsed(h.getLevel(), clock) == 0,
                "Future start timestamp cannot produce negative elapsed time");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void mountedAndUtilityTimersPreserveHandsAndStopAtLimit(GameTestHelper h) {
        var packets = new ArrayList<Packet<?>>();
        var player = player(h, packets);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 7));
        for (boolean ruck : new boolean[] {false, true}) {
            var bag = new ItemStack((ruck ? PoptartEquipmentBlocks.RUCKSACK : PoptartEquipmentBlocks.BACKPACK)
                    .get()
                    .asItem());
            BackpackAttachments.set(bag, 1, new ItemStack(Items.CLOCK));
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            h.assertTrue(UtilityWield.startAttachment(player), "Mounted clock enters working hand");
            Items.CLOCK.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            h.assertTrue(
                    Stopwatch.isRunning(BackpackAttachments.get(bag, 1)),
                    "G-equivalent use commits timer to mount immediately");
            UtilityWield.hand(player)
                    .set(TimekeepingComponents.STOPWATCH.get(), h.getLevel().getGameTime() - 20000);
            UtilityWield.commit(player);
            TimekeepingEvents.tick(player);
            h.assertTrue(
                    UtilityWield.isAttachmentWielding(player) && !Stopwatch.isRunning(BackpackAttachments.get(bag, 1)),
                    "Timeout commits without invalidating working hand");
            UtilityWield.stop(player);
            h.assertTrue(
                    player.getMainHandItem().is(Items.APPLE)
                            && player.getMainHandItem().getCount() == 7,
                    "Original hand preserved");
        }
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.UTILITY_SLOT, new ItemStack(Items.CLOCK));
        h.assertTrue(UtilityWield.start(player), "Utility clock enters working hand");
        Items.CLOCK.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        UtilityWield.stop(player);
        h.assertTrue(
                Stopwatch.isRunning(AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.UTILITY_SLOT)),
                "Utility slot stores start timestamp");
        h.succeed();
    }
}
