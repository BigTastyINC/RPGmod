package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.shop.ShopUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class SellHeldItemPacket {
    public SellHeldItemPacket() {}

    public static void encode(SellHeldItemPacket msg, PacketBuffer buf) {}
    public static SellHeldItemPacket decode(PacketBuffer buf) { return new SellHeldItemPacket(); }

    public static void handle(SellHeldItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) {
                player.sendMessage(new StringTextComponent("§cВозьмите предмет в руку!"), player.getUUID());
                return;
            }

            // Еду продавать нельзя (по твоему правилу)
            if (held.getItem().isEdible()) {
                player.sendMessage(new StringTextComponent("§cТорговцы не покупают еду. Её можно только купить."), player.getUUID());
                return;
            }

            // Проверка на уровень предмета
            if (!held.hasTag() || !held.getTag().contains("ItemLevel")) {
                player.sendMessage(new StringTextComponent("§cЭтот предмет не имеет уровня, торговцы такое не берут!"), player.getUUID());
                return;
            }

            int count = held.getCount();
            int pricePerItem = ShopUtils.calcSellPrice(held);
            int totalPrice = pricePerItem * count; // Считаем за всю стопку

            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                data.addCoins(totalPrice);

                // Удаляем всю стопку из руки
                player.setItemInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);

                // Красивое сообщение (с форматом цены)
                String formattedPrice = ShopUtils.formatPrice(totalPrice);
                player.sendMessage(new StringTextComponent("§aПродано §e" + count + " шт. §aза §6" + formattedPrice), player.getUUID());

                // Синхронизация
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(),
                                data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                data.getStamina(), data.getStaminaRegen(),
                                data.getCopper(), data.getSilver(), data.getGold()));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}