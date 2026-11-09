package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.entity.MobRarity;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import com.example.examplemod.quest.Quest;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "examplemod")
public class QuestEventHandler {

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        // Проверяем, что убил именно игрок
        if (!(event.getSource().getEntity() instanceof PlayerEntity)) return;
        // Проверяем, что убили моба
        if (!(event.getEntityLiving() instanceof MobEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
        MobEntity killedMob = (MobEntity) event.getEntityLiving();

        if (player.level.isClientSide) return;

        // Получаем данные о мобе
        int mobLevelRaw = killedMob.getPersistentData().getInt("RpgMobLevel");
        final int mobLevel = mobLevelRaw <= 0 ? 1 : mobLevelRaw; // final для лямбды

        int rarityIndex = killedMob.getPersistentData().getInt("RpgMobRarity");
        final MobRarity rarity = MobRarity.values()[Math.min(rarityIndex, MobRarity.values().length - 1)];

        // Полный ID моба (например, "minecraft:pig")
        final String mobId = killedMob.getType().getRegistryName().toString();

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            boolean progressChanged = false;

            for (Quest quest : data.getActiveQuests()) {
                if (quest.isCompleted()) continue;

                // Проверяем квест на убийство обычного моба
                if (quest.getType() == Quest.Type.KILL_MOB && quest.getTarget().equals(mobId)) {
                    if (mobLevel >= quest.getMinLevel()) {
                        quest.addProgress(1);
                        progressChanged = true;
                    }
                }
                // Проверяем квест на убийство Лорда
                else if (quest.getType() == Quest.Type.KILL_BOSS && quest.getTarget().equals(mobId)) {
                    if (rarity == MobRarity.LORD) {
                        quest.addProgress(1);
                        progressChanged = true;
                    }
                }
            }

            if (progressChanged) {
                player.displayClientMessage(new StringTextComponent("§aПрогресс квеста обновлен!"), true);

                // Синхронизируем с клиентом, чтобы GUI обновилось
                if (player instanceof ServerPlayerEntity) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                            new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                    data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                    data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                    data.getStamina(), data.getStaminaRegen(),
                                    data.getCopper(), data.getSilver(), data.getGold()));
                }
            }
        });
    }
}