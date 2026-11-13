package com.example.examplemod.block;

import net.minecraft.tileentity.TileEntity;

public class QuestBoardTileEntity extends TileEntity {
    // Пустой конструктор, который требует Builder
    public QuestBoardTileEntity() {
        super(ModTileEntities.QUEST_BOARD.get());
    }
}