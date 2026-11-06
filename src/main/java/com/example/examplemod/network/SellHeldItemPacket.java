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
            if (!held.hasTag() || !held.getTag().contains("ItemLevel")) {
                player.sendMessage(new StringTextComponent("§cЭтот предмет не имеет уровня, торговцы такое не берут!"), player.getUUID());
                return;
            }
            int price = ShopUtils.calcSellPrice(held);
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                data.addCoins(price);
                held.shrink(1);
                player.sendMessage(new StringTextComponent("§aПродано за " + price + " меди"), player.getUUID());
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(),
                                data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                data.getStamina(), data.getCopper(), data.getSilver(), data.getGold()));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}