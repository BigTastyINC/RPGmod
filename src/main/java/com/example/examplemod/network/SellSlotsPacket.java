package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.container.TradeContainer;
import com.example.examplemod.shop.ShopUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class SellSlotsPacket {
    public SellSlotsPacket() {}

    public static void encode(SellSlotsPacket msg, PacketBuffer buf) {}
    public static SellSlotsPacket decode(PacketBuffer buf) { return new SellSlotsPacket(); }

    public static void handle(SellSlotsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            if (player.containerMenu instanceof TradeContainer) {
                TradeContainer container = (TradeContainer) player.containerMenu;

                int totalPrice = 0;
                int itemsSold = 0;

                for (int i = 0; i < 10; i++) {
                    ItemStack stack = container.getSlot(i).getItem();

                    // НОВОЕ: принимаем тома, реликвии, а также предметы с редкостью/уровнем
                    if (!stack.isEmpty() && stack.hasTag()) {
                        CompoundNBT nbt = stack.getTag();
                        if (nbt.contains("ItemLevel")
                                || nbt.contains("RarityLevel")
                                || nbt.contains("SpellId")
                                || nbt.contains("RelicBasePrice")) {
                            int price = ShopUtils.calcSellPrice(stack) * stack.getCount();
                            totalPrice += price;
                            itemsSold += stack.getCount();
                            container.getSlot(i).set(ItemStack.EMPTY);
                        }
                    }
                }

                if (totalPrice > 0) {
                    final int finalTotalPrice = totalPrice;
                    final int finalItemsSold = itemsSold;

                    player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                        data.addCoins(finalTotalPrice);
                        player.sendMessage(new StringTextComponent("§aПродано §e" + finalItemsSold + " шт. §aза §6" + ShopUtils.formatPrice(finalTotalPrice)), player.getUUID());

                        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                        data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(),
                                        data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                        data.getStamina(), data.getStaminaRegen(),
                                        data.getCopper(), data.getSilver(), data.getGold()));
                    });
                } else {
                    player.sendMessage(new StringTextComponent("§cВ слотах нет предметов для продажи!"), player.getUUID());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}