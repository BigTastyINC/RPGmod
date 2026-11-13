package com.example.examplemod.jewelry;

import com.example.examplemod.item.ModItemGroup;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModJewelryItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "examplemod");

    // Кольца
    public static final RegistryObject<Item> RING_OF_STRENGTH = ITEMS.register("ring_of_strength",
            () -> new JewelryItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE), JewelryType.RING));

    public static final RegistryObject<Item> RING_OF_MAGIC = ITEMS.register("ring_of_magic",
            () -> new JewelryItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE), JewelryType.RING));

    // Амулет
    public static final RegistryObject<Item> AMULET_OF_PROTECTION = ITEMS.register("amulet_of_protection",
            () -> new JewelryItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE), JewelryType.AMULET));

    // Серьга
    public static final RegistryObject<Item> EARRING_OF_DEXTERITY = ITEMS.register("earring_of_dexterity",
            () -> new JewelryItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE), JewelryType.EARRING));

    // Браслет
    public static final RegistryObject<Item> BRACELET_OF_SWIFTNESS = ITEMS.register("bracelet_of_swiftness",
            () -> new JewelryItem(new Item.Properties().stacksTo(1).tab(ModItemGroup.INSTANCE), JewelryType.BRACELET));
}