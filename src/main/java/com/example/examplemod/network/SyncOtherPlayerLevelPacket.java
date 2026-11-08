package com.example.examplemod.network;

import com.example.examplemod.client.ClientOtherPlayerData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncOtherPlayerLevelPacket {
    private final UUID playerId;
    private final int level;

    public SyncOtherPlayerLevelPacket(UUID playerId, int level) {
        this.playerId = playerId;
        this.level = level;
    }

    public static void encode(SyncOtherPlayerLevelPacket msg, PacketBuffer buf) {
        buf.writeUUID(msg.playerId);
        buf.writeInt(msg.level);
    }

    public static SyncOtherPlayerLevelPacket decode(PacketBuffer buf) {
        return new SyncOtherPlayerLevelPacket(buf.readUUID(), buf.readInt());
    }

    public static void handle(SyncOtherPlayerLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientOtherPlayerData.setLevel(msg.playerId, msg.level)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}