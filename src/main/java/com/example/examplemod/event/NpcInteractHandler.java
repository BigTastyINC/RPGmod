package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.container.TradeContainer;
import com.example.examplemod.shop.ShopUtils;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class NpcInteractHandler {

    private static final long REFRESH_TIME_TICKS = 6000L;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof VillagerEntity)) return;
        VillagerEntity villager = (VillagerEntity) event.getTarget();
        if (!villager.hasCustomName()) return;

        String name = villager.getCustomName().getString();
        int merchantType;
        if (name.contains("Оружейник")) merchantType = 0;
        else if (name.contains("Бронник")) merchantType = 1;
        else if (name.contains("Маг")) merchantType = 2;
        else if (name.contains("Глава Гильдии")) merchantType = 3;
        else return;

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.ActionResultType.SUCCESS);

        if (event.getPlayer().level.isClientSide) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        // 1. Пытаемся взять уровень из памяти
        int npcLevel = villager.getPersistentData().getInt("RpgNpcLevel");

        // 2. Если памяти нет (старый NPC), парсим имя "[Lvl 139] Маг"
        if (npcLevel <= 0) {
            if (name.startsWith("[Lvl ")) {
                int end = name.indexOf("]");
                if (end > 5) {
                    try {
                        npcLevel = Integer.parseInt(name.substring(5, end));
                    } catch (Exception ignored) {}
                }
            }
        }

        // 3. Защита от нуля
        if (npcLevel <= 0) npcLevel = 1;

        long timeWindow = player.level.getGameTime() / REFRESH_TIME_TICKS;
        long seed = villager.getUUID().getMostSignificantBits() ^ (timeWindow * 31);
        Random rand = new Random(seed);

        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();

        final int finalNpcLevel = npcLevel;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            for (int i = 0; i < 5; i++) {
                // Используем уровень NPC
                int level = Math.max(1, finalNpcLevel + rand.nextInt(21) - 10);

                // Шанс на Эпик растет с уровнем NPC
                int epicChance = Math.min(20, 7 + (finalNpcLevel / 10));
                int rarity = rand.nextInt(100) < epicChance ? 4 : rand.nextInt(4);

                ItemStack stack = ShopUtils.generateItemForMerchant(rand, merchantType, level, rarity);
                int price = ShopUtils.calcBuyPrice(stack);
                items.add(stack);
                prices.add(price);
            }

            NetworkHooks.openGui(player, new INamedContainerProvider() {
                @Override
                public StringTextComponent getDisplayName() {
                    return new StringTextComponent("Магазин");
                }

                @Override
                public TradeContainer createMenu(int i, net.minecraft.entity.player.PlayerInventory inv, PlayerEntity p) {
                    return new TradeContainer(i, inv, items, prices, data.getCopper(), data.getSilver(), data.getGold());
                }
            }, buf -> {
                buf.writeInt(items.size());
                for (ItemStack item : items) {
                    buf.writeItemStack(item, false);
                }
                buf.writeInt(prices.size());
                for (Integer price : prices) {
                    buf.writeInt(price);
                }
                buf.writeInt(data.getCopper());
                buf.writeInt(data.getSilver());
                buf.writeInt(data.getGold());
            });
        });
    }
}