package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.entity.MobRarity;
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
        if (mobLevel <= 0) mobLevel = 1;

        // Получаем редкость моба (0 = Common, 1 = Leader, 2 = Lord)
        int rarityIndex = killedMob.getPersistentData().getInt("RpgMobRarity");
        MobRarity[] rarities = MobRarity.values();
        MobRarity rarity = rarities[Math.min(rarityIndex, rarities.length - 1)];

        final int finalMobLevel = mobLevel;
        final MobRarity finalRarity = rarity;

        // Базовый опыт и монеты умножаются на редкость
        final int baseXpReward = (int) (finalMobLevel * 10 * finalRarity.getXpMultiplier());
        final int baseCoinReward = (int) (finalMobLevel * 2 * finalRarity.getXpMultiplier());

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            int actualXp = baseXpReward;
            if (data.hasBuff(RpgBuffs.XP_BOOST)) {
                long end = data.getBuffEnd(RpgBuffs.XP_BOOST);
                if (player.level.getGameTime() < end) {
                    actualXp = (int) (actualXp * (1.0f + data.getBuffPower(RpgBuffs.XP_BOOST)));
                } else {
                    data.clearBuff(RpgBuffs.XP_BOOST);
                }
            }

            data.addXp(actualXp);
            data.addCoins(baseCoinReward);
            PlayerEventHandler.applyStats(player, data);

            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                        new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                data.getStamina(), data.getStaminaRegen(),
                                data.getCopper(), data.getSilver(), data.getGold())
                );
            }
        });
    }
}