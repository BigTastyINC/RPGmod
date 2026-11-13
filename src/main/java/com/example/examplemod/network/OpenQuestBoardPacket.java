package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.client.QuestScreen;
import com.example.examplemod.quest.Quest;
import com.example.examplemod.quest.QuestGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenQuestBoardPacket {
    private final List<Quest> availableQuests;
    private final List<Quest> activeQuests;

    public OpenQuestBoardPacket() {
        this.availableQuests = new ArrayList<>();
        this.activeQuests = new ArrayList<>();
    }

    public OpenQuestBoardPacket(List<Quest> availableQuests, List<Quest> activeQuests) {
        this.availableQuests = availableQuests;
        this.activeQuests = activeQuests;
    }

    public static void encode(OpenQuestBoardPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.availableQuests.size());
        for (Quest q : msg.availableQuests) buf.writeNbt(q.serializeNBT());

        buf.writeInt(msg.activeQuests.size());
        for (Quest q : msg.activeQuests) buf.writeNbt(q.serializeNBT());
    }

    public static OpenQuestBoardPacket decode(PacketBuffer buf) {
        int availSize = buf.readInt();
        List<Quest> avail = new ArrayList<>();
        for (int i = 0; i < availSize; i++) avail.add(Quest.deserializeNBT(buf.readNbt()));

        int activeSize = buf.readInt();
        List<Quest> active = new ArrayList<>();
        for (int i = 0; i < activeSize; i++) active.add(Quest.deserializeNBT(buf.readNbt()));

        return new OpenQuestBoardPacket(avail, active);
    }

    public static void handle(OpenQuestBoardPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        Minecraft.getInstance().setScreen(new QuestScreen(msg.availableQuests, msg.activeQuests, 0))
                );
            } else {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                        long currentTime = player.level.getGameTime();
                        long lastRefresh = data.getQuestBoardRefreshTime();

                        // НОВОЕ: Если прошло 5 минут (6000 тиков) или список пуст — генерируем новые
                        if (currentTime - lastRefresh >= 6000 || data.getAvailableQuests().isEmpty()) {
                            List<Quest> newQuests = QuestGenerator.generateDailyQuests(data.getLevel());
                            data.setAvailableQuests(newQuests);
                            data.setQuestBoardRefreshTime(currentTime);
                        }

                        // Отправляем клиенту
                        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                new OpenQuestBoardPacket(data.getAvailableQuests(), data.getActiveQuests()));
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}