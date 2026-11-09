package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.event.PlayerEventHandler;
import com.example.examplemod.quest.Quest;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class CompleteQuestPacket {
    private final Quest quest;

    public CompleteQuestPacket(Quest quest) {
        this.quest = quest;
    }

    public static void encode(CompleteQuestPacket msg, PacketBuffer buf) {
        buf.writeNbt(msg.quest.serializeNBT());
    }

    public static CompleteQuestPacket decode(PacketBuffer buf) {
        return new CompleteQuestPacket(Quest.deserializeNBT(buf.readNbt()));
    }

    public static void handle(CompleteQuestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                    // Ищем квест в списке игрока по ID
                    Quest activeQuest = null;
                    for (Quest q : data.getActiveQuests()) {
                        if (q.getId().equals(msg.quest.getId())) {
                            activeQuest = q;
                            break;
                        }
                    }

                    if (activeQuest == null) return;

                    if (!activeQuest.isCompleted()) {
                        player.sendMessage(new StringTextComponent("§cКвест ещё не выполнен!"), player.getUUID());
                        return;
                    }

                    // Выдаем награду
                    data.addCoins(activeQuest.getRewardCoins());
                    data.addXp(activeQuest.getRewardXp());

                    player.sendMessage(new StringTextComponent(
                            "§6✦ Квест выполнен! Награда: §e" + activeQuest.getRewardCoins() + " меди, "
                                    + activeQuest.getRewardXp() + " опыта ✦"
                    ), player.getUUID());

                    // Удаляем квест из списка
                    data.removeQuest(activeQuest);

                    // Синхронизация данных игрока (опыт, деньги)
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            PlayerEventHandler.buildSyncPacket(data));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}