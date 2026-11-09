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

        int npcLevel = villager.getPersistentData().getInt("RpgNpcLevel");

        if (npcLevel <= 0) {
            int start = name.indexOf("[Lvl ");
            if (start != -1) {
                int end = name.indexOf("]", start);
                if (end > start + 5) {
                    try {
                        npcLevel = Integer.parseInt(name.substring(start + 5, end));
                    } catch (Exception ignored) {}
                }
            }
        }

        if (npcLevel <= 0) npcLevel = 1;

        long timeWindow = player.level.getGameTime() / REFRESH_TIME_TICKS;
        long seed = villager.getUUID().getMostSignificantBits() ^ (timeWindow * 31);
        Random rand = new Random(seed);

        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();

        final int finalNpcLevel = npcLevel;
        final int finalMerchantType = merchantType;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            for (int i = 0; i < 5; i++) {
                int level = Math.max(1, finalNpcLevel + rand.nextInt(21) - 10);

                int rarity;
                if (finalMerchantType == 0 || finalMerchantType == 1) {
                    // НОВОЕ: Оружейник (0) и Бронник (1) — только Common (0), Uncommon (1), Rare (2)
                    rarity = rand.nextInt(3);
                } else {
                    // Маг (2) и Глава Гильдии (3) — обычная система с шансом на Legendary
                    int epicChance = Math.min(20, 7 + (finalNpcLevel / 10));
                    rarity = rand.nextInt(100) < epicChance ? 4 : rand.nextInt(4);
                }

                ItemStack stack = ShopUtils.generateItemForMerchant(rand, finalMerchantType, level, rarity);
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