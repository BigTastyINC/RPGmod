package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.quest.Quest;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class AbandonQuestPacket {
    private final UUID questId;

    public AbandonQuestPacket(UUID questId) {
        this.questId = questId;
    }

    public static void encode(AbandonQuestPacket msg, PacketBuffer buf) {
        buf.writeUUID(msg.questId);
    }

    public static AbandonQuestPacket decode(PacketBuffer buf) {
        return new AbandonQuestPacket(buf.readUUID());
    }

    public static void handle(AbandonQuestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                Quest toRemove = null;
                for (Quest q : data.getActiveQuests()) {
                    if (q.getId().equals(msg.questId)) {
                        toRemove = q;
                        break;
                    }
                }

                if (toRemove != null) {
                    data.removeQuest(toRemove);
                    player.sendMessage(new StringTextComponent("§cВы отказались от квеста: §e" + toRemove.getDescription().getString()), player.getUUID());

                    // Синхронизируем данные с клиентом
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                    data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                    data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                    data.getStamina(), data.getStaminaRegen(),
                                    data.getCopper(), data.getSilver(), data.getGold()));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}