package com.example.examplemod.network;

import com.example.examplemod.client.TradeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenShopPacket {
    public List<ItemStack> items;
    public List<Integer> prices;
    public int copper, silver, gold;

    public OpenShopPacket(List<ItemStack> items, List<Integer> prices, int copper, int silver, int gold) {
        this.items = items;
        this.prices = prices;
        this.copper = copper;
        this.silver = silver;
        this.gold = gold;
    }

    public static void encode(OpenShopPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.items.size());
        for (int i = 0; i < msg.items.size(); i++) {
            // ИСПРАВЛЕНО: writeItemStack(stack, false)
            buf.writeItemStack(msg.items.get(i), false);
            buf.writeInt(msg.prices.get(i));
        }
        buf.writeInt(msg.copper);
        buf.writeInt(msg.silver);
        buf.writeInt(msg.gold);
    }

    public static OpenShopPacket decode(PacketBuffer buf) {
        int count = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // ИСПРАВЛЕНО: readItem() вместо readItemStack()
            items.add(buf.readItem());
            prices.add(buf.readInt());
        }
        return new OpenShopPacket(items, prices, buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(OpenShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    Minecraft.getInstance().setScreen(new TradeScreen(msg))
            );
        });
        ctx.get().setPacketHandled(true);
    }
}