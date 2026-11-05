package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "examplemod")
public class XpEventHandler {

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof PlayerEntity)) return;
        if (!(event.getEntityLiving() instanceof MobEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
        MobEntity killedMob = (MobEntity) event.getEntityLiving();

        int mobLevel = killedMob.getPersistentData().getInt("RpgMobLevel");
        if (mobLevel <= 0 && killedMob.hasCustomName()) {
            String name = killedMob.getCustomName().getString();
            if (name.startsWith("[Lvl ")) {
                int endIndex = name.indexOf("]");
                if (endIndex > 5) {
                    try { mobLevel = Integer.parseInt(name.substring(5, endIndex)); } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (mobLevel <= 0) mobLevel = 1;

        final int finalMobLevel = mobLevel;
        int xpReward = finalMobLevel * 10;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            data.addXp(xpReward);
            PlayerEventHandler.applyStats(player, data);

            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                        // --- ИСПРАВЛЕНИЕ: Добавлены последние два параметра (HealthRegen и ManaRegen) ---
                        new SyncLevelPacket(
                                data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(), data.getMana(),
                                data.getHealthRegen(), data.getManaRegen()
                        )
                );
            }
        });
    }
}