package com.example.examplemod.event;

import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class NpcSpawnHandler {

    private static final Random RANDOM = new Random();
    private static final int NPC_CHANCE_PERCENT = 40;
    private static final String PROCESSED_TAG = "RpgNpcProcessed";

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (!(event.getWorld() instanceof ServerWorld)) return;
        if (!(event.getEntity() instanceof VillagerEntity)) return;
        processVillager((VillagerEntity) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        if (player.tickCount % 20 != 0) return;

        List<VillagerEntity> villagers = player.level.getEntitiesOfClass(
                VillagerEntity.class,
                player.getBoundingBox().inflate(64)
        );

        for (VillagerEntity villager : villagers) {
            processVillager(villager);
        }
    }

    private static void processVillager(VillagerEntity villager) {
        if (villager.getPersistentData().getBoolean(PROCESSED_TAG)) return;
        villager.getPersistentData().putBoolean(PROCESSED_TAG, true);

        // Если у жителя уже стоит НАШЕ NPC-имя — пропускаем
        if (villager.hasCustomName()) {
            String oldName = villager.getCustomName().getString();
            if (oldName.contains("Глава Гильдии") || oldName.contains("Оружейник")
                    || oldName.contains("Бронник") || oldName.contains("Маг")) {
                return;
            }
        }

        // Проверка шанса
        if (RANDOM.nextInt(100) >= NPC_CHANCE_PERCENT) return;

        // Вычисляем уровень моба САМИ (не полагаемся на MobLevelHandler)
        int level = calculateLevel(villager);

        // Выбираем тип NPC
        int type = RANDOM.nextInt(4);
        String npcBaseName;
        switch (type) {
            case 0:
                npcBaseName = "§6Глава Гильдии";
                villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NITWIT));
                break;
            case 1:
                npcBaseName = "§cОружейник";
                villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.WEAPONSMITH));
                break;
            case 2:
                npcBaseName = "§7Бронник";
                villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.ARMORER));
                break;
            default:
                npcBaseName = "§5Маг";
                villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.CLERIC));
                break;
        }

        // Формируем финальное имя: [Lvl X] §6Глава Гильдии
        String finalName = "[Lvl " + level + "] " + npcBaseName;
        villager.setCustomName(new StringTextComponent(finalName));
        villager.setCustomNameVisible(true);
    }

    // Считает уровень по той же формуле, что и MobLevelHandler (расстояние от центра)
    private static int calculateLevel(VillagerEntity villager) {
        // Если имя уже есть с [Lvl X] — используем его
        if (villager.hasCustomName()) {
            String name = villager.getCustomName().getString();
            if (name.startsWith("[Lvl ")) {
                int end = name.indexOf("]");
                if (end > 5) {
                    try {
                        return Integer.parseInt(name.substring(5, end));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        // Иначе считаем по расстоянию от центра мира (0, 64, 0)
        double dx = villager.getX() - 0.0;
        double dy = villager.getY() - 64.0;
        double dz = villager.getZ() - 0.0;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return Math.max(1, 1 + (int) (distance / 100.0));
    }
}