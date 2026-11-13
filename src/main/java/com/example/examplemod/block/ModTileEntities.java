package com.example.examplemod.block;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "examplemod");

    public static final RegistryObject<TileEntityType<QuestBoardTileEntity>> QUEST_BOARD =
            TILE_ENTITIES.register("quest_board", () ->
                    TileEntityType.Builder.of(QuestBoardTileEntity::new, ModBlocks.QUEST_BOARD.get()).build(null));
}