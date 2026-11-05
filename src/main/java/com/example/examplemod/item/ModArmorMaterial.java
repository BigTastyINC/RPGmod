package com.example.examplemod.item;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public enum ModArmorMaterial implements IArmorMaterial {
    // Имя, Прочность, Защита(Ботинки,Штаны,Нагрудник,Шлем), Зачарование, Звук, Ремонт
    SILVER("silver", 15, new int[]{1, 3, 5, 2}, 18, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, () -> Ingredient.of(ModItems.SILVER_INGOT.get())),
    COBALT("cobalt", 25, new int[]{2, 5, 6, 2}, 20, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, () -> Ingredient.of(ModItems.COBALT_INGOT.get())),
    MITHRIL("mithril", 35, new int[]{2, 6, 7, 3}, 25, SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0F, () -> Ingredient.of(ModItems.MITHRIL_INGOT.get())),
    ADAMANTIUM("adamantium", 45, new int[]{3, 6, 8, 3}, 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0F, () -> Ingredient.of(ModItems.ADAMANTIUM_INGOT.get())),
    ORICHALCUM("orichalcum", 55, new int[]{4, 7, 9, 4}, 40, SoundEvents.ARMOR_EQUIP_GOLD, 3.0F, () -> Ingredient.of(ModItems.ORICHALCUM_INGOT.get()));

    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private final String name;
    private final int maxDamageFactor;
    private final int[] damageReductionAmountArray;
    private final int enchantability;
    private final SoundEvent soundEvent;
    private final float toughness;
    private final java.util.function.Supplier<Ingredient> repairMaterial;

    ModArmorMaterial(String name, int maxDamageFactor, int[] damageReductionAmountArray, int enchantability, SoundEvent soundEvent, float toughness, java.util.function.Supplier<Ingredient> repairMaterial) {
        this.name = name;
        this.maxDamageFactor = maxDamageFactor;
        this.damageReductionAmountArray = damageReductionAmountArray;
        this.enchantability = enchantability;
        this.soundEvent = soundEvent;
        this.toughness = toughness;
        this.repairMaterial = repairMaterial;
    }

    @Override public int getDurabilityForSlot(EquipmentSlotType slotIn) { return MAX_DAMAGE_ARRAY[slotIn.getIndex()] * this.maxDamageFactor; }
    @Override public int getDefenseForSlot(EquipmentSlotType slotIn) { return this.damageReductionAmountArray[slotIn.getIndex()]; }
    @Override public int getEnchantmentValue() { return this.enchantability; }
    @Override public SoundEvent getEquipSound() { return this.soundEvent; }
    @Override public Ingredient getRepairIngredient() { return this.repairMaterial.get(); }
    @Override public String getName() { return "examplemod:" + this.name; }
    @Override public float getToughness() { return this.toughness; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}