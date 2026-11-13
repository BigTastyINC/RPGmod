package com.example.examplemod.network;

import com.example.examplemod.client.SpellBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class OpenSpellBookGuiPacket {
    public OpenSpellBookGuiPacket() {}

    public static void encode(OpenSpellBookGuiPacket msg, PacketBuffer buf) {}
    public static OpenSpellBookGuiPacket decode(PacketBuffer buf) { return new OpenSpellBookGuiPacket(); }

    public static void handle(OpenSpellBookGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                // КЛИЕНТ: открываем GUI
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        Minecraft.getInstance().setScreen(new SpellBookScreen())
                );
            } else {
                // СЕРВЕР: отправляем пакет только отправителю
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenSpellBookGuiPacket());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}