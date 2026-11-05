package com.example.examplemod.block;

import com.example.examplemod.item.ModItemGroup;
import com.example.examplemod.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "examplemod");

    public static final RegistryObject<Block> SILVER_ORE = registerBlock("silver_ore",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.IRON_ORE).harvestLevel(2).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> COBALT_ORE = registerBlock("cobalt_ore",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.DIAMOND_ORE).harvestLevel(3).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> MITHRIL_ORE = registerBlock("mithril_ore",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.ANCIENT_DEBRIS).harvestLevel(4).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ADAMANTIUM_ORE = registerBlock("adamantium_ore",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.ANCIENT_DEBRIS).harvestLevel(4).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ORICHALCUM_ORE = registerBlock("orichalcum_ore",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.ANCIENT_DEBRIS).harvestLevel(4).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, java.util.function.Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        // Используем нашу новую вкладку для блоков
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties().tab(ModItemGroup.INSTANCE)));
        return toReturn;
    }
}