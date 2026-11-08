package com.example.examplemod.client;

import com.example.examplemod.container.ModContainers;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientSetup {
    public static void init() {
        ClientRegistry.registerKeyBinding(KeyBindings.OPEN_STATS);
        ScreenManager.register(ModContainers.TRADE_CONTAINER.get(), TradeScreen::new);
    }
}