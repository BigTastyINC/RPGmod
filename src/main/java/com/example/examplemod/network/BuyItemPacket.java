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

public class BuyItemPacket {
    private final ItemStack stack;
    private final int price;

    public BuyItemPacket(ItemStack stack, int price) {
        this.stack = stack.copy();
        this.price = price;
    }

    public static void encode(BuyItemPacket msg, PacketBuffer buf) {
        buf.writeItemStack(msg.stack, false);
        buf.writeInt(msg.price);
    }

    public static BuyItemPacket decode(PacketBuffer buf) {
        return new BuyItemPacket(buf.readItem(), buf.readInt());
    }

    public static void handle(BuyItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int total = ShopUtils.getTotalCopper(data.getCopper(), data.getSilver(), data.getGold());
                if (total < msg.price) {
                    player.sendMessage(new StringTextComponent("§cНедостаточно монет!"), player.getUUID());
                    return;
                }
                ShopUtils.setFromTotalCopper(data, total - msg.price);
                player.inventory.add(msg.stack.copy());
                player.sendMessage(new StringTextComponent("§aКуплено: " + msg.stack.getDisplayName().getString() + " за " + msg.price + " меди"), player.getUUID());
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                data.getStamina(), data.getStaminaRegen(),
                                data.getCopper(), data.getSilver(), data.getGold()));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}