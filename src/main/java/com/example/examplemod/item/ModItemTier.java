package com.example.examplemod.item;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

public enum ModItemTier implements IItemTier {
    // (Уровень добычи, Прочность, Скорость, Урон, Зачарование)
    SILVER(2, 750, 7.0F, 3.0F, 18, () -> Ingredient.of(ModItems.SILVER_INGOT.get())),
    COBALT(3, 1200, 8.5F, 4.0F, 20, () -> Ingredient.of(ModItems.COBALT_INGOT.get())),
    MITHRIL(3, 1800, 9.5F, 5.0F, 25, () -> Ingredient.of(ModItems.MITHRIL_INGOT.get())),
    ADAMANTIUM(4, 2500, 11.0F, 6.0F, 30, () -> Ingredient.of(ModItems.ADAMANTIUM_INGOT.get())),
    ORICHALCUM(4, 3500, 13.0F, 8.0F, 40, () -> Ingredient.of(ModItems.ORICHALCUM_INGOT.get()));

    private final int harvestLevel;
    private final int maxUses;
    private final float efficiency;
    private final float attackDamage;
    private final int enchantability;
    private final java.util.function.Supplier<Ingredient> repairMaterial;

    ModItemTier(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability, java.util.function.Supplier<Ingredient> repairMaterial) {
        this.harvestLevel = harvestLevel;
        this.maxUses = maxUses;
        this.efficiency = efficiency;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairMaterial = repairMaterial;
    }

    @Override public int getUses() { return maxUses; }
    @Override public float getSpeed() { return efficiency; }
    @Override public float getAttackDamageBonus() { return attackDamage; }
    @Override public int getLevel() { return harvestLevel; }
    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public Ingredient getRepairIngredient() { return repairMaterial.get(); }
}