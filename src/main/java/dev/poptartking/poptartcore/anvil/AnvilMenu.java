package dev.poptartking.poptartcore.anvil;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.AnvilComponents;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import java.util.ArrayList;
import java.util.List;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags.Items;
import org.jetbrains.annotations.Nullable;

public class AnvilMenu extends ItemCombinerMenu {
    private static final int MAX_UPGRADES = 3;
    private static final String LEGACY_UPGRADE_KEY = "poptartcore:upgrade_count";
    private static final String SEPARATE_BONUSES_KEY = "poptartcore:separate_upgrade_bonuses";
    private int cost;

    public AnvilMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public AnvilMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(PoptartCoreMenus.PRIMITIVE_ANVIL_MENU.get(), containerId, playerInventory, access);
        access.execute((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof AnvilBlockEntity anvil) {
                var slot = new net.minecraft.world.inventory.Slot(new AnvilHammerContainer(level, pos), 0, 27, 20) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.is(PoptartCoreTags.HAMMERS);
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                };
                slot.index = 2;
                this.slots.set(2, slot);
            }
        });
    }

    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 27, 47, ItemStack::isDamageableItem)
                .withSlot(1, 76, 47, stack -> true)
                .withSlot(2, 27, 20, stack -> stack.is(PoptartCoreTags.HAMMERS))
                .withResultSlot(3, 134, 47)
                .build();
    }

    private ItemStack hammer() {
        return this.slots.get(2).getItem();
    }

    public boolean hasWorkingHammer() {
        ItemStack hammer = this.hammer();
        return !hammer.isEmpty()
                && hammer.is(PoptartCoreTags.HAMMERS)
                && hammer.getDamageValue() < hammer.getMaxDamage()
                && !isWornOut(hammer);
    }

    protected boolean mayPickup(Player player, boolean b) {
        return this.hasWorkingHammer();
    }

    protected void onTake(Player player, ItemStack stack) {
        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.cost > 0) {
            ItemStack repairItem = this.inputSlots.getItem(1);
            if (!repairItem.isEmpty() && repairItem.getCount() > this.cost) {
                repairItem.shrink(this.cost);
                this.inputSlots.setItem(1, repairItem);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        }

        this.access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                this.hammer()
                        .hurtAndBreak(
                                1,
                                serverLevel,
                                player,
                                i -> serverLevel.playSound(
                                        null,
                                        pos,
                                        stack.getBreakingSound(),
                                        SoundSource.PLAYERS,
                                        0.8F,
                                        0.8F + player.getRandom().nextFloat() * 0.4F));
            }

            if (level.getBlockEntity(pos) instanceof AnvilBlockEntity blockEntity) blockEntity.setHammer(this.hammer());
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AnvilBlock anvil) {
                anvil.maybeDamage(level, pos, state, player.getRandom());
                level.levelEvent(1030, pos, 0);
            }
        });
    }

    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof AnvilBlock;
    }

    public void createResult() {
        ItemStack input = this.inputSlots.getItem(0);
        ItemStack modifier = this.inputSlots.getItem(1);
        if (!input.isEmpty() && !modifier.isEmpty()) {
            ItemStack result = input.copy();
            this.cost = 1;
            if (input.isDamageableItem() && isRepairMaterial(input, modifier)) {
                int upgradeCost = getUpgradeCount(input) + 4;
                boolean canUpgrade = result.is(PoptartCoreTags.UPGRADEABLE)
                        && getUpgradeCount(input) < 3
                        && !modifier.is(PoptartCoreTags.CANNOT_UPGRADE_WITH);
                if (canUpgrade && modifier.getCount() >= upgradeCost) {
                    this.cost = upgradeCost;
                    this.upgradeItem(result);
                } else if (input.isDamaged()) {
                    int maxDmg = input.getMaxDamage();
                    int currDmg = input.getDamageValue();
                    int dmgToRepair = (int) ((double) maxDmg * 0.35 * (double) modifier.getCount());
                    result.setDamageValue(currDmg - dmgToRepair);
                    this.cost = Math.min(modifier.getCount(), this.calculateMaxRepairItemCost(maxDmg, currDmg));
                } else {
                    result = ItemStack.EMPTY;
                }
            } else if (modifier.is((Item) ModRegistry.SOAP.get()) && hasAnyModifications(input)) {
                if (hasModification(input, "fire_resistant")) {
                    if (!result.getItem().components().has(DataComponents.FIRE_RESISTANT))
                        result.remove(DataComponents.FIRE_RESISTANT);
                }

                removeAllModifications(result);
            } else if (input.is(Items.ARMORS)
                    && modifier.is(net.minecraft.world.item.Items.PHANTOM_MEMBRANE)
                    && !hasModification(input, "invisible")) {
                addModification(result, "invisible");
            } else if (modifier.is(net.minecraft.world.item.Items.SLIME_BALL) && !hasModification(input, "sticky")) {
                addModification(result, "sticky");
            } else if (modifier.is(net.minecraft.world.item.Items.MAGMA_CREAM)
                    && !hasModification(input, "fire_resistant")) {
                addModification(result, "fire_resistant");
                result.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
            } else {
                result = ItemStack.EMPTY;
            }

            this.resultSlots.setItem(0, result);
            this.broadcastChanges();
        } else {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }

    private int calculateMaxRepairItemCost(int maxDmg, int currDmg) {
        for (int i = 1; i < 3; i++) {
            int dmgToRepair = (int) ((double) maxDmg * 0.35 * (double) i);
            int newDmg = currDmg - dmgToRepair;
            if (newDmg <= 0) {
                return i;
            }
        }

        return 3;
    }

    private static boolean isRepairMaterial(ItemStack input, ItemStack material) {
        return input.is(net.minecraft.world.item.Items.TRIDENT)
                ? material.is(net.minecraft.world.item.Items.PRISMARINE_SHARD)
                : input.getItem().isValidRepairItem(input, material);
    }

    private void upgradeItem(ItemStack stack) {
        applyUpgrades(stack, Math.min(getUpgradeCount(stack) + 1, 3));
    }

    public static void applyUpgrades(ItemStack stack, int count) {
        count = Mth.clamp(count, 0, 3);
        boolean armor = isArmor(stack);
        UpgradeDurability durability = durabilityReference(stack, armor);
        int oldMax = stack.getMaxDamage();
        int oldDamage = stack.getDamageValue();
        ItemAttributeModifiers built = buildUpgradedModifiers(stack, count);
        CompoundTag data = getCustomData(stack);
        data.remove(LEGACY_UPGRADE_KEY);
        data.putBoolean(SEPARATE_BONUSES_KEY, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, built);

        if (count <= 0) {
            stack.remove(AnvilComponents.UPGRADE_COUNT.get());
            stack.remove(AnvilComponents.UPGRADE_DURABILITY.get());
        } else {
            stack.set(AnvilComponents.UPGRADE_COUNT.get(), count);
            if (durability != null) stack.set(AnvilComponents.UPGRADE_DURABILITY.get(), durability);
        }
        if (durability != null) {
            int newMax = durability.atLevel(count, armor);
            stack.set(DataComponents.MAX_DAMAGE, newMax);
            if (stack.isDamaged() && oldMax > 0) {
                double remaining = (double) (oldMax - oldDamage) / oldMax;
                stack.setDamageValue(Mth.clamp(newMax - (int) Math.round(remaining * newMax), 0, newMax - 1));
            }
        }
    }

    public static boolean isUpgraded(ItemStack stack) {
        return stack.has(AnvilComponents.UPGRADE_COUNT.get());
    }

    public static boolean isWornOut(ItemStack stack) {
        int max = stack.getMaxDamage();
        return max > 1 && isUpgraded(stack) && stack.getDamageValue() >= max - 1;
    }

    @Nullable
    private static UpgradeDurability durabilityReference(ItemStack stack, boolean armor) {
        int current = stack.getMaxDamage();
        if (current <= 0) return null;
        int count = getUpgradeCount(stack);
        UpgradeDurability saved = stack.get(AnvilComponents.UPGRADE_DURABILITY.get());
        if (saved != null) {
            return saved.atLevel(count, armor) == current ? saved : new UpgradeDurability(current, count);
        }
        if (count == 0) return new UpgradeDurability(current, 0);

        // Recognize old normal-item upgrades exactly, preserving their original rounding.
        int prototype = stack.getItem().components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
        if (prototype > 0) {
            var original = new UpgradeDurability(prototype, 0);
            if (current == prototype || original.atLevel(count, armor) == current) return original;
        }
        // Older custom items and later external edits retain their current maximum at the current level.
        return new UpgradeDurability(current, count);
    }

    public static boolean isArmor(ItemStack stack) {
        for (Entry entry : defaultModifiers(stack).modifiers()) {
            if (entry.attribute().equals(Attributes.ARMOR)) {
                return true;
            }
        }

        return false;
    }

    public static ItemAttributeModifiers buildUpgradedModifiers(ItemStack stack, int count) {
        count = Mth.clamp(count, 0, MAX_UPGRADES);
        ItemAttributeModifiers defaults = defaultModifiers(stack);
        ItemAttributeModifiers current = stack.getComponentsPatch().get(DataComponents.ATTRIBUTE_MODIFIERS) == null
                ? defaults
                : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        boolean armor = isArmor(stack);
        boolean mining = !armor && stack.is(PoptartCoreTags.UPGRADE_MINING);
        boolean attack = !armor && stack.is(PoptartCoreTags.UPGRADE_ATTACK);
        int legacyCount = getCustomData(stack).getBoolean(SEPARATE_BONUSES_KEY) ? 0 : getUpgradeCount(stack);
        List<Entry> entries = new ArrayList<>();
        for (Entry entry : current.modifiers()) {
            AttributeModifier modifier = entry.modifier();
            if (modifier.id()
                    .equals(upgradeModifier(entry.attribute(), entry.slot(), 0).id())) continue;
            // The previous format folded bonuses into the default modifier amounts.
            if (legacyCount > 0
                    && modifier.operation() == Operation.ADD_VALUE
                    && defaults.modifiers().stream()
                            .anyMatch(original -> original.attribute().equals(entry.attribute())
                                    && original.slot().equals(entry.slot())
                                    && original.modifier().id().equals(modifier.id()))) {
                double bonus = upgradeBonus(entry.attribute(), legacyCount, armor, mining, attack);
                entries.add(new Entry(
                        entry.attribute(),
                        new AttributeModifier(modifier.id(), modifier.amount() - bonus, modifier.operation()),
                        entry.slot()));
            } else {
                entries.add(entry);
            }
        }
        if (count > 0) {
            if (armor) {
                for (EquipmentSlotGroup slot : defaults.modifiers().stream()
                        .filter(entry -> entry.attribute().equals(Attributes.ARMOR))
                        .map(Entry::slot)
                        .distinct()
                        .toList()) {
                    entries.add(new Entry(Attributes.ARMOR, upgradeModifier(Attributes.ARMOR, slot, count), slot));
                    if (count == MAX_UPGRADES)
                        entries.add(new Entry(
                                Attributes.ARMOR_TOUGHNESS,
                                upgradeModifier(Attributes.ARMOR_TOUGHNESS, slot, 1),
                                slot));
                }
            } else {
                if (mining)
                    entries.add(new Entry(
                            Attributes.MINING_EFFICIENCY,
                            upgradeModifier(Attributes.MINING_EFFICIENCY, EquipmentSlotGroup.MAINHAND, count),
                            EquipmentSlotGroup.MAINHAND));
                if (attack)
                    entries.add(new Entry(
                            Attributes.ATTACK_DAMAGE,
                            upgradeModifier(Attributes.ATTACK_DAMAGE, EquipmentSlotGroup.MAINHAND, count),
                            EquipmentSlotGroup.MAINHAND));
            }
        }
        return new ItemAttributeModifiers(List.copyOf(entries), current.showInTooltip());
    }

    public static ItemAttributeModifiers defaultModifiers(ItemStack stack) {
        ItemAttributeModifiers prototype = (ItemAttributeModifiers) stack.getItem()
                .components()
                .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return prototype.modifiers().isEmpty() ? stack.getItem().getDefaultAttributeModifiers(stack) : prototype;
    }

    public static void healUpgradedModifiers(ItemStack stack) {
        Integer count = (Integer) stack.get(AnvilComponents.UPGRADE_COUNT.get());
        if (count != null && count > 0) {
            ItemAttributeModifiers built = buildUpgradedModifiers(stack, count);
            if (!built.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) {
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, built);
            }
            CompoundTag data = getCustomData(stack);
            if (!data.getBoolean(SEPARATE_BONUSES_KEY)) {
                data.putBoolean(SEPARATE_BONUSES_KEY, true);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            }
        }
    }

    private static double upgradeBonus(
            Holder<Attribute> attribute, int count, boolean isArmor, boolean mining, boolean attack) {
        if (isArmor) {
            if (attribute.equals(Attributes.ARMOR)) {
                return (double) count;
            } else if (attribute.equals(Attributes.ARMOR_TOUGHNESS)) {
                return count >= 3 ? 1.0 : 0.0;
            } else {
                return 0.0;
            }
        } else if (attack && attribute.equals(Attributes.ATTACK_DAMAGE)) {
            return (double) count;
        } else {
            return mining && attribute.equals(Attributes.MINING_EFFICIENCY) ? (double) count : 0.0;
        }
    }

    private static AttributeModifier upgradeModifier(
            Holder<Attribute> attribute, EquipmentSlotGroup slot, double amount) {
        ResourceLocation id = PoptartCore.location(
                "upgrade." + attribute.getKey().location().getPath() + "." + slot.getSerializedName());
        return new AttributeModifier(id, amount, Operation.ADD_VALUE);
    }

    public static CompoundTag getCustomData(ItemStack stack) {
        return ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
    }

    public static void removeAllModifications(ItemStack stack) {
        List<String> modifications = List.of("invisible", "sticky", "fire_resistant");
        CompoundTag data = getCustomData(stack);

        for (String mod : modifications) {
            data.remove("poptartcore:has_" + mod + "_modification");
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static boolean hasAnyModifications(ItemStack stack) {
        List<String> modifications = List.of("invisible", "sticky", "fire_resistant");
        CompoundTag data = getCustomData(stack);

        for (String mod : modifications) {
            if (data.getBoolean("poptartcore:has_" + mod + "_modification")) {
                return true;
            }
        }

        return false;
    }

    public static List<String> getModifications(ItemStack stack) {
        List<String> modifications = List.of("invisible", "sticky", "fire_resistant");
        List<String> modificationsPresent = new ArrayList<>();
        CompoundTag data = getCustomData(stack);

        for (String mod : modifications) {
            if (data.getBoolean("poptartcore:has_" + mod + "_modification")) {
                modificationsPresent.add(mod);
            }
        }

        return modificationsPresent;
    }

    public static boolean hasModification(ItemStack stack, String mod) {
        return getCustomData(stack).getBoolean("poptartcore:has_" + mod + "_modification");
    }

    public static int getUpgradeCount(ItemStack stack) {
        Integer count = (Integer) stack.get(AnvilComponents.UPGRADE_COUNT.get());
        return count != null ? Mth.clamp(count, 0, 3) : getLegacyUpgradeCount(stack);
    }

    public static int getLegacyUpgradeCount(ItemStack stack) {
        CustomData data = (CustomData) stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains("poptartcore:upgrade_count")
                ? Mth.clamp(data.copyTag().getInt("poptartcore:upgrade_count"), 0, 3)
                : 0;
    }

    public static void addModification(ItemStack stack, String mod) {
        String key = "poptartcore:has_" + mod + "_modification";
        CompoundTag data = getCustomData(stack);
        data.putBoolean(key, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
