package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.entity.MobRarity;
import com.example.examplemod.profession.Profession;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod")
public class HunterEventHandler {

    @SubscribeEvent
    public static void onAnimalKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof PlayerEntity)) return;
        if (!(event.getEntityLiving() instanceof AnimalEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
        if (player.level.isClientSide) return;

        int mobLevel = event.getEntityLiving().getPersistentData().getInt("RpgMobLevel");
        if (mobLevel <= 0) mobLevel = 1;

        int rarityIndex = event.getEntityLiving().getPersistentData().getInt("RpgMobRarity");
        MobRarity rarity = MobRarity.values()[Math.min(rarityIndex, MobRarity.values().length - 1)];

        final int finalMobLevel = mobLevel;
        final MobRarity finalRarity = rarity;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            // Охотник получает опыт от уровня и редкости моба
            int baseXp = 10 + (finalMobLevel * 2);
            int xpGain = (int) (baseXp * finalRarity.getXpMultiplier());
            xpGain = PlayerEventHandler.getProfessionXpWithBonus(player, data, xpGain);

            data.addProfessionXp(Profession.HUNTER, xpGain);
            PlayerEventHandler.syncData(player, data);
        });
    }
}