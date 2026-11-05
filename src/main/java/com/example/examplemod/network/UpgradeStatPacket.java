package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.event.PlayerEventHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class UpgradeStatPacket {
    private final String statName;

    public UpgradeStatPacket(String statName) {
        this.statName = statName;
    }

    public static void encode(UpgradeStatPacket msg, PacketBuffer buf) {
        buf.writeUtf(msg.statName);
    }

    public static UpgradeStatPacket decode(PacketBuffer buf) {
        return new UpgradeStatPacket(buf.readUtf(32767));
    }

    public static void handle(UpgradeStatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                    if (data.getStatPoints() > 0) {
                        boolean success = false;
                        switch (msg.statName) {
                            case "strength": data.setStrength(data.getStrength() + 1); success = true; break;
                            case "dexterity": data.setDexterity(data.getDexterity() + 1); success = true; break;
                            case "intelligence": data.setIntelligence(data.getIntelligence() + 1); success = true; break;
                            case "luck": data.setLuck(data.getLuck() + 1); success = true; break;
                            case "vitality": data.setVitality(data.getVitality() + 1); success = true; break;
                            case "healthregen": data.setHealthRegen(data.getHealthRegen() + 1); success = true; break;
                            case "manaregen": data.setManaRegen(data.getManaRegen() + 1); success = true; break;
                        }

                        if (success) {
                            data.setStatPoints(data.getStatPoints() - 1);
                            PlayerEventHandler.applyStats(player, data);

                            NetworkHandler.INSTANCE.send(
                                    PacketDistributor.PLAYER.with(() -> player),
                                    new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                            data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(), data.getMana(),
                                            data.getHealthRegen(), data.getManaRegen())
                            );
                        }
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}