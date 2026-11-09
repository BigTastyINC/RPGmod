package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.quest.Quest;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AcceptQuestPacket {
    private final Quest quest;

    public AcceptQuestPacket(Quest quest) {
        this.quest = quest;
    }

    public static void encode(AcceptQuestPacket msg, PacketBuffer buf) {
        buf.writeNbt(msg.quest.serializeNBT());
    }

    public static AcceptQuestPacket decode(PacketBuffer buf) {
        return new AcceptQuestPacket(Quest.deserializeNBT(buf.readNbt()));
    }

    public static void handle(AcceptQuestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                    boolean alreadyHas = data.getActiveQuests().stream()
                            .anyMatch(q -> q.getId().equals(msg.quest.getId()));

                    if (!alreadyHas) {
                        data.addQuest(msg.quest);
                        // Здесь тоже используем append
                        player.sendMessage(new StringTextComponent("§aКвест принят: §e").append(msg.quest.getDescription()), player.getUUID());

                        NetworkHandler.INSTANCE.send(
                                net.minecraftforge.fml.network.PacketDistributor.PLAYER.with(() -> player),
                                new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                        data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                        data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                        data.getStamina(), data.getStaminaRegen(),
                                        data.getCopper(), data.getSilver(), data.getGold())
                        );
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}