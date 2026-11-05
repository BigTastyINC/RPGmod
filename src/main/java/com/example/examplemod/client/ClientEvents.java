package com.example.examplemod.client;

import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenStatsGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.OPEN_STATS.isDown() && Minecraft.getInstance().screen == null) {
            NetworkHandler.INSTANCE.sendToServer(new OpenStatsGuiPacket());
        }
    }
}