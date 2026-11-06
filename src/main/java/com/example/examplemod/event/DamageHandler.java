package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod")
public class DamageHandler {

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (player.level.isClientSide) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            if (data.hasBuff(RpgBuffs.DAMAGE_RESIST)) {
                long end = data.getBuffEnd(RpgBuffs.DAMAGE_RESIST);
                if (player.level.getGameTime() < end) {
                    float reduce = data.getBuffPower(RpgBuffs.DAMAGE_RESIST);
                    event.setAmount(event.getAmount() * (1.0f - reduce));
                } else {
                    data.clearBuff(RpgBuffs.DAMAGE_RESIST);
                }
            }
        });
    }
}