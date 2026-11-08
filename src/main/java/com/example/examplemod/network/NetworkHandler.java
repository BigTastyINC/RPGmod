package com.example.examplemod.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("examplemod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, SyncLevelPacket.class, SyncLevelPacket::encode, SyncLevelPacket::decode, SyncLevelPacket::handle);
        INSTANCE.registerMessage(id++, OpenStatsGuiPacket.class, OpenStatsGuiPacket::encode, OpenStatsGuiPacket::decode, OpenStatsGuiPacket::handle);
        INSTANCE.registerMessage(id++, UpgradeStatPacket.class, UpgradeStatPacket::encode, UpgradeStatPacket::decode, UpgradeStatPacket::handle);
        INSTANCE.registerMessage(id++, BuyItemPacket.class, BuyItemPacket::encode, BuyItemPacket::decode, BuyItemPacket::handle);
        INSTANCE.registerMessage(id++, SellHeldItemPacket.class, SellHeldItemPacket::encode, SellHeldItemPacket::decode, SellHeldItemPacket::handle);
        INSTANCE.registerMessage(id++, OpenSpellBookGuiPacket.class, OpenSpellBookGuiPacket::encode, OpenSpellBookGuiPacket::decode, OpenSpellBookGuiPacket::handle);
        INSTANCE.registerMessage(id++, SellSlotsPacket.class, SellSlotsPacket::encode, SellSlotsPacket::decode, SellSlotsPacket::handle);
        INSTANCE.registerMessage(id++, SyncBuffPacket.class, SyncBuffPacket::encode, SyncBuffPacket::decode, SyncBuffPacket::handle);
        // НОВЫЙ ПАКЕТ
        INSTANCE.registerMessage(id++, SyncOtherPlayerLevelPacket.class, SyncOtherPlayerLevelPacket::encode, SyncOtherPlayerLevelPacket::decode, SyncOtherPlayerLevelPacket::handle);
    }
}