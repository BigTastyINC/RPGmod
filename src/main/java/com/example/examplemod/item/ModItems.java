package com.example.examplemod.item;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "examplemod");

    // --- КНИГИ ---
    public static final RegistryObject<Item> STATS_BOOK = ITEMS.register("stats_book",
            () -> new StatsBookItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE)));

    public static final RegistryObject<Item> SPELL_TOME = ITEMS.register("spell_tome",
            () -> new SpellTomeItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE)));

    // --- МАГИЧЕСКИЕ КРИСТАЛЛЫ ---
    public static final RegistryObject<Item> SMALL_MAGIC_CRYSTAL = ITEMS.register("small_magic_crystal",
            () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MEDIUM_MAGIC_CRYSTAL = ITEMS.register("medium_magic_crystal",
            () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> LARGE_MAGIC_CRYSTAL = ITEMS.register("large_magic_crystal",
            () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- СЛИТКИ ---
    public static final RegistryObject<Item> SILVER_INGOT = ITEMS.register("silver_ingot", () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> COBALT_INGOT = ITEMS.register("cobalt_ingot", () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MITHRIL_INGOT = ITEMS.register("mithril_ingot", () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ADAMANTIUM_INGOT = ITEMS.register("adamantium_ingot", () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ORICHALCUM_INGOT = ITEMS.register("orichalcum_ingot", () -> new Item(new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- МЕЧИ ---
    public static final RegistryObject<Item> SILVER_SWORD = ITEMS.register("silver_sword", () -> new SwordItem(ModItemTier.SILVER, 3, -2.4F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> COBALT_SWORD = ITEMS.register("cobalt_sword", () -> new SwordItem(ModItemTier.COBALT, 3, -2.4F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MITHRIL_SWORD = ITEMS.register("mithril_sword", () -> new SwordItem(ModItemTier.MITHRIL, 3, -2.4F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ADAMANTIUM_SWORD = ITEMS.register("adamantium_sword", () -> new SwordItem(ModItemTier.ADAMANTIUM, 3, -2.4F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ORICHALCUM_SWORD = ITEMS.register("orichalcum_sword", () -> new SwordItem(ModItemTier.ORICHALCUM, 3, -2.4F, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- ТОПОРЫ ---
    public static final RegistryObject<Item> SILVER_AXE = ITEMS.register("silver_axe", () -> new AxeItem(ModItemTier.SILVER, 6.0F, -3.0F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> COBALT_AXE = ITEMS.register("cobalt_axe", () -> new AxeItem(ModItemTier.COBALT, 6.0F, -3.0F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MITHRIL_AXE = ITEMS.register("mithril_axe", () -> new AxeItem(ModItemTier.MITHRIL, 6.0F, -3.0F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ADAMANTIUM_AXE = ITEMS.register("adamantium_axe", () -> new AxeItem(ModItemTier.ADAMANTIUM, 6.0F, -3.0F, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ORICHALCUM_AXE = ITEMS.register("orichalcum_axe", () -> new AxeItem(ModItemTier.ORICHALCUM, 6.0F, -3.0F, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- БРОНЯ: СЕРЕБРО ---
    public static final RegistryObject<Item> SILVER_HELMET = ITEMS.register("silver_helmet", () -> new ModArmorItem(ModArmorMaterial.SILVER, EquipmentSlotType.HEAD, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> SILVER_CHESTPLATE = ITEMS.register("silver_chestplate", () -> new ModArmorItem(ModArmorMaterial.SILVER, EquipmentSlotType.CHEST, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> SILVER_LEGGINGS = ITEMS.register("silver_leggings", () -> new ModArmorItem(ModArmorMaterial.SILVER, EquipmentSlotType.LEGS, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> SILVER_BOOTS = ITEMS.register("silver_boots", () -> new ModArmorItem(ModArmorMaterial.SILVER, EquipmentSlotType.FEET, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- БРОНЯ: КОБАЛЬТ ---
    public static final RegistryObject<Item> COBALT_HELMET = ITEMS.register("cobalt_helmet", () -> new ModArmorItem(ModArmorMaterial.COBALT, EquipmentSlotType.HEAD, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> COBALT_CHESTPLATE = ITEMS.register("cobalt_chestplate", () -> new ModArmorItem(ModArmorMaterial.COBALT, EquipmentSlotType.CHEST, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> COBALT_LEGGINGS = ITEMS.register("cobalt_leggings", () -> new ModArmorItem(ModArmorMaterial.COBALT, EquipmentSlotType.LEGS, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> COBALT_BOOTS = ITEMS.register("cobalt_boots", () -> new ModArmorItem(ModArmorMaterial.COBALT, EquipmentSlotType.FEET, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- БРОНЯ: МИФРИЛ ---
    public static final RegistryObject<Item> MITHRIL_HELMET = ITEMS.register("mithril_helmet", () -> new ModArmorItem(ModArmorMaterial.MITHRIL, EquipmentSlotType.HEAD, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MITHRIL_CHESTPLATE = ITEMS.register("mithril_chestplate", () -> new ModArmorItem(ModArmorMaterial.MITHRIL, EquipmentSlotType.CHEST, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MITHRIL_LEGGINGS = ITEMS.register("mithril_leggings", () -> new ModArmorItem(ModArmorMaterial.MITHRIL, EquipmentSlotType.LEGS, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> MITHRIL_BOOTS = ITEMS.register("mithril_boots", () -> new ModArmorItem(ModArmorMaterial.MITHRIL, EquipmentSlotType.FEET, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- БРОНЯ: АДАМАНТИЙ ---
    public static final RegistryObject<Item> ADAMANTIUM_HELMET = ITEMS.register("adamantium_helmet", () -> new ModArmorItem(ModArmorMaterial.ADAMANTIUM, EquipmentSlotType.HEAD, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ADAMANTIUM_CHESTPLATE = ITEMS.register("adamantium_chestplate", () -> new ModArmorItem(ModArmorMaterial.ADAMANTIUM, EquipmentSlotType.CHEST, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ADAMANTIUM_LEGGINGS = ITEMS.register("adamantium_leggings", () -> new ModArmorItem(ModArmorMaterial.ADAMANTIUM, EquipmentSlotType.LEGS, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ADAMANTIUM_BOOTS = ITEMS.register("adamantium_boots", () -> new ModArmorItem(ModArmorMaterial.ADAMANTIUM, EquipmentSlotType.FEET, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- БРОНЯ: ОРИХАЛК ---
    public static final RegistryObject<Item> ORICHALCUM_HELMET = ITEMS.register("orichalcum_helmet", () -> new ModArmorItem(ModArmorMaterial.ORICHALCUM, EquipmentSlotType.HEAD, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ORICHALCUM_CHESTPLATE = ITEMS.register("orichalcum_chestplate", () -> new ModArmorItem(ModArmorMaterial.ORICHALCUM, EquipmentSlotType.CHEST, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ORICHALCUM_LEGGINGS = ITEMS.register("orichalcum_leggings", () -> new ModArmorItem(ModArmorMaterial.ORICHALCUM, EquipmentSlotType.LEGS, new Item.Properties().tab(ModItemGroup.INSTANCE)));
    public static final RegistryObject<Item> ORICHALCUM_BOOTS = ITEMS.register("orichalcum_boots", () -> new ModArmorItem(ModArmorMaterial.ORICHALCUM, EquipmentSlotType.FEET, new Item.Properties().tab(ModItemGroup.INSTANCE)));

    // --- ЗЕЛЬЯ ---
    public static final RegistryObject<Item> MANA_POTION = ITEMS.register("mana_potion",
            () -> new RpgPotionItem(true, new Item.Properties().stacksTo(16).tab(ModItemGroup.INSTANCE)));

    public static final RegistryObject<Item> HEALTH_POTION = ITEMS.register("health_potion",
            () -> new RpgPotionItem(false, new Item.Properties().stacksTo(16).tab(ModItemGroup.INSTANCE)));

    // НОВОЕ: ЗОЛОТОЕ ЗЕЛЬЕ
    public static final RegistryObject<Item> LUCK_POTION = ITEMS.register("luck_potion",
            () -> new LuckPotionItem(new Item.Properties().stacksTo(16).tab(ModItemGroup.INSTANCE)));
}