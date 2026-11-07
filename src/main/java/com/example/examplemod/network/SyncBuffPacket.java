package com.example.examplemod.network;

import com.example.examplemod.client.ClientBuffTracker;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBuffPacket {
    private final int buffId;
    private final long endTime;

    public SyncBuffPacket(int buffId, long endTime) {
        this.buffId = buffId;
        this.endTime = endTime;
    }

    public static void encode(SyncBuffPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.buffId);
        buf.writeLong(msg.endTime);
    }

    public static SyncBuffPacket decode(PacketBuffer buf) {
        return new SyncBuffPacket(buf.readInt(), buf.readLong());
    }

    public static void handle(SyncBuffPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientBuffTracker.addBuffLocally(msg.buffId, msg.endTime)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}