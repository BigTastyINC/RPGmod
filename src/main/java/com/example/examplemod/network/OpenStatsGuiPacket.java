package com.example.examplemod.network;

import com.example.examplemod.client.StatsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenStatsGuiPacket {
    public OpenStatsGuiPacket() {}

    public static void encode(OpenStatsGuiPacket msg, PacketBuffer buf) {}
    public static OpenStatsGuiPacket decode(PacketBuffer buf) { return new OpenStatsGuiPacket(); }

    public static void handle(OpenStatsGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    // Исправлено: displayGuiScreen -> setScreen
                    Minecraft.getInstance().setScreen(new StatsScreen())
            );
        });
        ctx.get().setPacketHandled(true);
    }
}