package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackAttachmentGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachmentReadsRemainIndependentAndKeepSparseSlotIndices(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (boolean rucksack : new boolean[] {false, true}) {
            var bag = bag(rucksack);
            int last = BackpackAttachments.slots(rucksack) - 1;
            var compass = new ItemStack(Items.COMPASS);
            compass.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Original compass"));
            BackpackAttachments.set(bag, last, compass);
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            var before = bag.copy();
            var contents = BackpackAttachments.contents(bag);
            helper.assertTrue(
                    BackpackAttachments.get(bag, 1).isEmpty()
                            && BackpackAttachments.find(bag, "minecraft:compass") == last
                            && BackpackAttachments.has(player, "minecraft:compass")
                            && !BackpackAttachments.has(player, "minecraft:clock"),
                    "Sparse attachment slots retain their indices and presence checks");
            var read = BackpackAttachments.get(bag, last);
            read.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Uncommitted edit"));
            read.shrink(1);
            var snapshotRead = BackpackAttachments.get(contents, last);
            snapshotRead.setCount(3);
            compass.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Changed input"));
            helper.assertTrue(
                    ItemStack.matches(bag, before)
                            && BackpackAttachments.contents(bag) == contents
                            && BackpackAttachments.get(bag, last).getCount() == 1
                            && BackpackAttachments.get(bag, last)
                                    .getHoverName()
                                    .getString()
                                    .equals("Original compass"),
                    "Reading or editing detached stacks cannot change stored attachments or the source bag");
            BackpackAttachments.set(bag, last, snapshotRead);
            snapshotRead.setCount(7);
            helper.assertTrue(
                    BackpackAttachments.get(bag, last).getCount() == 3
                            && BackpackAttachments.get(contents, last).getCount() == 1,
                    "Explicit writes commit independently and preserve earlier snapshots");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachmentQueriesKeepSlotLimitsAndHandleEmptyBags(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(
                BackpackAttachments.get(ItemStack.EMPTY, 4).isEmpty()
                        && BackpackAttachments.find(ItemStack.EMPTY, "minecraft:clock") == -1
                        && !BackpackAttachments.has(player, "minecraft:clock"),
                "Missing bags have no attachments and absent trailing slots read empty");
        for (boolean rucksack : new boolean[] {false, true}) {
            var bag = bag(rucksack);
            BackpackAttachments.set(bag, 4, new ItemStack(Items.CLOCK));
            BackpackAttachments.set(bag, 1, new ItemStack(Items.SPYGLASS));
            BackpackAttachments.set(bag, 2, new ItemStack(Items.SPYGLASS));
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
            helper.assertTrue(
                    BackpackAttachments.find(bag, "minecraft:spyglass") == 1
                            && BackpackAttachments.has(player, "minecraft:spyglass"),
                    "Duplicate attachments still select the first matching slot");
            helper.assertTrue(
                    BackpackAttachments.has(player, "minecraft:clock") == rucksack
                            && BackpackAttachments.find(bag, "minecraft:clock") == (rucksack ? 4 : -1),
                    "Unused lower mounts on backpacks cannot activate saved attachments");
            helper.assertTrue(
                    BackpackAttachments.find(bag, "minecraft:air") == 0
                            && BackpackAttachments.has(player, "minecraft:air"),
                    "Empty-slot queries retain their existing behavior");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void equalEntityIdsDoNotShareUseSessionsOrQuiverCaches(GameTestHelper helper) {
        var first = helper.makeMockPlayer(GameType.SURVIVAL);
        var second = helper.makeMockPlayer(GameType.SURVIVAL);
        second.setId(first.getId());
        helper.assertTrue(first.equals(second), "Client and integrated server player equality reproduced");
        var firstBag = bag(false);
        var secondBag = bag(false);
        BackpackAttachments.set(firstBag, 1, new ItemStack(Items.SPYGLASS));
        BackpackAttachments.set(secondBag, 1, new ItemStack(Items.SPYGLASS));
        AccessoryInventory.get(first).setStackInSlot(AccessoryInventory.BACK_SLOT, firstBag);
        AccessoryInventory.get(second).setStackInSlot(AccessoryInventory.BACK_SLOT, secondBag);
        try {
            helper.assertTrue(
                    UtilityWield.startAttachment(first) && UtilityWield.startAttachment(second),
                    "Both sides can establish use");
            helper.assertTrue(
                    UtilityWield.hand(first) != UtilityWield.hand(second), "Each side has its own working stack");
            Items.SPYGLASS.use(helper.getLevel(), first, InteractionHand.MAIN_HAND);
            Items.SPYGLASS.use(helper.getLevel(), second, InteractionHand.MAIN_HAND);
            for (int tick = 0; tick < 90; tick++) {
                if (tick % 10 == 0) {
                    UtilityWield.input(first, true, true);
                    UtilityWield.input(second, true, true);
                }
                UtilityWield.tick(first);
                UtilityWield.tick(second);
                helper.assertTrue(
                        first.isUsingItem() && second.isUsingItem(), "Both spyglass sessions remain active while held");
            }
            UtilityWield.stop(second);
            helper.assertTrue(
                    UtilityWield.isWielding(first) && first.isUsingItem(),
                    "Stopping server session does not cancel client session");
            UtilityWield.stop(first);
            var quiver = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("supplementaries:quiver")));
            BackpackAttachments.set(firstBag, 1, quiver);
            BackpackAttachments.set(secondBag, 1, quiver);
            var firstQuiver = BackpackAttachmentEffects.quiver(first);
            var secondQuiver = BackpackAttachmentEffects.quiver(second);
            helper.assertTrue(firstQuiver != secondQuiver, "Quiver caches are also isolated by player instance");
            firstQuiver.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("First side"));
            helper.assertTrue(
                    !BackpackAttachments.get(secondBag, 1).has(DataComponents.CUSTOM_NAME),
                    "Quiver edits cannot cross client/server bags");
            helper.assertTrue(
                    UtilityWield.sameAttachmentSource(firstBag, firstBag.copy(), true)
                            && !UtilityWield.sameAttachmentSource(firstBag, firstBag.copy(), false),
                    "Equivalent inventory sync copies are accepted only on client");
        } finally {
            UtilityWield.stop(first);
            UtilityWield.stop(second);
            BackpackAttachmentEffects.clear(first);
            BackpackAttachmentEffects.clear(second);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void heavyCoreAndWardingEffigyHaveTheirPassiveEffects(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = bag(false);
        BackpackAttachments.set(stack, 1, new ItemStack(Items.HEAVY_CORE));
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, stack);
        player.tickCount = 0;
        BackpackAttachmentEffects.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        helper.assertTrue(
                player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
                        && player.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
                                        .getAmplifier()
                                == 3,
                "Heavy core applies Slowness IV");
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        player.knockback(1, 1, 0);
        helper.assertTrue(player.getDeltaMovement().lengthSqr() == 0, "Heavy core prevents knockback");
        var effigy = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("nomansland:warding_effigy")));
        helper.assertTrue(!effigy.isEmpty(), "No Man's Land effigy exists");
        BackpackAttachments.set(stack, 1, effigy);
        var mob = new net.minecraft.world.entity.monster.Zombie(helper.getLevel());
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        mob.setPos(pos.getX(), pos.getY(), pos.getZ());
        double range = mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
        player.setPos(pos.getX() + range * .75, pos.getY(), pos.getZ());
        mob.setTarget(player);
        helper.assertTrue(mob.getTarget() == null, "Effigy rejects targets outside half normal follow range");
        player.setPos(pos.getX() + range * .25, pos.getY(), pos.getZ());
        mob.setTarget(player);
        helper.assertTrue(mob.getTarget() == player, "Nearby mobs can still target the player");
        helper.succeed();
    }

    private static ItemStack bag(boolean rucksack) {
        var bag =
                new ItemStack(rucksack ? PoptartEquipmentBlocks.RUCKSACK.get() : PoptartEquipmentBlocks.BACKPACK.get());
        BackpackMenu.contents(bag).setItem(rucksack ? 53 : 26, new ItemStack(Items.DIAMOND, 9));
        BackpackColors.set(bag, BackpackColors.Part.BODY, 0x123456);
        return bag;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachmentSlotsRestrictItemsAndShiftClickWithoutTouchingStorage(GameTestHelper helper) {
        for (boolean rucksack : new boolean[] {false, true}) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var stack = bag(rucksack);
            player.getInventory().setItem(0, stack);
            player.getInventory().setItem(1, new ItemStack(Items.COMPASS, 3));
            var pos = helper.absolutePos(new BlockPos(4, 2, 4));
            var state = (rucksack ? PoptartEquipmentBlocks.RUCKSACK.get() : PoptartEquipmentBlocks.BACKPACK.get())
                    .defaultBlockState();
            helper.getLevel().setBlockAndUpdate(pos, state);
            player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 1);
            var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
            entity.applyComponentsFromItemStack(stack);
            var menu = new BackpackMenu(1, player.getInventory(), entity, ItemStack.EMPTY);
            int size = rucksack ? 54 : 27;
            int first = size + 36;
            helper.assertTrue(
                    menu.slots.size() == first + BackpackAttachments.slots(rucksack),
                    "Correct number of attachment mounts");
            helper.assertTrue(
                    menu.getSlot(first).mayPlace(new ItemStack(Items.WHITE_BANNER)) == rucksack,
                    "Only rucksack accepts top banner");
            helper.assertTrue(
                    !menu.getSlot(first + 1).mayPlace(new ItemStack(Items.DIAMOND))
                            && menu.getSlot(first + 1).getMaxStackSize() == 1,
                    "Side mounts accept only attachment items, one each");
            menu.quickMoveStack(player, size + 28);
            helper.assertTrue(
                    BackpackAttachments.items(entity.attachments()).get(1).is(Items.COMPASS)
                            && BackpackAttachments.items(entity.attachments())
                                            .get(1)
                                            .getCount()
                                    == 1,
                    "Shift click installs a single primary attachment");
            helper.assertTrue(entity.getItem(size - 1).getCount() == 9, "Storage untouched");
            menu.quickMoveStack(player, first + 1);
            helper.assertTrue(
                    BackpackAttachments.items(entity.attachments()).get(1).isEmpty(), "Shift click removes attachment");
            helper.assertTrue(
                    menu.getSlot(first + 1).x == -27 && menu.getSlot(first + 1).y == (rucksack ? 36 : 18),
                    "Wayward left-strip slot position");
            var portable = new BackpackMenu(2, player.getInventory(), BackpackMenu.contents(stack), stack);
            helper.assertTrue(
                    !portable.canEditAttachments()
                            && !portable.getSlot(first + 1).isActive()
                            && !portable.getSlot(first + 1).mayPickup(player)
                            && portable.quickMoveStack(player, first + 1).isEmpty(),
                    "Attachments can only be edited while placed, like Wayward");
            helper.assertTrue(
                    player.getInventory().countItem(Items.COMPASS) >= 1, "Removed attachment returned to player");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachmentsPersistThroughSavePacketsDropsAndRenderSnapshots(GameTestHelper helper) {
        for (boolean rucksack : new boolean[] {false, true}) {
            var stack = bag(rucksack);
            BackpackAttachments.set(stack, 1, new ItemStack(Items.SPYGLASS));
            if (rucksack) BackpackAttachments.set(stack, 0, new ItemStack(Items.RED_BANNER));
            var pos = helper.absolutePos(new BlockPos(4, 2, 4));
            var state = (rucksack ? PoptartEquipmentBlocks.RUCKSACK.get() : PoptartEquipmentBlocks.BACKPACK.get())
                    .defaultBlockState();
            helper.getLevel().setBlockAndUpdate(pos, state);
            var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
            entity.applyComponentsFromItemStack(stack);
            var loaded = new BackpackBlockEntity(pos, state);
            loaded.loadWithComponents(
                    entity.saveWithFullMetadata(helper.getLevel().registryAccess()),
                    helper.getLevel().registryAccess());
            helper.assertTrue(
                    BackpackAttachments.items(loaded.attachments()).get(1).is(Items.SPYGLASS),
                    "Attachments survive world reload");
            var drop = Block.getDrops(state, helper.getLevel(), pos, entity).getFirst();
            helper.assertTrue(
                    BackpackAttachments.get(drop, 1).is(Items.SPYGLASS)
                            && BackpackMenu.contents(drop)
                                            .getItem(rucksack ? 53 : 26)
                                            .getCount()
                                    == 9,
                    "Block loot keeps attachments and full storage");
            var visual = BackpackVisual.renderStack(drop);
            helper.assertTrue(
                    BackpackAttachments.get(visual, 1).is(Items.SPYGLASS)
                            && !visual.has(DataComponents.CONTAINER)
                            && BackpackColors.color(visual, BackpackColors.Part.BODY) == 0x123456,
                    "Worn/corpse snapshot keeps attachments and dye without storage");
            var receiver = new BackpackBlockEntity(pos, state);
            receiver.setItem(0, new ItemStack(Items.EMERALD, 3));
            receiver.handleUpdateTag(
                    entity.getUpdateTag(helper.getLevel().registryAccess()),
                    helper.getLevel().registryAccess());
            helper.assertTrue(
                    BackpackAttachments.items(receiver.attachments()).get(1).is(Items.SPYGLASS)
                            && receiver.getItem(0).getCount() == 3,
                    "Client update synchronizes attachments without replacing inventory");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachmentVirtualHandUsesSpyglassAndCommitsOnlyToTheSameBag(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = bag(false);
        BackpackAttachments.set(stack, 1, new ItemStack(Items.SPYGLASS));
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, stack);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
        helper.assertTrue(UtilityWield.startAttachment(player), "Primary attachment enters virtual hand");
        Items.SPYGLASS.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.isUsingItem() && player.getUseItem().is(Items.SPYGLASS), "Vanilla spyglass use works");
        helper.assertTrue(player.getInventory().getItem(0).is(Items.DIAMOND_SWORD), "Real held item unchanged");
        player.getMainHandItem()
                .set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Attached spyglass"));
        UtilityWield.tick(player);
        UtilityWield.stop(player);
        helper.assertTrue(
                BackpackAttachments.get(stack, 1).getHoverName().getString().equals("Attached spyglass"),
                "Working edits committed to attachment");
        helper.assertTrue(UtilityWield.startAttachment(player), "Can use again");
        var replacement = bag(false);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, replacement);
        UtilityWield.tick(player);
        helper.assertTrue(
                !UtilityWield.isWielding(player)
                        && BackpackAttachments.get(replacement, 1).isEmpty(),
                "Changing bags stops use without stale writes");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachedTotemSavesPlayerAndIsConsumedOnce(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = bag(false);
        BackpackAttachments.set(stack, 1, new ItemStack(Items.TOTEM_OF_UNDYING));
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, stack);
        player.hurt(helper.getLevel().damageSources().generic(), 1000);
        helper.assertTrue(player.isAlive() && player.getHealth() > 0, "Mounted totem protects player");
        helper.assertTrue(BackpackAttachments.get(stack, 1).isEmpty(), "Totem consumed once");
        helper.assertTrue(
                BackpackMenu.contents(stack).getItem(26).getCount() == 9, "Totem does not consume stored items");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void attachedQuiverIsActiveAndAmmoEditsPersistImmediately(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = bag(false);
        var quiver = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("supplementaries:quiver")));
        helper.assertTrue(!quiver.isEmpty(), "Supplementaries quiver is installed");
        var type = net.mehvahdjukaar.supplementaries.reg.ModComponents.QUIVER_CONTENT.get();
        var content = quiver.get(type).toMutable();
        content.setStackInSlot(0, new ItemStack(Items.ARROW, 5));
        quiver.set(type, content.toImmutable());
        BackpackAttachments.set(stack, 1, quiver);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, stack);
        var active = net.mehvahdjukaar.supplementaries.common.items.QuiverItem.findActiveQuiver(player);
        helper.assertTrue(!active.isEmpty(), "Attached quiver participates in Supplementaries lookup");
        var edit = active.get(type).toMutable();
        edit.consumeSelected(1);
        active.set(type, edit.toImmutable());
        helper.assertTrue(
                BackpackAttachments.get(stack, 1).get(type).getSelected().getCount() == 4,
                "Arrow consumption saved immediately in bag");
        helper.assertTrue(
                net.mehvahdjukaar.supplementaries.common.items.QuiverItem.findActiveQuiverSlot(player) != null,
                "Quiver exposes an ammunition slot reference");
        helper.succeed();
    }
}
