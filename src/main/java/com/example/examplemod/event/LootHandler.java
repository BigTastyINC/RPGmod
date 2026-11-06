package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class LootHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        // ИСПРАВЛЕНО: getEntity() вместо getTrueSource()
        Entity source = event.getSource().getEntity();

        if (!(source instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) source;
        if (player.level.isClientSide) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            if (data.hasBuff(RpgBuffs.LOOT_LUCK)) {
                long end = data.getBuffEnd(RpgBuffs.LOOT_LUCK);
                if (player.level.getGameTime() < end) {
                    float chance = data.getBuffPower(RpgBuffs.LOOT_LUCK);
                    List<ItemStack> extra = new ArrayList<>();
                    for (ItemEntity drop : event.getDrops()) {
                        if (RANDOM.nextFloat() < chance) {
                            extra.add(drop.getItem().copy());
                        }
                    }
                    for (ItemStack item : extra) {
                        ItemEntity ie = new ItemEntity(event.getEntityLiving().level,
                                event.getEntityLiving().getX(),
                                event.getEntityLiving().getY(),
                                event.getEntityLiving().getZ(),
                                item);
                        event.getEntityLiving().level.addFreshEntity(ie);
                    }
                } else {
                    data.clearBuff(RpgBuffs.LOOT_LUCK);
                }
            }
        });
    }
}