package com.example.examplemod.event;

import com.example.examplemod.entity.SummonedSkeletonArcher;
import com.example.examplemod.entity.SummonedSkeletonWarrior;
import com.example.examplemod.entity.SummonedZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "examplemod")
public class SummonedUndeadHandler {

    private static boolean isSummon(Entity entity) {
        return entity instanceof SummonedSkeletonArcher
                || entity instanceof SummonedSkeletonWarrior
                || entity instanceof SummonedZombie;
    }

    // Таймер жизни 5 минут
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntityLiving();
        if (entity.level.isClientSide) return;
        if (!isSummon(entity)) return;
        if (!entity.getPersistentData().contains("SummonTime")) return;

        long summonTime = entity.getPersistentData().getLong("SummonTime");
        if (entity.level.getGameTime() - summonTime > 6000) {
            entity.remove();
        }
    }

    // Запрет бить своих
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide) return;
        if (!isSummon(target)) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;

        UUID ownerUUID = target.getPersistentData().hasUUID("OwnerUUID")
                ? target.getPersistentData().getUUID("OwnerUUID")
                : null;

        if (ownerUUID != null && ownerUUID.equals(attacker.getUUID())) {
            event.setCanceled(true);
        }
    }

    // При выходе игрока — удалить его призывы
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        UUID uuid = player.getUUID();
        if (player.level instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) player.level;
            serverWorld.getEntities().forEach(entity -> {
                if (isSummon(entity)
                        && entity.getPersistentData().hasUUID("OwnerUUID")
                        && entity.getPersistentData().getUUID("OwnerUUID").equals(uuid)) {
                    entity.remove();
                }
            });
        }
    }

    // При смерти игрока — удалить его призывы
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        UUID uuid = player.getUUID();
        if (player.level instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) player.level;
            serverWorld.getEntities().forEach(entity -> {
                if (isSummon(entity)
                        && entity.getPersistentData().hasUUID("OwnerUUID")
                        && entity.getPersistentData().getUUID("OwnerUUID").equals(uuid)) {
                    entity.remove();
                }
            });
        }
    }
}