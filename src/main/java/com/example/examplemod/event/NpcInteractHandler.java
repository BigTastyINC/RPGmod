package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenShopPacket;
import com.example.examplemod.shop.ShopUtils;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class NpcInteractHandler {

    private static final long REFRESH_TIME_TICKS = 6000L; // 5 минут

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof VillagerEntity)) return;
        VillagerEntity villager = (VillagerEntity) event.getTarget();
        if (!villager.hasCustomName()) return;

        String name = villager.getCustomName().getString();
        int merchantType;
        if (name.contains("Оружейник")) merchantType = 0;
        else if (name.contains("Бронник")) merchantType = 1;
        else if (name.contains("Маг") || name.contains("Глава Гильдии")) {
            if (event.getPlayer() instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) event.getPlayer()).sendMessage(
                        new StringTextComponent("§6Этот торговец пока не готов к торговле. Загляните позже."),
                        event.getPlayer().getUUID());
            }
            return;
        }
        else return;

        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);

        // Стабильный seed: UUID жителя + номер 5-минутного окна
        long timeWindow = player.level.getGameTime() / REFRESH_TIME_TICKS;
        long seed = villager.getUUID().getMostSignificantBits() ^ (timeWindow * 31);
        Random rand = new Random(seed);

        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            for (int i = 0; i < 5; i++) {
                // Уровень: от -10 до +10 от уровня игрока (но не меньше 1)
                int level = Math.max(1, data.getLevel() + rand.nextInt(21) - 10);

                // Редкость: 7% на Легендарный, иначе 0..3
                int rarity = rand.nextInt(100) < 7 ? 4 : rand.nextInt(4);

                ItemStack stack = ShopUtils.generateItemForMerchant(rand, merchantType, level, rarity);
                int price = ShopUtils.calcBuyPrice(stack);
                items.add(stack);
                prices.add(price);
            }
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new OpenShopPacket(items, prices, data.getCopper(), data.getSilver(), data.getGold()));
        });
    }
}