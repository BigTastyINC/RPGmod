package com.example.examplemod.client;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.event.FoodBuffHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ClientBuffTracker {

    private static final Map<Integer, Long> buffEndTimes = new HashMap<>();

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (!player.level.isClientSide) return;

        ItemStack stack = event.getItem();
        if (!FoodBuffHandler.isPreparedFood(stack.getItem())) return;
        if (!stack.hasTag() || !stack.getTag().contains("RarityLevel")) return;

        int rarity = Math.max(0, Math.min(4, stack.getTag().getInt("RarityLevel")));
        int buffId = RpgBuffs.getBuffForFood(stack.getItem());
        int durationTicks = RpgBuffs.DURATION_MINUTES[rarity] * 60 * 20;
        long endTime = player.level.getGameTime() + durationTicks;

        // Если бафф ещё активен — берём максимальное время
        if (buffEndTimes.containsKey(buffId) && buffEndTimes.get(buffId) > player.level.getGameTime()) {
            long oldEnd = buffEndTimes.get(buffId);
            if (oldEnd > endTime) endTime = oldEnd;
        }
        buffEndTimes.put(buffId, endTime);
    }

    public static Map<Integer, Long> getActiveBuffs() {
        return buffEndTimes;
    }
}