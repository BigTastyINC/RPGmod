package com.example.examplemod.network;

import com.example.examplemod.client.StatsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class OpenStatsGuiPacket {
    public OpenStatsGuiPacket() {}

    public static void encode(OpenStatsGuiPacket msg, PacketBuffer buf) {}
    public static OpenStatsGuiPacket decode(PacketBuffer buf) { return new OpenStatsGuiPacket(); }

    public static void handle(OpenStatsGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                // КЛИЕНТ: получили подтверждение от сервера — открываем GUI
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        Minecraft.getInstance().setScreen(new StatsScreen())
                );
            } else {
                // СЕРВЕР: получили запрос от клиента — отправляем пакет только этому игроку
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenStatsGuiPacket());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}