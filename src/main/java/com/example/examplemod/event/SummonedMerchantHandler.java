package com.example.examplemod.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod")
public class SummonedMerchantHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntityLiving();

        // Проверяем только жителей, которых призвали мы
        if (entity instanceof VillagerEntity) {
            if (entity.getPersistentData().getBoolean("IsSummonedMerchant")) {
                long spawnTime = entity.getPersistentData().getLong("SummonTime");

                // 5 минут * 60 секунд * 20 тиков = 6000 тиков
                if (entity.level.getGameTime() - spawnTime > 6000) {
                    entity.remove();
                }
            }
        }
    }
}