package com.example.examplemod.network;

import com.example.examplemod.client.JewelryScreen;
import com.example.examplemod.container.JewelryContainer;
import com.example.examplemod.jewelry.IJewelryInventory;
import com.example.examplemod.jewelry.ModJewelry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenJewelryGuiPacket {
    public OpenJewelryGuiPacket() {}

    public static void encode(OpenJewelryGuiPacket msg, PacketBuffer buf) {}
    public static OpenJewelryGuiPacket decode(PacketBuffer buf) { return new OpenJewelryGuiPacket(); }

    public static void handle(OpenJewelryGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(ModJewelry.JEWELRY_CAP).ifPresent(jewelry -> {
                NetworkHooks.openGui(player, new INamedContainerProvider() {
                    @Override
                    public StringTextComponent getDisplayName() {
                        return new StringTextComponent("Бижутерия");
                    }

                    @Override
                    public JewelryContainer createMenu(int id, net.minecraft.entity.player.PlayerInventory inv, PlayerEntity p) {
                        return new JewelryContainer(id, inv, jewelry);
                    }
                }, buf -> {
                    buf.writeInt(IJewelryInventory.TOTAL_SLOTS);
                    for (int i = 0; i < IJewelryInventory.TOTAL_SLOTS; i++) {
                        buf.writeItem(jewelry.getSlot(i));
                    }
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}