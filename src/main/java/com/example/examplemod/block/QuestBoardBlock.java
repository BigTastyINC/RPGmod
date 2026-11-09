package com.example.examplemod.block;

import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenQuestBoardPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class QuestBoardBlock extends Block {
    public QuestBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isClientSide && player instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.sendToServer(new OpenQuestBoardPacket());
            // Или правильнее: отправить пакет с сервера на клиент
            NetworkHandler.INSTANCE.send(net.minecraftforge.fml.network.PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new OpenQuestBoardPacket());
        }
        return ActionResultType.SUCCESS;
    }
}